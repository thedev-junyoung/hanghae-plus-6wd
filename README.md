# **STEP10 - Finalize 과제**

## 📌 프로젝트 목표

이번 단계에서는 **전체 API의 가용성 확보**를 목표로, 기존 도메인 기능들을 통합하고 부가 기능(필터, 인터셉터, 스케줄러 등)을 구현하였습니다.

또한 모든 시나리오에 대한 **E2E 테스트**를 작성하여, 운영 환경에서도 안정적으로 동작할 수 있는 수준으로 애플리케이션을 완성하는 것이 목적입니다.

---

## ✅ 주요 구현 항목

### 1. 🔄 API 통합 및 테스트 보강

| API | 테스트 방식 | 클래스명 |
| --- | --- | --- |
| **잔액** | MockMvc + RestTemplate | `BalanceControllerIntegrationTest`, `BalanceApiIntegrationTest` |
| **쿠폰** | MockMvc + RestTemplate | `CouponControllerIntegrationTest`, `CouponApiIntegrationTest` |
| **주문** | MockMvc + RestTemplate | `OrderControllerIntegrationTest`, `OrderApiIntegrationTest` |
| **결제** | MockMvc + RestTemplate+ 주문 생성 동적 처리 | `PaymentControllerIntegrationTest`, `PaymentApiIntegrationTest` |
| **상품** | MockMvc + RestTemplate | `ProductControllerIntegrationTest`, `ProductApiIntegrationTest` |

각 테스트는 실제 흐름에 가까운 요청/응답 기반 시나리오를 포함하고 있으며, `X-USER-ID` 인증 헤더가 반드시 포함되어야 합니다.

---

### 2. 🧩 공통 컴포넌트 (부가 기능)

| 컴포넌트 | 설명 |
| --- | --- |
| **`DevAuthInterceptor`** | 요청마다 `X-USER-ID` 헤더 존재 확인, 없으면 401 반환 |
| **`LoggingFilter`** | 요청 URI 및 메서드를 로그로 출력하여 추적 가능 |
| **`CouponExpiryScheduler`** | 만료된 쿠폰 자동 비활성화, 매일 0시 실행 예정 (스케줄러 테스트 포함) |



### 📁 경로 가이드

- 공통 필터/인터셉터: `kr.hhplus.be.server.common.filter` / `interceptor`
- API 테스트: `src/test/java/kr/hhplus/be/server/interfaces/**`
- 스케줄러 테스트: `CouponExpirySchedulerTest`



## ⚙️ 실행 방법

### 1. DB 및 초기 데이터 구성

```bash
./init/reset-db.sh
```

- MySQL 컨테이너를 완전히 초기화하고, `init/01-schema.sql`, `02-data.sql`을 자동 적용합니다.

- `./data/mysql` 디렉토리도 함께 초기화되어 **일관된 테스트 조건이 보장**됩니다.


---

### 2. 테스트 실행

```bash
./gradlew test
```

- **Testcontainers 기반 통합 테스트**가 실행됩니다.
- DB 설정을 별도로 할 필요 없이 `application.yml` 설정 없이 자동 구성됩니다.
- 모든 테스트는 `@Transactional`로 격리되어 **신뢰 가능한 시뮬레이션 환경**에서 실행됩니다.
