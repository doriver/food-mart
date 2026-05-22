# SQL 성능 개선 예시 15가지

## 목차

### Part 1. 쿼리 구조 개선 (3가지)
- [1. 일별 매출 조회 — DATE() 함수 + 복합 인덱스 부재](#1-일별-매출-조회--date-함수--복합-인덱스-부재)
- [2. 재고 부족 알림 — JOIN 전 필터링 누락](#2-재고-부족-알림--join-전-필터링-누락)
- [3. 피킹 완료 현황 — HAVING 집계 후 필터](#3-피킹-완료-현황--having-집계-후-필터)

### Part 2. 집계 방식 개선 (3가지)
- [4. 인기 상품 순위 — order_item 직접 집계 vs item_sales_count](#4-인기-상품-순위--order_item-직접-집계-vs-item_sales_count)
- [5. 유저 주문 이력 — OFFSET vs Cursor 페이지네이션](#5-유저-주문-이력--offset-vs-cursor-페이지네이션)
- [6. 창고별 재고 현황 — 스칼라 서브쿼리 N번 vs GROUP BY JOIN](#6-창고별-재고-현황--스칼라-서브쿼리-n번-vs-group-by-join)

### Part 3. 인덱스 개선 — 기본 (3가지)
- [7. 복합 인덱스 — order(user_id, status, created_at)](#7-복합-인덱스--orderuser_id-status-created_at)
- [8. 커버링 인덱스 — picking(order_id, picking_status)](#8-커버링-인덱스--pickingorder_id-picking_status)
- [9. 카디널리티 함정 — inbound_item 단독 status 인덱스](#9-카디널리티-함정--inbound_item-단독-status-인덱스)

### Part 4. 인덱스 개선 — 복잡한 쿼리 (3가지)
- [10. 출고 지연 SLA 탐지 — 다중 조인 + 날짜 연산](#10-출고-지연-sla-탐지--다중-조인--날짜-연산)
- [11. 피킹 완료율 집계 — GROUP BY + 날짜 범위](#11-피킹-완료율-집계--group-by--날짜-범위)
- [12. 재고 소진 위험 탐지 — 상관 서브쿼리 + 집계 중첩](#12-재고-소진-위험-탐지--상관-서브쿼리--집계-중첩)

### Part 5. 인덱스 + JOIN 최적화 (3가지)
- [13. 카테고리별 재고 현황 — JOIN ON 절 복합 인덱스 (기본)](#13-카테고리별-재고-현황--join-on-절-복합-인덱스-기본)
- [14. 담당자별 출고 현황 — JOIN 체인 드라이빙 테이블 커버링 (기본)](#14-담당자별-출고-현황--join-체인-드라이빙-테이블-커버링-기본)
- [15. 상품별 입고-출고 비교 — 서브쿼리 4중 JOIN 커버링 (복잡)](#15-상품별-입고-출고-비교--서브쿼리-4중-join-커버링-복잡)

---

## Part 1. 쿼리 구조 개선

### 1. 일별 매출 조회 — DATE() 함수 + 복합 인덱스 부재

**상황**: 대시보드에서 지난 30일 일별 매출을 조회. 데이터가 쌓일수록 응답이 수초씩 걸림.

**느린 쿼리**

```sql
SELECT DATE(o.created_at)       AS date,
       COUNT(DISTINCT o.id)     AS order_count,
       SUM(oi.count * oi.price) AS daily_revenue
FROM `order` o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND o.status = 'PAID'
GROUP BY DATE(o.created_at)
ORDER BY date;
```

**문제점**
- `Order`에 `idx_user(user_id)` 인덱스만 존재. `status`, `created_at` 인덱스 없음 → 풀 테이블 스캔
- `DATE(o.created_at)` 함수 적용 컬럼으로 GROUP BY → 인덱스 사용 불가, filesort 발생

**개선 쿼리**

```sql
-- 1. 인덱스 추가 (DDL)
CREATE INDEX idx_status_created_at ON `order` (status, created_at);

-- 2. GROUP BY 에서 함수 제거
SELECT DATE(o.created_at)       AS date,
       COUNT(DISTINCT o.id)     AS order_count,
       SUM(oi.count * oi.price) AS daily_revenue
FROM `order` o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.status = 'PAID'
  AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY o.created_at
ORDER BY DATE(o.created_at);
```

**핵심**: `(status, created_at)` 순서인 이유는 `status = 'PAID'` 등치 조건이 먼저, 이후 `created_at` range scan. 순서가 반대면 range 이후 등치 필터가 인덱스 효과를 잃음.

---

### 2. 재고 부족 알림 — JOIN 전 필터링 누락

**상황**: 운영팀이 매일 아침 재고 임계치 이하 상품을 확인. 비활성 상품까지 조인 후 버리는 비용이 누적됨.

**느린 쿼리**

```sql
SELECT i.name, s.count, w.name AS warehouse
FROM stock s
JOIN item i ON s.item_id = i.id
JOIN warehouse w ON s.warehouse_id = w.id
WHERE s.count < 10
  AND i.status = 'ACTIVE'
ORDER BY s.count ASC;
```

**문제점**
- `stock.count` 인덱스 없음 → stock 풀 스캔
- `i.status = 'ACTIVE'` 필터가 JOIN 이후 적용 → 비활성 상품의 재고 행까지 조인하고 버림

**개선 쿼리**

```sql
-- 1. 커버링 인덱스 추가
CREATE INDEX idx_stock_count_item ON stock (count, item_id, warehouse_id);

-- 2. ACTIVE 상품만 먼저 서브쿼리로 추린 뒤 조인
SELECT i.name, s.count, w.name AS warehouse
FROM stock s
JOIN (
    SELECT id FROM item WHERE status = 'ACTIVE'
) active_item ON s.item_id = active_item.id
JOIN item i ON s.item_id = i.id
JOIN warehouse w ON s.warehouse_id = w.id
WHERE s.count < 10
ORDER BY s.count ASC;
```

**핵심**: 조인 대상 자체를 줄이는 것이 인덱스보다 먼저 적용되어야 할 원칙. 서브쿼리로 ACTIVE 상품 ID만 먼저 걸러낸 후 stock과 조인하면 조인 행 수가 대폭 감소.

---

### 3. 피킹 완료 현황 — HAVING 집계 후 필터

**상황**: 출고 등록 전 미완료 피킹이 남아 있는 주문을 확인하는 창고 화면. 주문이 수천 건 쌓이면서 느려짐.

**느린 쿼리**

```sql
SELECT o.id AS order_id,
       COUNT(p.id)                                                      AS total_picking,
       SUM(CASE WHEN p.picking_status = 'COMPLETED' THEN 1 ELSE 0 END) AS done,
       SUM(CASE WHEN p.picking_status = 'READY'     THEN 1 ELSE 0 END) AS pending
FROM `order` o
JOIN picking p ON p.order_id = o.id
GROUP BY o.id
HAVING pending > 0;
```

**문제점**
- `HAVING pending > 0`은 전체 주문을 집계한 뒤 필터링 → 완료된 주문까지 모두 GROUP BY 후 버림
- `picking_status` 인덱스 없으면 picking 풀 스캔

**개선 쿼리**

```sql
-- 1. 복합 인덱스 추가
CREATE INDEX idx_picking_status_order ON picking (picking_status, order_id);

-- 2. READY 상태인 주문 ID를 EXISTS로 사전 필터링 후 집계
SELECT o.id AS order_id,
       COUNT(p.id)                                                      AS total_picking,
       SUM(CASE WHEN p.picking_status = 'COMPLETED' THEN 1 ELSE 0 END) AS done,
       SUM(CASE WHEN p.picking_status = 'READY'     THEN 1 ELSE 0 END) AS pending
FROM `order` o
JOIN picking p ON p.order_id = o.id
WHERE EXISTS (
    SELECT 1 FROM picking rp
    WHERE rp.order_id = o.id
      AND rp.picking_status = 'READY'
)
GROUP BY o.id;
```

**핵심**: EXISTS 서브쿼리가 `(picking_status, order_id)` 인덱스를 타고 READY가 있는 주문 ID만 빠르게 걸러냄. GROUP BY 집계 대상 자체가 줄어들어 전체 처리량 감소.

---

## Part 2. 집계 방식 개선

### 4. 인기 상품 순위 — order_item 직접 집계 vs item_sales_count

**상황**: 상품 목록 인기순 정렬, 메인 배너 인기 상품 노출. order_item 직접 집계로 구현되어 있어 주문 누적 시 느려짐.

**느린 쿼리**

```sql
SELECT i.id, i.name,
       SUM(oi.count) AS total_sold
FROM order_item oi
JOIN item i ON oi.item_id = i.id
JOIN `order` o ON oi.order_id = o.id
WHERE o.status = 'PAID'
GROUP BY i.id
ORDER BY total_sold DESC
LIMIT 10;
```

**문제점**
- 주문이 쌓일수록 `order_item` 전체를 스캔 후 집계 → 선형으로 느려짐
- 프로젝트에 `item_sales_count` 테이블과 `ItemSalesCountSyncScheduler` 가 이미 존재하지만 미활용

**개선 쿼리**

```sql
-- ItemSalesCountSyncScheduler가 주기적으로 동기화한 집계 테이블 활용
SELECT i.id, i.name,
       isc.count AS total_sold
FROM item_sales_count isc
JOIN item i ON isc.item_id = i.id
WHERE i.status = 'ACTIVE'
ORDER BY isc.count DESC
LIMIT 10;
```

**핵심**: `item_sales_count`는 이미 집계된 값이라 조회가 O(1)에 가까움. 스케줄러 주기(예: 1시간)만큼 데이터가 지연되지만 인기 순위는 실시간성이 불필요한 경우가 대부분.

---

### 5. 유저 주문 이력 — OFFSET vs Cursor 페이지네이션

**상황**: 마이페이지 주문 내역 무한스크롤. 뒤 페이지로 갈수록 응답이 느려진다는 민원 발생.

**느린 쿼리 (Offset 방식)**

```sql
-- 10페이지 요청 (OFFSET 90)
SELECT o.id, o.status, o.created_at, o.delivery_address
FROM `order` o
WHERE o.user_id = :userId
ORDER BY o.created_at DESC
LIMIT 10 OFFSET 90;
```

**문제점**
- `OFFSET 90`은 앞의 90개 행을 읽고 버린 뒤 10개 반환
- 페이지가 뒤로 갈수록 버리는 행이 선형 증가 → OFFSET 1000이면 1000개 읽고 버림
- `idx_user(user_id)` 인덱스가 있어도 OFFSET 자체가 비효율

**개선 쿼리 (Cursor 방식)**

```sql
-- 클라이언트가 마지막으로 받은 order.id(lastId) 를 파라미터로 넘김
-- 항상 인덱스 range scan으로 정확히 10건만 읽음
SELECT o.id, o.status, o.created_at, o.delivery_address
FROM `order` o
WHERE o.user_id = :userId
  AND o.id < :lastId
ORDER BY o.id DESC
LIMIT 10;
```

**핵심**: `id < :lastId` 조건이 이미 정렬된 인덱스의 특정 지점부터 읽기 시작해 항상 10건만 접근. 몇 페이지든 실행 비용이 동일. 단, 특정 페이지로 직접 점프하는 UI에는 적합하지 않음.

---

### 6. 창고별 재고 현황 — 스칼라 서브쿼리 N번 vs GROUP BY JOIN

**상황**: 창고 관리 화면에서 각 창고의 현재 총 재고량 표시. 창고 수가 늘수록 느려짐.

**느린 쿼리 (스칼라 서브쿼리)**

```sql
SELECT w.id,
       w.name,
       (SELECT SUM(s.count)
        FROM stock s
        WHERE s.warehouse_id = w.id) AS total_stock
FROM warehouse w;
```

**문제점**
- 창고가 N개면 서브쿼리가 N번 실행 → N+1 문제와 동일한 구조
- 창고 20개 → stock 테이블 20번 스캔

**개선 쿼리**

```sql
SELECT w.id,
       w.name,
       COALESCE(agg.total_stock, 0) AS total_stock
FROM warehouse w
LEFT JOIN (
    SELECT warehouse_id,
           SUM(count) AS total_stock
    FROM stock
    GROUP BY warehouse_id
) agg ON agg.warehouse_id = w.id
ORDER BY total_stock DESC;
```

**핵심**: stock 테이블을 단 한 번만 읽고 GROUP BY로 창고별 합산 후 JOIN. `Stock` 엔티티의 `idx_warehouse(warehouse_id)` 인덱스가 GROUP BY에서 그대로 활용됨.

---

## Part 3. 인덱스 개선 — 기본

### 7. 복합 인덱스 — order(user_id, status, created_at)

**상황**: 마이페이지에서 특정 유저의 PAID 주문만 최신순으로 조회.

**현재 인덱스 상태**

```java
// Order.java
@Table(indexes = {
    @Index(name = "idx_user", columnList = "user_id")  // 단일 인덱스만 존재
})
```

**문제 쿼리**

```sql
SELECT id, status, created_at, delivery_address
FROM `order`
WHERE user_id = :userId
  AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 10;
```

**현재 실행 흐름**
1. `idx_user`로 `user_id` 조건 행 전부 추출 (수백 건 가능)
2. `status = 'PAID'` 필터링 → 테이블 재접근
3. `created_at DESC` 정렬 → 별도 filesort 발생

**개선**

```java
@Table(indexes = {
    @Index(name = "idx_user",               columnList = "user_id"),
    @Index(name = "idx_user_status_created", columnList = "user_id, status, created_at")
})
```

**핵심**: 컬럼 순서가 결정적. `(user_id, status, created_at)` 순서는 등치(user_id) → 등치(status) → range/정렬(created_at) 순서로 MySQL이 최적으로 처리. 역순이면 range 이후 등치 필터가 인덱스 효과를 잃음.

---

### 8. 커버링 인덱스 — picking(order_id, picking_status)

**상황**: 출고 등록 전 해당 주문의 모든 피킹이 완료됐는지 확인하는 로직 (`OutboundService` 내부).

**현재 인덱스 상태**

```java
// Picking.java
@Table(indexes = {
    @Index(name = "idx_staff", columnList = "staff_id"),
    @Index(name = "idx_order", columnList = "order_id"),  // 단일
    @Index(name = "idx_stock", columnList = "stock_id")
})
```

**문제 쿼리**

```sql
SELECT order_id, picking_status
FROM picking
WHERE order_id = :orderId;
```

**현재 실행 흐름**
1. `idx_order`로 `order_id` 조건의 PK 목록 조회
2. PK마다 테이블로 돌아가서 `picking_status` 컬럼 읽음 → 랜덤 I/O

**개선**

```java
@Table(indexes = {
    @Index(name = "idx_staff",         columnList = "staff_id"),
    @Index(name = "idx_order_status",  columnList = "order_id, picking_status"),  // 교체
    @Index(name = "idx_stock",         columnList = "stock_id")
})
```

**핵심**: `(order_id, picking_status)` 두 컬럼이 인덱스에 모두 포함되어 SELECT 하는 컬럼이 인덱스 안에서 해결됨 (Index Only Scan). EXPLAIN Extra: `Using index`. 주문 처리 흐름에서 매번 호출되는 로직이므로 랜덤 I/O 제거의 누적 효과가 큼.

---

### 9. 카디널리티 함정 — inbound_item 단독 status 인덱스

**상황**: 전체 적재 미완료 건을 모니터링하거나, 특정 입고 건의 미완료 항목을 조회.

**현재 인덱스 상태**

```java
// InboundItem.java
@Table(indexes = {
    @Index(name = "idx_inbound", columnList = "inbound_id"),
    @Index(name = "idx_staff",   columnList = "stacking_staff_id"),
    @Index(name = "idx_item",    columnList = "item_id")
    // inbound_stacking_status 인덱스 없음
})
```

**단독 인덱스를 추가하면 안 되는 이유**

```sql
-- inbound_stacking_status 단독 인덱스를 추가했다고 가정
CREATE INDEX idx_status ON inbound_item (inbound_stacking_status);

SELECT COUNT(*) FROM inbound_item
WHERE inbound_stacking_status = 'PENDING';

-- inbound_stacking_status 는 PENDING / COMPLETED 2가지 값만 존재
-- 카디널리티 = 2 → 전체 행의 약 50%가 'PENDING'
-- MySQL 옵티마이저 판단: "인덱스 타는 비용 > 풀스캔 비용" → 인덱스 무시
-- EXPLAIN key: NULL (인덱스 미사용)
```

**올바른 개선 — 실제 조회 패턴에 맞는 복합 인덱스**

```java
// 실제 조회 패턴: "이 입고 건(inbound_id)에서 아직 PENDING인 항목"
@Table(indexes = {
    @Index(name = "idx_inbound",        columnList = "inbound_id"),
    @Index(name = "idx_inbound_status", columnList = "inbound_id, inbound_stacking_status"),  // 추가
    @Index(name = "idx_staff",          columnList = "stacking_staff_id"),
    @Index(name = "idx_item",           columnList = "item_id")
})
```

```sql
SELECT COUNT(*)
FROM inbound_item
WHERE inbound_id = :inboundId
  AND inbound_stacking_status = 'PENDING';
-- EXPLAIN: type=range, key=idx_inbound_status, Extra="Using index"
```

**핵심**: 카디널리티가 낮은 Enum/상태 컬럼은 단독 인덱스가 옵티마이저에게 무시됨. 항상 선택도가 높은 컬럼(inbound_id)을 앞에 두고 복합으로 구성해야 실제로 동작함.

---

## Part 4. 인덱스 개선 — 복잡한 쿼리

### 10. 출고 지연 SLA 탐지 — 다중 조인 + 날짜 연산

**상황**: 물류팀이 "주문 후 48시간이 지났는데 출고가 완료되지 않은 건"을 실시간 모니터링.

**쿼리**

```sql
SELECT o.id                                              AS order_id,
       o.created_at                                      AS ordered_at,
       u.name                                            AS customer,
       d.delivery_company,
       ob.outbound_status,
       TIMESTAMPDIFF(HOUR, o.created_at, NOW())          AS hours_elapsed
FROM `order` o
JOIN user u      ON u.id = o.user_id
JOIN delivery d  ON d.order_id = o.id
JOIN outbound ob ON ob.delivery_id = d.id
WHERE o.status = 'PAID'
  AND o.created_at < DATE_SUB(NOW(), INTERVAL 2 DAY)
  AND ob.outbound_status != 'COMPLETED'
ORDER BY hours_elapsed DESC;
```

**현재 실행 흐름**

| 테이블 | 현재 상태 |
|--------|-----------|
| `order` | `idx_user(user_id)` 만 존재. `status`, `created_at` 인덱스 없음 → 풀 스캔 |
| `delivery` | `idx_order(orderId)` 로 조인 (ok) |
| `outbound` | `idx_delivery(deliveryId)` 로 조인 후, `outbound_status` 인덱스 없음 → 전체 행 필터 |

**인덱스 추가**

```java
// Order.java
@Table(indexes = {
    @Index(name = "idx_user",              columnList = "user_id"),
    @Index(name = "idx_status_created_at", columnList = "status, created_at")  // 추가
})

// Outbound.java
@Table(indexes = {
    @Index(name = "idx_staff",           columnList = "staff_id"),
    @Index(name = "idx_delivery_status", columnList = "delivery_id, outbound_status")  // 변경
})
```

**개선 후 실행 흐름**

```
order    → idx_status_created_at 로 PAID + 48시간 이전 range scan
           → 10만 건 중 수십 건만 추출
delivery → idx_order 로 조인
outbound → idx_delivery_status(delivery_id, outbound_status) 커버링
           → delivery_id 매칭 + COMPLETED 아닌 것을 인덱스 안에서 필터 (테이블 접근 0)
```

**핵심**: `(status, created_at)` 순서로 등치 후 range. `(delivery_id, outbound_status)` 복합 인덱스가 조인 키와 필터 컬럼을 모두 포함해 outbound 테이블 접근을 완전히 제거.

---

### 11. 피킹 완료율 집계 — GROUP BY + 날짜 범위

**상황**: 창고 관리자가 이번 주 담당자별 피킹 완료율과 처리량을 집계해 주간 성과 보고서 생성.

**쿼리**

```sql
SELECT w.name                                                              AS warehouse,
       st.name                                                             AS staff,
       COUNT(p.id)                                                         AS total,
       SUM(CASE WHEN p.picking_status = 'COMPLETED' THEN 1 ELSE 0 END)    AS completed,
       ROUND(
           SUM(CASE WHEN p.picking_status = 'COMPLETED' THEN 1 ELSE 0 END)
               * 100.0 / COUNT(p.id), 1
       )                                                                   AS completion_rate
FROM picking p
JOIN stock    sk ON sk.id = p.stock_id
JOIN warehouse w ON w.id = sk.warehouse_id
JOIN staff   st  ON st.id = p.staff_id
WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY w.id, st.id
ORDER BY w.name, completion_rate DESC;
```

**현재 실행 흐름**

| 테이블 | 현재 상태 |
|--------|-----------|
| `picking` | `created_at` 인덱스 없음 → 전체 피킹 풀 스캔 후 7일치만 필터 |
| `picking` | `staff_id`, `stock_id` 각각 단독 인덱스 → GROUP BY 최적화 불가 |
| `picking` | `picking_status` 인덱스 밖 → CASE WHEN 집계 시 테이블 재접근 |

**인덱스 추가**

```java
// Picking.java
@Table(indexes = {
    @Index(name = "idx_staff",                columnList = "staff_id"),
    @Index(name = "idx_order",                columnList = "order_id"),
    @Index(name = "idx_stock",                columnList = "stock_id"),
    @Index(name = "idx_created_staff_status", columnList = "created_at, staff_id, picking_status")  // 추가
})
```

**개선 후 실행 흐름**

```
picking → idx_created_staff_status 로 7일 range scan
          → staff_id 가 인덱스에 포함 → GROUP BY 준비 비용 감소
          → picking_status 가 인덱스에 포함 → CASE WHEN 집계 시 테이블 접근 없음
          → EXPLAIN Extra: "Using index" (커버링)
stock / warehouse / staff → 각 인덱스로 조인
```

**핵심**: `(created_at, staff_id, picking_status)` 3컬럼이 인덱스에 함께 있어 집계에 필요한 컬럼들을 인덱스만으로 처리. 주 1회 보고서라도 picking이 수십만 건이면 수초 차이가 발생.

---

### 12. 재고 소진 위험 탐지 — 상관 서브쿼리 + 집계 중첩

**상황**: 재고 잔량은 적은데 최근 7일 피킹 수요가 높은 상품을 우선순위로 추려 발주 목록 생성.

**쿼리**

```sql
SELECT i.id,
       i.name,
       SUM(s.count)                                      AS current_stock,
       recent.pick_demand,
       ROUND(SUM(s.count) / recent.pick_demand, 1)       AS days_remaining
FROM stock s
JOIN item i ON i.id = s.item_id
JOIN (
    -- 최근 7일간 아이템별 완료된 피킹 수량 집계 (서브쿼리)
    SELECT sk.item_id,
           SUM(p.count) AS pick_demand
    FROM picking p
    JOIN stock sk ON sk.id = p.stock_id
    WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
      AND p.picking_status = 'COMPLETED'
    GROUP BY sk.item_id
) recent ON recent.item_id = i.id
WHERE i.status = 'ACTIVE'
GROUP BY i.id
HAVING current_stock < 100
   AND days_remaining < 3
ORDER BY days_remaining ASC;
```

**현재 실행 흐름**

| 구간 | 현재 상태 |
|------|-----------|
| 서브쿼리 picking | `created_at` 인덱스 없음 → 풀 스캔 후 7일 필터 |
| 서브쿼리 picking | `picking_status` 인덱스 밖 → 테이블 재접근 |
| 서브쿼리 stock | `idx_stock(stock_id)` 조인 후 item_id 확보 |
| 메인 item | `idx_category(category_id)` 만 존재, `status` 인덱스 없음 → 풀 스캔 |

**인덱스 추가**

```java
// Picking.java — 서브쿼리 완전 커버링
@Index(name = "idx_created_status_stock_count",
       columnList = "created_at, picking_status, stock_id, count")
//                   ↑ range     ↑ 필터           ↑ 조인키   ↑ 집계값
// 4컬럼 모두 인덱스 → 서브쿼리 테이블 접근 0

// Item.java — 메인 쿼리 status 필터
@Index(name = "idx_status", columnList = "status")
```

**개선 후 실행 흐름**

```
서브쿼리:
  picking → idx_created_status_stock_count 로 7일 range scan
            → picking_status = 'COMPLETED' 인덱스 안에서 필터
            → stock_id, count 모두 인덱스에 있어 stock 테이블 조인 불필요
            → EXPLAIN Extra: "Using index" (완전 커버링)

메인 쿼리:
  item  → idx_status 로 ACTIVE 필터
  stock → idx_item 으로 조인
```

**핵심**: 서브쿼리의 `(created_at, picking_status, stock_id, count)` 4컬럼 인덱스가 핵심. 쿼리에 필요한 모든 컬럼이 인덱스 안에 있어 picking 테이블 본체를 전혀 읽지 않음. 발주 담당자가 매일 호출하는 쿼리로 데이터가 쌓일수록 효과가 선형으로 커짐.

---

## Part 5. 인덱스 + JOIN 최적화

### 13. 카테고리별 재고 현황 — JOIN ON 절 복합 인덱스 (기본)

**상황**: 상품 관리 화면에서 카테고리별 활성 상품 수와 총 재고량을 한 화면에 표시.

**느린 쿼리**

```sql
SELECT c.name                        AS category,
       COUNT(DISTINCT i.id)           AS item_count,
       COALESCE(SUM(s.count), 0)      AS total_stock
FROM category c
JOIN item i       ON i.category_id = c.id
LEFT JOIN stock s ON s.item_id = i.id
WHERE i.status = 'ACTIVE'
GROUP BY c.id
ORDER BY total_stock DESC;
```

**현재 인덱스 상태**

```java
// Item.java
@Table(indexes = {
    @Index(name = "idx_category", columnList = "category_id")  // 단일만 존재
})
```

**현재 실행 흐름**

```
category → item (idx_category로 category_id 탐색)
         → 테이블 재접근: status 컬럼 읽기 (랜덤 I/O)
         → 비활성 상품도 가져온 뒤 WHERE에서 버림
         → stock LEFT JOIN (비활성 상품 포함, 버려지는 조인 발생)
```

**개선**

```java
// Item.java
@Table(indexes = {
    @Index(name = "idx_category",        columnList = "category_id"),
    @Index(name = "idx_category_status", columnList = "category_id, status")  // 추가
})
```

```sql
-- WHERE → ON 절 이동으로 조인 대상 자체를 줄임
SELECT c.name,
       COUNT(DISTINCT i.id),
       COALESCE(SUM(s.count), 0)
FROM category c
JOIN item i       ON i.category_id = c.id AND i.status = 'ACTIVE'
LEFT JOIN stock s ON s.item_id = i.id
GROUP BY c.id
ORDER BY 3 DESC;
```

**개선 후 실행 흐름**

```
category → item (idx_category_status: category_id 등치 + status 등치 동시 처리)
         → 테이블 재접근 없음 (두 컬럼 모두 인덱스에 포함)
         → 비활성 상품은 조인 자체가 발생하지 않음
         → stock LEFT JOIN: ACTIVE 상품만 대상
```

**핵심**: JOIN ON 절의 필터 조건도 WHERE와 동일하게 인덱스를 탄다. `(category_id, status)` 로 카테고리 필터와 상태 필터를 인덱스 한 번에 처리해, 비활성 상품과의 stock 조인이 아예 발생하지 않음. WHERE에서 ON으로 이동은 인덱스 없이는 효과가 없고, 인덱스가 있어야 의미를 가짐.

---

### 14. 담당자별 출고 현황 — JOIN 체인 드라이빙 테이블 커버링 (기본)

**상황**: 물류팀 관리자가 이번 달 직원별 출고 처리 건수와 평균 처리 시간을 조회하는 월간 리포트.

**쿼리**

```sql
SELECT st.name                                                               AS staff,
       d.delivery_company,
       COUNT(ob.id)                                                          AS outbound_count,
       ROUND(AVG(TIMESTAMPDIFF(HOUR, o.created_at, ob.created_at)), 1)      AS avg_process_hours
FROM outbound ob
JOIN staff    st ON st.id = ob.staff_id
JOIN delivery d  ON d.id  = ob.delivery_id
JOIN `order`  o  ON o.id  = d.order_id
WHERE ob.created_at    >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND ob.outbound_status = 'COMPLETED'
GROUP BY st.id, d.delivery_company
ORDER BY outbound_count DESC;
```

**현재 인덱스 상태**

```java
// Outbound.java
@Table(indexes = {
    @Index(name = "idx_staff",    columnList = "staff_id"),
    @Index(name = "idx_delivery", columnList = "delivery_id")
    // outbound_status, created_at 인덱스 없음
})
```

**현재 실행 흐름**

| 테이블 | 상태 |
|--------|------|
| `outbound` | `created_at` 인덱스 없음 → 풀 스캔 후 30일 필터 |
| `outbound` | `outbound_status` 인덱스 없음 → 테이블 재접근 후 필터 |
| `outbound` | `staff_id`, `delivery_id` 는 각각 단독 인덱스 존재 → 집계 시 테이블 재접근 필요 |
| `delivery` | `idx_order(orderId)` → order 조인 ok |

**인덱스 추가**

```java
// Outbound.java
@Table(indexes = {
    @Index(name = "idx_staff",                   columnList = "staff_id"),
    @Index(name = "idx_created_status_covering", columnList = "outbound_status, created_at, staff_id, delivery_id")  // 추가
})
```

**개선 후 실행 흐름**

```
outbound → idx_created_status_covering
           (outbound_status = 'COMPLETED') 등치 → (created_at >= 30일전) range scan
           → staff_id, delivery_id 인덱스에 포함 → 테이블 본체 접근 없음
           → EXPLAIN Extra: "Using index"

staff    → PK 조인 (tiny table, 영향 없음)
delivery → idx_order(orderId)로 order 조인 ok
order    → PK 조인
```

**핵심**: JOIN 체인의 시작점(드라이빙 테이블)인 outbound 를 `(outbound_status, created_at, staff_id, delivery_id)` 커버링 인덱스로 최적화하면, 필터 + 이후 조인에 필요한 키 두 개(staff_id, delivery_id)를 테이블 접근 없이 공급한다. 드라이빙 테이블의 인덱스가 전체 조인 체인 성능을 결정짓는 이유.

---

### 15. 상품별 입고-출고 비교 — 서브쿼리 4중 JOIN 커버링 (복잡)

**상황**: 월간 운영 리포트 — 상품별 이번 달 입고량 vs 피킹(출고 준비)량을 비교해 재고 회전율을 분석. 두 개의 집계 서브쿼리를 item 기준으로 합산.

**쿼리**

```sql
SELECT i.name                         AS item,
       COALESCE(inb.total_inbound, 0) AS total_inbound,
       COALESCE(pk.total_picking, 0)  AS total_picking,
       ROUND(
           COALESCE(pk.total_picking, 0) * 100.0
           / NULLIF(COALESCE(inb.total_inbound, 0), 0), 1
       )                              AS turnover_rate
FROM item i
LEFT JOIN (
    -- ① 이번 달 입고 완료된 수량 (inbound → inbound_item)
    SELECT ii.item_id,
           SUM(ii.count) AS total_inbound
    FROM inbound ib
    JOIN inbound_item ii ON ii.inbound_id = ib.id
    WHERE ib.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
      AND ii.inbound_stacking_status = 'COMPLETED'
    GROUP BY ii.item_id
) inb ON inb.item_id = i.id
LEFT JOIN (
    -- ② 이번 달 피킹 완료된 수량 (picking → stock)
    SELECT sk.item_id,
           SUM(p.count) AS total_picking
    FROM picking p
    JOIN stock sk ON sk.id = p.stock_id
    WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
      AND p.picking_status = 'COMPLETED'
    GROUP BY sk.item_id
) pk ON pk.item_id = i.id
WHERE i.status = 'ACTIVE'
ORDER BY turnover_rate DESC;
```

**현재 문제점**

| 서브쿼리 | 테이블 | 문제 |
|---------|--------|------|
| ① | `inbound` | `created_at` 인덱스 없음 → 풀 스캔 후 날짜 필터 |
| ① | `inbound_item` | `inbound_stacking_status` 단독 인덱스는 카디널리티 낮아 무시됨 (예시 9 참고) |
| ① | `inbound_item` | `item_id`, `count` 집계 위해 테이블 재접근 |
| ② | `picking` | `created_at`, `picking_status` 인덱스 없음 → 풀 스캔 |
| ② | `picking` | `count` 집계 위해 테이블 재접근 |
| 메인 | `item` | `status` 인덱스 없음 → 풀 스캔 후 ACTIVE 필터 |

**인덱스 추가**

```java
// Inbound.java
@Index(name = "idx_created_at", columnList = "created_at")

// InboundItem.java
// 기존: idx_inbound(inbound_id), idx_staff, idx_item
@Index(name = "idx_inbound_status_item_count",
       columnList = "inbound_id, inbound_stacking_status, item_id, count")
//                   ↑ 조인키      ↑ 필터(카디널리티↑로 동작)  ↑ GROUP BY  ↑ 집계값

// Picking.java (예시 12 인덱스 재사용)
@Index(name = "idx_created_status_stock_count",
       columnList = "created_at, picking_status, stock_id, count")
//                   ↑ range      ↑ 필터             ↑ 조인키   ↑ 집계값

// Item.java
@Index(name = "idx_status", columnList = "status")
```

**개선 후 실행 흐름**

```
서브쿼리 ①:
  inbound      → idx_created_at 로 30일 range scan → PK 목록
  inbound_item → idx_inbound_status_item_count
                 inbound_id (조인키) 등치 필터 → inbound_stacking_status 인덱스 내 필터
                 item_id, count 인덱스에 포함 → 테이블 본체 접근 0
                 EXPLAIN Extra: "Using index"

서브쿼리 ②:
  picking → idx_created_status_stock_count
            created_at range scan → picking_status 인덱스 내 필터
            stock_id(조인키), count 인덱스에 포함 → stock 테이블 조인 없이 GROUP BY 가능
            EXPLAIN Extra: "Using index"

메인 쿼리:
  item → idx_status 로 ACTIVE 필터
  두 서브쿼리 결과와 item_id 기준 LEFT JOIN (인메모리 해시 조인)
```

**핵심**: 두 서브쿼리가 독립적으로 실행된 뒤 item 에 붙는 구조에서, 각 서브쿼리의 인덱스 설계 목표는 동일하다 — **조인 키 + 필터 컬럼 + 집계 컬럼을 하나의 인덱스에 모두 포함**시켜 서브쿼리 내 테이블 본체 접근을 0으로 만드는 것. 서브쿼리가 많을수록 각각의 커버링 인덱스 설계가 독립적으로 적용되므로 효과가 배가됨.

---

## 전체 요약

| # | 분류 | 시나리오 | 핵심 개선 포인트 |
|---|------|---------|----------------|
| 1 | 쿼리 구조 | 일별 매출 조회 | DATE() 함수 제거 + `(status, created_at)` 복합 인덱스 |
| 2 | 쿼리 구조 | 재고 부족 알림 | 서브쿼리로 비활성 상품 사전 필터링 |
| 3 | 쿼리 구조 | 피킹 완료 현황 | HAVING → EXISTS 사전 필터로 집계 대상 축소 |
| 4 | 집계 방식 | 인기 상품 순위 | order_item 직접 집계 → item_sales_count 활용 |
| 5 | 집계 방식 | 주문 이력 페이지 | OFFSET → Cursor(id <) 방식 전환 |
| 6 | 집계 방식 | 창고별 재고 현황 | 스칼라 서브쿼리 N번 → GROUP BY + LEFT JOIN 1번 |
| 7 | 인덱스 기본 | 유저 주문 조회 | `(user_id, status, created_at)` 복합 인덱스, 컬럼 순서 |
| 8 | 인덱스 기본 | 피킹 상태 체크 | `(order_id, picking_status)` 커버링 인덱스 |
| 9 | 인덱스 기본 | 적재 상태 조회 | 낮은 카디널리티 단독 인덱스 → 복합 인덱스 |
| 10 | 인덱스 복잡 | 출고 지연 SLA 탐지 | `(status, created_at)` + `(delivery_id, outbound_status)` |
| 11 | 인덱스 복잡 | 피킹 완료율 집계 | `(created_at, staff_id, picking_status)` 커버링 |
| 12 | 인덱스 복잡 | 재고 소진 위험 탐지 | `(created_at, picking_status, stock_id, count)` 완전 커버링 |
| 13 | 인덱스+JOIN | 카테고리별 재고 현황 | `(category_id, status)` 복합 인덱스로 JOIN ON 절 필터 + 비활성 조인 제거 |
| 14 | 인덱스+JOIN | 담당자별 출고 현황 | `(outbound_status, created_at, staff_id, delivery_id)` 드라이빙 테이블 커버링 |
| 15 | 인덱스+JOIN | 상품별 입고-출고 비교 | 두 서브쿼리 각각 커버링 인덱스 (조인키+필터+집계 컬럼 일체화) |
