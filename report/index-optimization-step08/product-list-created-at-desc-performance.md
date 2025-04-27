## 1. **배경 (Context)**

이 문서는 **정렬 기반 목록 조회 API의 성능 병목**을 사전에 분석하고, 인덱스 최적화 방안을 도출하기 위해 작성되었다.

→ `"정렬 + 페이징 쿼리에서 발생하는 병목을 인덱스로 해결하는 것이 목표"`

---

## 2. **문제 정의**

`/api/v1/products` API는 다음과 같은 조건으로 데이터를 조회한다:

- 상품 전체 목록 대상
- `ORDER BY created_at DESC` 정렬
- `LIMIT + OFFSET` 페이징 사용

해당 조합은 **Full Table Scan + filesort + Top-N 정렬 병목**의 대표적인 패턴으로,

대량 데이터 환경에서는 응답 시간이 급격히 저하될 수 있다.

---

## 3. **대상 선정**

쿼리 대상: `product` 테이블

문제되는 필드: `created_at`

### 왜 `created_at`인가?

- 상품 등록일 기준 정렬은 목록 조회에서 가장 일반적인 정렬 기준
- 정렬 인덱스가 없으면 → filesort + 전체 데이터 스캔 발생
- 실시간 서비스에서 최신순 리스트는 자주 호출되므로 병목 우려가 크다

---

## 4. **측정 방식**

- `EXPLAIN ANALYZE` 사용
- 인덱스 적용 전/후 `type`, `rows`, `Using filesort`, `실행 시간` 비교
- 정렬 방식 및 LIMIT 병목 여부 파악

---

## 5. **테스트 데이터 조건**

- product 테이블 row 수: 약 100,000건
- 다양한 created_at 값 보유
- 정렬 기준: `created_at DESC`, LIMIT 20
- 전체 row에서 최신 20개를 조회하는 시나리오로 테스트

---

## 6. 실행 쿼리 및 성능 개선 과정

### 6.1 기본 정렬 쿼리 (`SELECT *`, 인덱스 없음)

```sql
EXPLAIN ANALYZE
SELECT *
FROM product
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;
```

```sql
-> Limit: 20 row(s)  (cost=1.45 rows=12) (actual time=59..59 rows=20 loops=1)
    -> Sort: product.created_at DESC, limit input to 20 row(s) per chunk
        -> Table scan on product (actual time=0.0956..40.2 rows=100000 loops=1)
```

---

### 📊 실행 계획 해석

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Table scan on product` | ❌ 100,000건 Full Scan |
| **정렬 처리** | `Sort: created_at DESC` + filesort | ❌ 정렬 인덱스 없이 메모리 정렬 |
| **rows** | 100,000 | 정렬 대상 row 수 |
| **LIMIT** | 20 | 결과는 적지만 전체 정렬 필요 |
| **실행 시간** | 약 `59ms` | 병목 발생 (정렬 + 테이블 접근) |

---

### 6.2 개선 방안 : 정렬 인덱스 생성

가장 기본적인 접근은 정렬 기준 필드(`created_at`)에 대한 인덱스 생성

```sql
CREATE INDEX idx_product_created_at_desc ON product(created_at DESC);
```

---

**✅ 테스트할 실행 쿼리 A-2: 인덱스 생성 후 정렬 성능 확인**

```sql
EXPLAIN ANALYZE
SELECT *
FROM product
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;
```

```sql
-> Limit: 20 row(s)  (cost=1.45 rows=12) (actual time=54.9..54.9 rows=20 loops=1)
    -> Sort: product.created_at DESC, limit input to 20 row(s) per chunk  (cost=1.45 rows=12) (actual time=54.9..54.9 rows=20 loops=1)
        -> Table scan on product  (cost=1.45 rows=12) (actual time=0.0635..37.6 rows=100000 loops=1)
