-- ============================================================
-- 출고 지연 탐지 쿼리 (findOutboundDelays) 성능 테스트용 더미 데이터
--
-- 데이터 분포:
--   order.status     : REGISTER / WAITDELIVERY / DELIVERY / COMPLETE / CANCEL 순환
--   order.created_at : 1~10일 전 순환 (쿼리 조건 = 2일 초과분만 히트)
--   outbound.status  : READY / COMPLETED / CANCEL 순환
--
-- 쿼리 히트율 (findOutboundDelays 조건 충족 비율):
--   status=WAITDELIVERY  → 5개 중 1개  ≈ 20%
--   created_at > 2일     → 10개 중 8개 ≈ 80%  (3~10일 전)
--   status != COMPLETED  → 3개 중 2개  ≈ 67%
--   예상 히트율          ≈ 10~11%
--
-- 사용 방법:
--   1. MySQL Workbench / DBeaver 등에서 이 파일 전체 실행
--   2. CALL insert_delay_dummy(원하는건수);  -- 기본 10000
--   3. GET /api/outbound/delays 로 결과 확인
--   4. EXPLAIN 으로 실행 계획 확인 (하단 참조)
--   5. 테스트 완료 후 하단 "정리 스크립트" 실행
-- ============================================================

-- ─────────────────────────────────────────
-- 1. staff 기준 데이터 (이미 존재하면 재삽입 방지)
--    users는 프로시저 내부에서 cnt/3명 생성
-- ─────────────────────────────────────────
INSERT INTO staff (nickname, staff_role)
SELECT 'perf_staff', 'STAFF'
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE nickname = 'perf_staff');

-- ─────────────────────────────────────────
-- 2. 저장 프로시저 정의
--    - users         : cnt/3명 (perf_user_0 ~ perf_user_{cnt/3-1})
--    - order.status  : REGISTER/WAITDELIVERY/DELIVERY/COMPLETE/CANCEL 순환
--    - order.created_at : 1~10일 전 순환
--    - outbound.status  : READY/COMPLETED/CANCEL 순환
-- ─────────────────────────────────────────
DROP PROCEDURE IF EXISTS insert_delay_dummy;

DELIMITER $$
CREATE PROCEDURE insert_delay_dummy(IN cnt INT)
BEGIN
    DECLARE i              INT    DEFAULT 0;
    DECLARE user_cnt       INT;
    DECLARE uid            BIGINT;
    DECLARE sid            BIGINT;
    DECLARE oid            BIGINT;
    DECLARE did            BIGINT;
    DECLARE order_status   VARCHAR(20);
    DECLARE days_ago       INT;
    DECLARE ob_status      VARCHAR(20);

    SET user_cnt = GREATEST(1, cnt / 3);
    SET sid = (SELECT id FROM staff WHERE nickname = 'perf_staff' LIMIT 1);

    -- 유저 cnt/3명 생성
    WHILE i < user_cnt DO
        INSERT INTO users (nickname, user_role)
        SELECT CONCAT('perf_user_', i), 'USER'
        WHERE NOT EXISTS (SELECT 1 FROM users WHERE nickname = CONCAT('perf_user_', i));
        SET i = i + 1;
    END WHILE;

    -- 주문 cnt건 삽입
    SET i = 0;
    WHILE i < cnt DO
        SET uid          = (SELECT id FROM users WHERE nickname = CONCAT('perf_user_', i % user_cnt) LIMIT 1);
        SET order_status = ELT(1 + (i % 5), 'REGISTER', 'WAITDELIVERY', 'DELIVERY', 'COMPLETE', 'CANCEL');
        SET days_ago     = 1 + (i % 10);   -- 1~10일 전 순환
        SET ob_status    = ELT(1 + (i % 3), 'READY', 'COMPLETED', 'CANCEL');

        INSERT INTO `order` (user_id, delivery_address, status, created_at, updated_at)
        VALUES (uid, '서울시 성동구 뚝섬로 1길', order_status,
                NOW() - INTERVAL days_ago DAY, NOW() - INTERVAL days_ago DAY);
        SET oid = LAST_INSERT_ID();

        INSERT INTO delivery (order_id, address, delivery_company, tracking_code)
        VALUES (oid, '서울시 성동구 뚝섬로 1길',
                ELT(1 + (i % 2), 'CJ', 'HANJIN'),
                CONCAT('PERF', LPAD(i, 8, '0')));
        SET did = LAST_INSERT_ID();

        INSERT INTO outbound (staff_id, delivery_id, outbound_status)
        VALUES (sid, did, ob_status);

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- ─────────────────────────────────────────
-- 3. 실행 (건수 조정 가능)
-- ─────────────────────────────────────────
CALL insert_delay_dummy(10000);

-- ─────────────────────────────────────────
-- 4. 분포 확인
-- ─────────────────────────────────────────
SELECT status, COUNT(*) AS cnt
FROM `order`
WHERE delivery_address = '서울시 성동구 뚝섬로 1길'
GROUP BY status;

SELECT outbound_status, COUNT(*) AS cnt
FROM outbound ob
    JOIN delivery d  ON ob.delivery_id = d.id
    JOIN `order` o   ON d.order_id = o.id
WHERE o.delivery_address = '서울시 성동구 뚝섬로 1길'
GROUP BY ob.outbound_status;

SELECT COUNT(*) AS query_hit_count
FROM `order` o
    JOIN delivery d  ON d.order_id = o.id
    JOIN outbound ob ON ob.delivery_id = d.id
WHERE o.status = 'WAITDELIVERY'
  AND o.created_at < DATE_SUB(NOW(), INTERVAL 2 DAY)
  AND ob.outbound_status != 'COMPLETED';

-- ─────────────────────────────────────────
-- 5. EXPLAIN — 실행 계획 확인
-- ─────────────────────────────────────────
EXPLAIN
SELECT o.id                                             AS orderId,
       o.created_at                                     AS orderedAt,
       u.nickname                                       AS customer,
       d.delivery_company                               AS deliveryCompany,
       ob.outbound_status                               AS outboundStatus,
       TIMESTAMPDIFF(HOUR, o.created_at, NOW())         AS hoursElapsed
FROM `order` o
         JOIN users u     ON u.id = o.user_id
         JOIN delivery d  ON d.order_id = o.id
         JOIN outbound ob ON ob.delivery_id = d.id
WHERE o.status = 'WAITDELIVERY'
  AND o.created_at < DATE_SUB(NOW(), INTERVAL 2 DAY)
  AND ob.outbound_status != 'COMPLETED'
ORDER BY o.created_at ASC;

-- ============================================================
-- 정리 스크립트 (성능 테스트 완료 후 실행)
-- ============================================================

/*
DELETE ob FROM outbound ob
    JOIN delivery d ON ob.delivery_id = d.id
    JOIN `order` o  ON d.order_id = o.id
    JOIN users u    ON o.user_id = u.id
WHERE u.nickname = 'perf_user';

DELETE d FROM delivery d
    JOIN `order` o ON d.order_id = o.id
    JOIN users u   ON o.user_id = u.id
WHERE u.nickname = 'perf_user';

DELETE o FROM `order` o
    JOIN users u ON o.user_id = u.id
WHERE u.nickname = 'perf_user';

DELETE FROM users WHERE nickname LIKE 'perf_user_%';
DELETE FROM staff WHERE nickname = 'perf_staff';
DROP PROCEDURE IF EXISTS insert_delay_dummy;
*/
