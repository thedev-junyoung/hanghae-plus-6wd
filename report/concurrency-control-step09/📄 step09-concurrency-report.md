> 본 보고서는 서비스 내 주요 기능에서 발생할 수 있는 동시성 문제를 식별하고,
RDBMS 기반의 동시성 제어 방식(Optimistic / Pessimistic Lock)을 통해 이를 해결한 과정을 정리합니다.
>

---

## 1. 문제 식별 (Context & Issue)

## 🧩 문제 식별

우리 서비스는 사용자 간 다음과 같은 **경쟁 조건(Race Condition)**이 발생할 수 있다:

| 시나리오 | 설명 |
| --- | --- |
| **잔액 충전** | 여러 요청이 동시에 충전되면 중복 반영 가능성 |
| **쿠폰 발급** | 수량 제한된 쿠폰에 대해 동시 요청 시 초과 발급 |
| **주문** | 재고 차감 시 동시 주문으로 인해 재고 초과 판매 발생 가능 |
| **결제** | 동일 주문에 대해 중복 결제 요청 시 이중 결제 가능성 |

이러한 동시성 이슈는 **트랜잭션 격리 수준**만으로 제어하기 어렵고, 적절한 **락 전략(DB Lock)**이 요구된다.

---

## 🔍 분석

각 시나리오별로 발생 가능한 이슈와 기존 구조의 한계를 아래와 같이 도출했다:

### 1. **Balance 충전**

- 문제: `@Retryable`로 재시도하지만 중복 충전 위험
- 원인: 낙관적 락으로 충돌은 감지되나, **성공한 후 재시도 요청 차단 로직 없음**
- 보완 필요: **멱등성 처리(requestId 기반)** 추가 필요

### 2. **쿠폰 발급**

- 문제: 재고 초과 발급
- 원인: `remainingQuantity--` 시점의 동시 접근
- 보완 필요: DB Row-level 락 필요

### 3. **주문/재고**

- 문제: 10개 재고에 5개씩 주문 시, 최대 2명만 성공해야 함
- 원인: select 후 재고 감소 전 타 사용자 접근 가능
- 보완 필요: select for update

### 4. **결제**

- 문제: 하나의 주문에 대해 여러 결제 요청 → 중복 결제
- 원인: 주문 상태 미확인 또는 선점 미실행
- 보완 필요: 주문 상태 변경 전 락을 통한 선점

---

## 🛠 해결

### ✅ 1. 잔액 충전 (Balance)

- `@Version`으로 낙관적 락 적용
- `@Retryable`(5회, 100ms backoff)로 충돌 재시도
- **requestId 기반 멱등성 처리**:
    - `balanceHistoryRepository.existsByRequestId(...)` 선 체크
    - 이미 처리된 충전은 이전 잔액 그대로 반환
- **추가 보호**: `InMemoryRateLimiter` 도입으로 "따닥 클릭" 차단

```java
if (balanceHistoryRepository.existsByRequestId(command.requestId())) {
      log.warn("이미 처리된 충전 요청입니다: userId={}, requestId={}", command.userId(), command.requestId());
      Balance existing = balanceRepository.findByUserId(command.userId()).orElseThrow();
      return BalanceInfo.from(existing); // 이전 충전 결과 그대로 반환
}
```

### ✅ 2. 쿠폰 발급

