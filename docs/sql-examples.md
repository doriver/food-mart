# 프로젝트 유용한 SQL 예시 10가지

## 1. 재고 부족 상품 알림 (임계치 이하)

```sql
SELECT i.name, s.count, w.name AS warehouse
FROM stock s
JOIN item i ON s.item_id = i.id
JOIN warehouse w ON s.warehouse_id = w.id
WHERE s.count < 10
  AND i.status = 'ACTIVE'
ORDER BY s.count ASC;
```

> 운영팀이 매일 아침 재고 보충 여부를 판단하기 위해 조회

---

## 2. 주문별 피킹 완료 현황

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

> 출고 등록 전 모든 피킹이 완료됐는지 확인 (`OutboundService.registerOutbound`의 검증 로직과 동일)

---

## 3. 카테고리별 주간 매출

```sql
SELECT c.name AS category,
       SUM(oi.count * oi.price) AS weekly_revenue
FROM order_item oi
JOIN item i ON oi.item_id = i.id
JOIN category c ON i.category_id = c.id
WHERE oi.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY c.id
ORDER BY weekly_revenue DESC;
```

> 마케팅팀이 어느 카테고리에 프로모션을 집중할지 결정

---

## 4. 유저별 총 구매금액 + 주문 횟수 (우수고객 분류)

```sql
SELECT u.id, u.name,
       COUNT(DISTINCT o.id)        AS order_count,
       SUM(oi.count * oi.price)    AS total_spent
FROM `order` o
JOIN user u ON o.user_id = u.id
JOIN order_item oi ON oi.order_id = o.id
WHERE o.status = 'PAID'
GROUP BY u.id
ORDER BY total_spent DESC
LIMIT 20;
```

> VIP 고객 선정, 등급제 도입 검토

---

## 5. 입고 후 적재 미완료 아이템 목록

```sql
SELECT ib.id AS inbound_id, ib.supplier,
       i.name AS item_name, ii.count,
       ii.created_at AS inbound_time
FROM inbound_item ii
JOIN inbound ib ON ii.inbound_id = ib.id
JOIN item i ON ii.item_id = i.id
WHERE ii.inbound_stacking_status != 'COMPLETED'
ORDER BY ii.created_at;
```

> 창고 담당자가 적재 대기 중인 입고품을 확인

---

## 6. 상품별 재고 총합 (창고 통합)

```sql
SELECT i.id, i.name,
       SUM(s.count) AS total_stock,
       GROUP_CONCAT(w.name ORDER BY w.name SEPARATOR ', ') AS warehouses
FROM stock s
JOIN item i ON s.item_id = i.id
JOIN warehouse w ON s.warehouse_id = w.id
GROUP BY i.id
ORDER BY total_stock;
```

> 상품 상세 페이지에서 전체 가용 재고 표시, 또는 SOLDOUT 전환 기준 판단

---

## 7. 배송사별 출고 건수 (이번 달)

```sql
SELECT d.delivery_company,
       COUNT(*) AS outbound_count
FROM outbound ob
JOIN delivery d ON ob.delivery_id = d.id
WHERE ob.created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')
GROUP BY d.delivery_company
ORDER BY outbound_count DESC;
```

> 물류팀이 배송사별 계약 협상 시 물량 근거 데이터로 활용

---

## 8. 지갑 잔액 마이너스 발생 이력 감지

```sql
SELECT slh.wallet_id, slh.amount, slh.type, slh.created_at,
       w.balance AS current_balance
FROM shop_ledger_history slh
JOIN wallet w ON slh.wallet_id = w.id
WHERE slh.type = 'PAY'
  AND w.balance < 0
ORDER BY slh.created_at DESC;
```

> 결제 처리 버그 또는 동시성 문제로 잔액이 음수가 된 케이스 감사(audit)

---

## 9. 특정 주문의 전체 흐름 추적 (주문 → 피킹 → 출고 → 배송)

```sql
SELECT o.id          AS order_id,
       o.status      AS order_status,
       p.picking_status,
       ob.outbound_status,
       d.delivery_company,
       d.tracking_code
FROM `order` o
LEFT JOIN picking p  ON p.order_id = o.id
LEFT JOIN delivery d ON d.order_id = o.id
LEFT JOIN outbound ob ON ob.delivery_id = d.id
WHERE o.id = :orderId;
```

> CS팀이 고객 문의 시 주문 진행 상태를 한 쿼리로 파악

---

## 10. 지난 30일 일별 주문 건수 + 매출 추이

```sql
SELECT DATE(o.created_at)          AS date,
       COUNT(DISTINCT o.id)        AS order_count,
       SUM(oi.count * oi.price)    AS daily_revenue
FROM `order` o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND o.status = 'PAID'
GROUP BY DATE(o.created_at)
ORDER BY date;
```

> 대시보드 차트 데이터, 이상 매출 급락 날짜 탐지