```

### 6.3 개선 방안 : 적용 결과 분석

**📊 실행 계획 비교 (`SELECT * FROM product ORDER BY created_at DESC LIMIT 20`)**

| 항목 | 인덱스 없음 (기존) | 인덱스 있음 (idx_product_created_at_desc) |
| --- | --- | --- |
| **Table Access** | Table Scan | Table Scan |
| **정렬 처리** | Sort + filesort | Sort + filesort |
| **rows** | 100,000 | 100,000 |
| **실행 시간** | ~59ms | ~54.9ms |

---

**❌ 결론**

- **정렬 인덱스 생성만으로는 실행 계획에 변화 없음**
- 이유: `SELECT *` 때문에 **MySQL은 결국 테이블까지 접근해야 하므로**, 인덱스를 통해 얻을 수 있는 이점이 상쇄됨
- 옵티마이저는 여전히 **"인덱스를 타느니 그냥 전체 정렬하자"** 고 판단

### 6.4 개선 방안 2 : 필드 제한 + Covering Index 유도

이번에는 인덱스가 실질적으로 **정렬 + 조회 모두를 처리**할 수 있도록 필드 수를 제한해보자.

→ 즉, **covering index**를 활용한 최적화 시도야.

---

**🧪 테스트 쿼리 B-1: 필요한 필드만 선택**

```sql
EXPLAIN ANALYZE
SELECT id, name, created_at
FROM product
ORDER BY created_at DESC
LIMIT 20;
```

## 6.5 개선 방안 : 적용 결과 분석

```sql
SELECT id, name, created_at
FROM product
ORDER BY created_at DESC
LIMIT 20;
```

**📊 실행 계획 요약**

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Table scan on product` | ❌ 전체 테이블 스캔 여전함 |
| **정렬 처리** | `Sort + filesort` | ❌ 인덱스만으로 정렬되지 않음 |
| **rows** | 100,000 | 정렬 대상 row 수는 여전히 많음 |
| **LIMIT** | 20 | 결과는 적지만, 정렬 부담 존재 |
| **실행 시간** | 약 `54.7ms` | 소폭 개선됐지만 병목 여전 |

📌 **결론**

- `SELECT *` → `SELECT id, name, created_at`로 줄였지만, 여전히 **Covering Index**로 동작하지 않음.
- 이유는 현재 생성된 인덱스가 `created_at DESC` 하나뿐이고, 쿼리에 필요한 다른 컬럼(`id`, `name`)이 인덱스에 없어서 **결국 테이블에 접근**함.
- 따라서 **인덱스만으로 데이터를 처리할 수 없고**, MySQL은 **Table Scan + Sort**를 유지함.

---

## 6.6 개선 방안 : Covering Index 적용

필요한 필드만 선택한 쿼리에 대해, 인덱스가 **정렬 + 조회를 모두 처리**할 수 있도록 **Covering Index**를 생성하여 최적화 효과를 확인.

**✅ 생성한 인덱스**

```sql
CREATE INDEX idx_product_covering ON product(created_at DESC, id, name);
```

- `created_at DESC` 정렬 기준 포함
- `id`, `name` 필드를 함께 포함시켜 **인덱스만으로 결과 도출 가능**
- 즉, **인덱스 레벨에서 모든 작업이 처리되어 Table Access 자체가 생략**됨

---

**🧪 테스트 쿼리 B-2: Covering Index 적용 후**

```sql
EXPLAIN ANALYZE
SELECT id, name, created_at
FROM product
ORDER BY created_at DESC
LIMIT 20;
```

```sql
-> Limit: 20 row(s)  (cost=1.45 rows=12) (actual time=0.039..0.0529 rows=20 loops=1)
    -> Covering index scan on product using idx_product_covering  (cost=1.45 rows=12) (actual time=0.0379..0.0504 rows=20 loops=1)
```

---

**📊 실행 계획 비교 (`SELECT id, name, created_at FROM product ORDER BY created_at DESC LIMIT 20`)**

| 항목 | 인덱스 없음 (기존) | Covering Index 적용 |
| --- | --- | --- |
| **Table Access** | Table Scan | ✅ Covering Index Scan |
| **정렬 처리** | Sort + filesort | ✅ 인덱스 기반 정렬 |
| **rows** | 100,000 | 20 |
| **실행 시간** | ~54.7ms | ✅ **~0.05ms** |

---

**✅ 6.6.1 결론**

- Covering Index를 적용함으로써 MySQL은 **인덱스 레벨에서 정렬 및 조회 모두 수행** 가능해짐
- `Table Scan`, `filesort`, `Sort` 전부 제거됨
- 실행 시간은 **약 1000배 이상 단축 (~54ms → ~0.05ms)**
- 실시간 목록 API에서 병목을 해소하는 **가장 효과적인 인덱스 최적화 방식**임

## 6.7 개선 방안 : OFFSET 병목 → Cursor 기반 페이징으로 전환