- `@Lock(PESSIMISTIC_WRITE)` 사용한 `findByCodeForUpdate`
- `remainingQuantity` 검사 및 차감 시점 락 보장
- 중복 발급 여부: 사용자+쿠폰 조합으로 중복 방지

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.code = :code")
Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
```

### ✅ 3. 주문 (재고 차감)

- `ProductStockRepository.findByProductIdAndSizeForUpdate()` 사용
- select + 재고 수량 감소를 트랜잭션 내에서 보장
- 실패 시 예외로 대응, 재시도는 미처리 (명시적 실패 처리)

### ✅ 4. 결제

- 주문 조회 시 `findByIdForUpdate()` 사용해 선점
- 잔액 차감 → 결제 저장 → 주문 상태 변경 트랜잭션 내 처리
- 복수 결제 요청 → 한 명만 성공, 나머지는 예외 처리

---

## 🔄 대안 및 확장 방향

| 항목 | 대안 |
| --- | --- |
| 멱등성 처리 | requestId 외에도 Redis setNX 기반 분산 키 고려 |
| 비관적 락의 비용 | 낙관적 락 + queueing으로 전환 고려 |
| 재시도 전략 | backoff + jitter 등 조정 |
| 트랜잭션 경계 | 히스토리 저장은 event 기반으로 트랜잭션 분리 가능 |
| 테스트 방식 | API endpoint 기반 E2E 테스트 확장 가능 |

---

## 🧪 실험 결과 (통합 테스트)

| 테스트 클래스 | 성공 기준 | 테스트 결과 |
| --- | --- | --- |
| `BalanceConcurrencyTest` | 모든 충전 정확히 누적 | ✅ 통과 |
| `CouponConcurrencyTest` | 최대 2건 발급 | ✅ 통과 |
| `OrderConcurrencyTest` | 재고 10개 기준 최대 2건 주문 성공 | ✅ 통과 |
| `PaymentConcurrencyTest` | 1건만 결제 성공 | ✅ 통과 |

---

## ⚠️ 한계점

- `InMemoryRateLimiter`는 **단일 인스턴스 환경에만 유효** → 분산 환경에서는 Redis 도입 필요
- `@Retryable`의 실패 후 로직 제한 → 외부 재처리 시스템 고려 필요
- 테스트는 JVM 단위로 동시성을 시뮬레이션 → 실제 prod 환경과 gap 존재

---

## ✅ 결론

이번 STEP09 과제를 통해, 단순한 트랜잭션 관리가 아닌 **동시성 시나리오에 맞는 락 전략 선정**,

그리고 그에 따른 **비즈니스 로직의 재설계 및 보완(멱등성, 락 경계, 재시도, 실패 처리)**이 얼마나 중요한지를 체감할 수 있었다.

---

> "동시성 처리는 단순히 락을 거는 것이 아니다.
>
>
> 실패했을 때 무엇을 포기하고, 무엇을 반드시 지켜야 하는지를 정하는 것이 동시성 설계의 본질이다."
>

### 1.1. 주요 시나리오 및 예상 이슈

| 시나리오 | 예상 문제 | 설명 |
| --- | --- | --- |
| 잔액 충전 | 중복 충전, Race Condition | 동시에 동일한 유저가 잔액을 충전할 경우 누적 이상 발생 가능 |
| 쿠폰 발급 | 초과 발급 | 선착순 제한 쿠폰에서 동시 요청 시 발급 수량 초과 가능성 |
| 상품 재고 차감 | 음수 재고 | 다수 주문 요청이 동시에 들어올 경우 재고가 음수가 될 수 있음 |
| 결제 요청 | 중복 결제 | 동일 주문에 대해 여러 결제 시도가 발생할 수 있음 |

---

## 2. 분석 (AS-IS)

| 시나리오 | 동시성 방어 없음 시 문제 | 테스트 결과 |
| --- | --- | --- |
| 잔액 충전 | 중복 커밋 발생 → 잔액 초과 | 실제 테스트에서 `@Transactional`만 사용 시 100,000원이 530,000원까지 증가 |
| 쿠폰 발급 | 수량 제한 무시하고 초과 발급 발생 | 동시 요청 시 100개 중 105건 발급 확인 |
| 재고 차감 | `재고 >= 수량` 체크 이전에 충돌 발생 | 재고가 음수로 내려감 |
| 결제 요청 | 중복 결제 로직이 여러 번 실행됨 | 주문 상태가 여러 번 변경되거나, 잔액이 중복 차감됨 |

---

## 3. 해결 방안 (TO-BE 설계)

### 3.1. 잔액 충전 - Optimistic Lock + Retry

- `@Version` 필드 활용하여 충돌 감지
- `@Retryable`로 재시도 로직 구성
- `requestId`로 **idempotency** 보장

```java
    public BalanceInfo charge(ChargeBalanceCommand command) {
        if (balanceHistoryRepository.existsByRequestId(command.requestId())) {
            log.warn("이미 처리된 충전 요청입니다: userId={}, requestId={}", command.userId(), command.requestId());
            Balance existing = balanceRepository.findByUserId(command.userId()).orElseThrow();
            return BalanceInfo.from(existing); // 이전 충전 결과 그대로 반환
        }

        Balance balance = balanceRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BalanceException.NotFoundException(command.userId()));

        balance.charge(Money.wons(command.amount()));
        balanceRepository.save(balance);

        return BalanceInfo.from(balance);
    }
