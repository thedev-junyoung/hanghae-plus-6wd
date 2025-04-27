## 1. **배경 (Context)**

이 문서는 **정렬 기반 목록 조회 API의 성능 병목**을 사전에 분석하고, 인덱스 최적화 방안을 도출하기 위해 작성되었다.

→ `"정렬 + 페이징 쿼리에서 발생하는 병목을 인덱스로 해결하는 것이 목표"`

---

## 2. **문제 정의**

`/api/v1/products` API는 다음과 같은 조건으로 데이터를 조회한다:

- 상품 전체 목록을 대상으로
- `ORDER BY price DESC` 정렬
- `LIMIT + OFFSET` 기반 페이징

> 해당 조합은 filesort, Full Table Scan, Top-N 정렬 병목의 대표 사례이다.
>

---

## 3. **대상 선정**

**쿼리 대상**: `product` 테이블

**문제되는 필드**: `price`

### 왜 `price`인가?

- 가격순 정렬은 **사용자 행동 패턴에서 매우 빈번하게 사용됨**
- 인덱스가 없을 경우 정렬을 위해 **전 row를 메모리에 정렬 → filesort 발생**
- 데이터가 많아질수록 **정렬 병목**이 크게 증가

---

## 4. **측정 방식**

- `EXPLAIN ANALYZE`로 실행 계획 및 실제 수행 시간 확인
- 인덱스 적용 전/후 `type`, `rows`, `Using filesort`, `실행 시간` 비교

---

## 5. **테스트 데이터 조건**

- 약 **100,000건**의 product 데이터
- 다양한 가격 분포 존재
- 정렬 기준: `price DESC`, 페이지 크기: `LIMIT 20`

---

## 6. 성능 분석 및 개선 전략

### 6.1 기본 정렬 쿼리 (`SELECT *`, 인덱스 없음)

```sql
EXPLAIN ANALYZE
SELECT *
FROM product
ORDER BY price DESC
LIMIT 20 OFFSET 0;
```

```
-> Limit: 20 row(s)
    -> Sort: price DESC, limit input to 20 row(s) per chunk
        -> Table scan on product
(actual time=0.0447..36.2 rows=100000 loops=1)
```

**📊 실행 계획 해석**

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Table scan on product` | ❌ Full Scan, 전체 10만 건 순회 |
| **정렬 처리** | `filesort` | ❌ 정렬 인덱스 없이 메모리 정렬 발생 |
| **rows** | 100,000 | 정렬을 위해 전체 데이터를 메모리 정렬 |
| **실행 시간** | `~53ms` | 매우 비효율적 (대용량에서 악화됨) |

📌 **결론:** 정렬 인덱스가 없고, SELECT *로 인해 covering index 활용 불가

---

### 6.2 인덱스 강제 적용 (`FORCE INDEX`)

```sql
EXPLAIN ANALYZE
SELECT *
FROM product FORCE INDEX (idx_product_price_desc)
ORDER BY price DESC
LIMIT 20 OFFSET 0;
```

```
-> Index scan on product using idx_product_price_desc
(actual time=0.0738..0.153 rows=20 loops=1)
```

**📊 인덱스 강제 적용 결과**

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Index scan` | ✅ 정렬 인덱스 사용 |
| **정렬 처리** | 없음 | ✅ 인덱스 순서로 바로 정렬된 결과 추출 |
| **rows** | 20 | 필요한 Top-N만 추출 |
| **실행 시간** | `~0.15ms` | 기존 대비 약 **350배 성능 향상** |

📌 **주의:** 옵티마이저를 우회하므로 실무에서는 반복 검증 후 제한적으로 적용

---

### ⚠️ FORCE INDEX 사용 시 주의점

| 항목 | 설명 |
| --- | --- |
| **장점** | 옵티마이저가 무시한 인덱스를 강제로 사용하여 정렬 효율 확보 |
| **단점** | 옵티마이저가 더 나은 계획을 찾을 수 있는 가능성 차단 |
| **실무 기준** | 성능이 반복적으로 검증된 쿼리에서만 제한적으로 사용 권장. 모니터링 및 주석 필수 |

---

### 6.3 필드 제한 시도 (Covering Index 유도)

```sql
EXPLAIN ANALYZE
SELECT id, name, price
FROM product
ORDER BY price DESC
LIMIT 20 OFFSET 0;
```

```
-> Table scan on product
-> Sort: price DESC
(actual time=0.207..32.9 rows=100000 loops=1)
```

**📊 결과 해석**

| 항목 | 값 | 해석 |
| --- | --- | --- |
| **Table Access** | `Table scan` | ❌ 여전히 Full Scan |
| **정렬 처리** | `filesort` | ❌ 정렬 인덱스 미사용 |
| **실행 시간** | `~46ms` | 여전히 병목 발생 |

📌 **결론:** 인덱스가 있어도 옵티마이저가 선택하지 않으면 성능 개선 불가

---

**📊 인덱스 적용 전/후 비교표**

| 항목 | 인덱스 전 | 인덱스 후 (`FORCE INDEX`) |
| --- | --- | --- |
| **type** | `ALL` (Full Table Scan) | `index scan` ✅ |
| **rows** | 100,000 | 20 |
| **Using filesort** | ✅ 있음 | ❌ 없음 |
| **실행 시간** | `~53ms` | `~0.15ms` ✅ |
| **인덱스** | ❌ 없음 | `idx_product_price_desc` |

---

## 6.4 전략별 비교 요약

| 전략 | 인덱스 사용 | 정렬 처리 방식 | 실행 시간 | 특징 및 실무 적합도 |
| --- | --- | --- | --- | --- |
| 기본 OFFSET 방식 (`SELECT *`) | ❌ 미사용 | `filesort` + Full Scan | `~53ms` | ❌ 비효율적, 대량 데이터에서 병목 발생 |
| 필드 제한 (Covering Index 시도) | ❌ 미사용 | `filesort` + Full Scan | `~46ms` | ❌ 옵티마이저가 인덱스 선택하지 않음 |
| FORCE INDEX + `SELECT *` | ✅ 사용 | 정렬 생략 (인덱스 순회) | `~0.15ms` | ⚠️ 옵티마이저 무시, 테스트 후 조건부 추천 |
| 필드 제한 + FORCE INDEX | ✅ 사용 | 정렬 생략 (covering index) | `~0.10ms` 예상 | ✅ 안정적, 추천 구조 |
| Keyset Pagination (price 기준) | ✅ 사용 | 인덱스 범위 스캔 | `~0.20ms` 내외 | ⭐ 실시간 인기순 조회에 최적 |

---

## 7. 결론 및 가이드

- `price DESC` 정렬 쿼리는 **데이터량이 많을수록 filesort 병목이 심화**
- `SELECT *` 사용 시 옵티마이저는 인덱스를 비선택할 가능성이 높음
- **FORCE INDEX**로 인덱스 강제 사용 가능하나, **운영 환경에서는 반복 검증 후 적용 필요**
- 가장 효과적인 전략은:
    - `필드 제한 + 정렬 인덱스 → Covering Index 구조` 유도
    - 또는 `price < ?` 기반의 **Keyset Pagination** 적용