기존 `LIMIT + OFFSET` 방식은 페이지가 뒤로 갈수록 성능이 급격히 저하되는 구조다. 이를 해결하기 위해, **Cursor 기반 페이징 (Seek 방식)** 을 도입하여 성능 최적화를 유도한다.

**🧩 기존 방식의 문제점**

기존 `OFFSET` 방식:

```sql
EXPLAIN ANALYZE
SELECT id, name, created_at
FROM product
ORDER BY created_at DESC
LIMIT 20 OFFSET 10000;
```

```sql
-> Limit/Offset: 20/10000 row(s)  (cost=1.45 rows=0) (actual time=4.62..4.62 rows=20 loops=1)
    -> Covering index scan on product using idx_product_covering  (cost=1.45 rows=12) (actual time=0.0577..4.16 rows=10020 loops=1)
```

- 이 쿼리는 **앞의 10,000건을 스킵하고** 그 다음 20건을 가져오는데,
- 실제로는 10,020건을 **다 훑고 나서 20건만 리턴**하는 구조.
- → `OFFSET`이 커질수록 성능이 선형적으로 느려짐 (병목 💀)

**✅ 기존 방식: OFFSET 방식 페이징**

```sql
EXPLAIN ANALYZE
SELECT id, name, created_at
FROM product
ORDER BY created_at DESC
LIMIT 20 OFFSET 10000;
```

```sql
-> Limit/Offset: 20/10000 row(s)  (cost=1.45 rows=0) (actual time=4.62..4.62 rows=20 loops=1)
    -> Covering index scan on product using idx_product_covering  (cost=1.45 rows=12) (actual time=0.0577..4.16 rows=10020 loops=1)
```

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Covering index scan` | ✅ 인덱스는 사용하지만... |
| **정렬 처리** | 이미 인덱스에 의해 정렬되어 있음 | ⭕ 효과 있음 |
| **OFFSET** | 10,000 | ❌ 앞의 10,000건을 모두 스캔 |
| **rows** | 10,020 | 불필요한 row 접근 많음 |
| **실행 시간** | 약 `4.62ms` | ❌ 커질수록 성능 저하 |

📌 **결론:** OFFSET이 클수록 탐색 비용이 증가하며, 불필요한 row 수만큼 리소스 낭비 발생

---

**✅ 개선 방식: Cursor 기반 페이징 (Seek 방식)**

```sql
EXPLAIN ANALYZE
SELECT id, name, created_at
FROM product
WHERE created_at < '2025-04-17 23:59:59'
ORDER BY created_at DESC
LIMIT 20;
```

```sql
-> Limit: 20 row(s)  (cost=2.84 rows=6) (actual time=0.084..0.109 rows=20 loops=1)
    -> Filter: (product.created_at < TIMESTAMP'2025-04-17 23:59:59')
        -> Covering index range scan on product using idx_product_covering over ('2025-04-17 23:59:59' < created_at)
```

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Covering index range scan` | ✅ 범위 탐색으로 정확한 위치부터 조회 |
| **정렬 처리** | 인덱스에 정렬 포함 | ✅ 추가 정렬 없음 |
| **조건** | `created_at < '2025-04-17 23:59:59'` | ✅ 커서 기반 탐색 |
| **rows** | 20 | 딱 필요한 만큼만 스캔 |
| **실행 시간** | 약 `0.109ms` | ✅ 매우 빠름 |

📌 **결론:** 커서 기준으로 탐색을 시작하여, 성능이 **페이지 수와 무관하게 일정**함

---

**✅ 최종 비교 요약**

| 방식 | Table Access | 정렬 처리 | rows 접근 수 | 실행 시간 |
| --- | --- | --- | --- | --- |
| OFFSET 방식 | Covering Index Scan | 인덱스 정렬 | 10,020 | ~4.62ms |
| **Cursor 방식** | Covering Index **Range Scan** | ✅ 인덱스 정렬 | ✅ 20 | ✅ ~0.11ms |

---

### 7. ✅ 결론

OFFSET 방식은 페이지가 뒤로 갈수록 심각한 성능 저하를 초래하는 반면, Cursor 기반 페이징은 인덱스를 100% 활용해 일관된 고속 응답 성능을 보장한다.
특히, 정렬 인덱스 + Covering Index + Cursor 방식의 조합은 정렬 기반 목록 조회에서 최상의 성능을 확보할 수 있는 가장 효과적인 전략이다.