```

### 3.2. 쿠폰 발급 - Pessimistic Lock (FOR UPDATE)

`CouponRepository.findByCodeForUpdate(...)`

수량 차감 로직과 함께 트랜잭션 묶음

```java
    @Transactional
    public CouponResult issueLimitedCoupon(IssueLimitedCouponCommand command) {
        // 락 걸고 조회
        Coupon coupon = couponRepository.findByCodeForUpdate(command.couponCode());

        // 중복 발급 방지
        if (couponIssueRepository.hasIssued(command.userId(), coupon.getId())) {
            throw new CouponException.AlreadyIssuedException(command.userId(), command.couponCode());
        }

        // 도메인 책임으로 발급 생성 및 수량 차감
        CouponIssue issue = CouponIssue.create(command.userId(), coupon);

        // 저장
        couponIssueRepository.save(issue);

        return CouponResult.from(issue);
    }
```

### 3.3. 재고 차감 - Pessimistic Lock (FOR UPDATE)

`ProductStockRepository.findByProductIdAndSizeForUpdate(...)`

수량 차감 트랜잭션에 묶어 음수 방지

### 3.4. 결제 요청 - 중복 방지 로직

- 주문 상태 확인 후 처리
- 충돌 시 예외 발생 → 재시도 하지 않음
- 잔액 차감/결제 기록/주문 상태 변경을 단일 트랜잭션으로 묶음

## 4. 테스트 및 검증

✅ 동시성 테스트 100회 시도에서 잔액 초과 없음, 중복 발급 없음, 재고 음수 없음 확인
`@Retryable`, `@Transactional` 전파 수준 조정으로 문제 해결

`BalanceServiceIntegrationTest`에서 `@Transactional(propagation = NOT_SUPPORTED)`으로 테스트 트랜잭션 분리하여 `REQUIRES_NEW` 정상 반영

## 5. 대안 및 한계

| 항목 | 선택한 방식 | 비고 |
| --- | --- | --- |
| 잔액 충전 | Optimistic Lock | 트래픽 많은 경우 성능 고려 |
| 쿠폰 발급 | Pessimistic Lock | Redis를 사용할 경우 스케일 아웃 용이 |
| 재고 차감 | Pessimistic Lock | 고빈도 재고 차감에서 더 나은 확장성 |
| 결제 요청 | 상태 확인 + Optimistic | 복잡성 증가, 유연성 증가 가능 |

## 6. 결론

- 단순한 시나리오에서는 RDBMS 수준의 Lock으로 충분히 해결 가능
- **트랜잭션 경계 설정 및 전파 수준이 핵심**
- 대량 트래픽 예상 시 Redis, Kafka 등 메시지 기반 전략 고려
- 테스트와 실험을 통해 직접 병목을 체험하고 구조를 설계한 것이 가장 큰 학습

## 🧩 문제 식별

우리 서비스는 사용자 간 다음과 같은 **경쟁 조건(Race Condition)**이 발생할 수 있다:

| 시나리오 | 설명 |
| --- | --- |
| **잔액 충전** | 여러 요청이 동시에 충전되면 중복 반영 가능성 |
| **쿠폰 발급** | 수량 제한된 쿠폰에 대해 동시 요청 시 초과 발급 |
| **주문** | 재고 차감 시 동시 주문으로 인해 재고 초과 판매 발생 가능 |
| **결제** | 동일 주문에 대해 중복 결제 요청 시 이중 결제 가능성 |

이러한 동시성 이슈는 **트랜잭션 격리 수준**만으로 제어하기 어렵고, 적절한 **락 전략(DB Lock)**이 요구된다.

---

## 🔍 분석

각 시나리오별로 발생 가능한 이슈와 기존 구조의 한계를 아래와 같이 도출했다:

### 1. **Balance 충전**

- 문제: `@Retryable`로 재시도하지만 중복 충전 위험
- 원인: 낙관적 락으로 충돌은 감지되나, **성공한 후 재시도 요청 차단 로직 없음**
- 보완 필요: **멱등성 처리(requestId 기반)** 추가 필요

### 2. **쿠폰 발급**

- 문제: 재고 초과 발급
- 원인: `remainingQuantity--` 시점의 동시 접근
- 보완 필요: DB Row-level 락 필요

### 3. **주문/재고**

- 문제: 10개 재고에 5개씩 주문 시, 최대 2명만 성공해야 함
- 원인: select 후 재고 감소 전 타 사용자 접근 가능
- 보완 필요: select for update

### 4. **결제**

- 문제: 하나의 주문에 대해 여러 결제 요청 → 중복 결제
- 원인: 주문 상태 미확인 또는 선점 미실행
- 보완 필요: 주문 상태 변경 전 락을 통한 선점

---

## 🛠 해결

### ✅ 1. 잔액 충전 (Balance)

- `@Version`으로 낙관적 락 적용
- `@Retryable`(5회, 100ms backoff)로 충돌 재시도
- **requestId 기반 멱등성 처리**:
    - `balanceHistoryRepository.existsByRequestId(...)` 선 체크
    - 이미 처리된 충전은 이전 잔액 그대로 반환
- **추가 보호**: `InMemoryRateLimiter` 도입으로 "따닥 클릭" 차단

```java
if (balanceHistoryRepository.existsByRequestId(command.requestId())) {
    return BalanceInfo.from(existing); // 중복 요청 방지
}
```

### ✅ 2. 쿠폰 발급

- `@Lock(PESSIMISTIC_WRITE)` 사용한 `findByCodeForUpdate`
- `remainingQuantity` 검사 및 차감 시점 락 보장
- 중복 발급 여부: 사용자+쿠폰 조합으로 중복 방지

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.code = :code")
Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
```

### ✅ 3. 주문 (재고 차감)

- `ProductStockRepository.findByProductIdAndSizeForUpdate()` 사용
- select + 재고 수량 감소를 트랜잭션 내에서 보장
- 실패 시 예외로 대응, 재시도는 미처리 (명시적 실패 처리)

### ✅ 4. 결제

- 주문 조회 시 `findByIdForUpdate()` 사용해 선점
- 잔액 차감 → 결제 저장 → 주문 상태 변경 트랜잭션 내 처리
- 복수 결제 요청 → 한 명만 성공, 나머지는 예외 처리

---

## 🔄 대안 및 확장 방향

| 항목 | 대안 |
| --- | --- |
| 멱등성 처리 | requestId 외에도 Redis setNX 기반 분산 키 고려 |
| 비관적 락의 비용 | 낙관적 락 + queueing으로 전환 고려 |
| 재시도 전략 | backoff + jitter 등 조정 |
| 트랜잭션 경계 | 히스토리 저장은 event 기반으로 트랜잭션 분리 가능 |
| 테스트 방식 | API endpoint 기반 E2E 테스트 확장 가능 |

---

## 🧪 실험 결과 (통합 테스트)

| 테스트 클래스 | 성공 기준 | 테스트 결과 |
| --- | --- | --- |
| `BalanceConcurrencyTest` | 모든 충전 정확히 누적 | ✅ 통과 |
| `CouponConcurrencyTest` | 최대 2건 발급 | ✅ 통과 |
| `OrderConcurrencyTest` | 재고 10개 기준 최대 2건 주문 성공 | ✅ 통과 |
| `PaymentConcurrencyTest` | 1건만 결제 성공 | ✅ 통과 |

---

## ⚠️ 한계점

- `InMemoryRateLimiter`는 **단일 인스턴스 환경에만 유효** → 분산 환경에서는 Redis 도입 필요
- `@Retryable`의 실패 후 로직 제한 → 외부 재처리 시스템 고려 필요
- 테스트는 JVM 단위로 동시성을 시뮬레이션 → 실제 prod 환경과 gap 존재

---

## ✅ 결론

이번 STEP09 과제를 통해, 단순한 트랜잭션 관리가 아닌 **동시성 시나리오에 맞는 락 전략 선정**,

그리고 그에 따른 비즈니스 로직의 재설계 및 보완(멱등성, 락 경계, 재시도, 실패 처리)이 얼마나 중요한지를 체감할 수 있었다.

---

> "동시성 처리는 단순히 락을 거는 것이 아니다.
>
>
> 실패했을 때 무엇을 포기하고, 무엇을 반드시 지켜야 하는지를 정하는 것이 동시성 설계의 본질이다."
>