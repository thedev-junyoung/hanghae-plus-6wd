SET NAMES utf8mb4;



-- Products
INSERT INTO product (id, name, brand, price, release_date, image_url, description, created_at, updated_at)
VALUES
    (1, 'New Balance 993', 'New Balance', 199000, '2025-04-14', 'http://example.com/nb993.jpg', '미국산 프리미엄 쿠셔닝', NOW(), NOW()),
    (2, 'ASICS GEL-Kayano 14', 'ASICS', 169000, '2025-04-14', 'http://example.com/gelkayano14.jpg', '복각 러닝 슈즈의 정석', NOW(), NOW()),
    (3, 'New Balance 530', 'New Balance', 129000, '2025-04-14', 'http://example.com/nb530.jpg', '캐주얼한 데일리 슈즈', NOW(), NOW()),
    (4, 'Nike Daybreak', 'Nike', 109000, '2025-04-14', 'http://example.com/daybreak.jpg', '빈티지 감성 러닝화', NOW(), NOW()),
    (5, 'Nike Air Force 1', 'Nike', 139000, '2025-04-14', 'http://example.com/airforce1.jpg', '클래식 로우탑', NOW(), NOW()),
    (6, 'Autry Medalist', 'Autry', 185000, '2025-04-14', 'http://example.com/autry.jpg', '빈티지 미국 감성 스니커즈', NOW(), NOW()),
    (7, 'Adidas Samba OG', 'Adidas', 129000, '2025-04-14', 'http://example.com/samba.jpg', '레트로 감성 풋살화', NOW(), NOW()),
    (8, 'Converse Chuck 70', 'Converse', 85000, '2025-04-14', 'http://example.com/chuck70.jpg', '빈티지 캔버스 로우탑', NOW(), NOW()),
    (9, 'Vans Old Skool', 'Vans', 69000, '2025-04-14', 'http://example.com/oldskool.jpg', '스케이트 보드화', NOW(), NOW()),
    (10, 'Reebok Club C 85', 'Reebok', 99000, '2025-04-14', 'http://example.com/clubc85.jpg', '클래식 화이트 스니커즈', NOW(), NOW()),
    (11, 'Hoka One One Bondi 8', 'Hoka One One', 249000, '2025-04-14', 'http://example.com/bondi8.jpg', '최고의 쿠셔닝 러닝화', NOW(), NOW()),
    (12, 'On Cloudstratus', 'On Running', 239000, '2025-07-14', 'http://example.com/cloudstratus.jpg', '스위스 기술의 러닝화', NOW(), NOW());


-- Product Stocks
INSERT INTO product_stock (id, product_id, size, stock_quantity, updated_at)
VALUES
    (1, 1, 270, 50, NOW()),
    (2, 1, 280, 30, NOW()),
    (3, 2, 265, 40, NOW()),
    (4, 2, 275, 60, NOW()),
    (5, 3, 260, 70, NOW()),
    (6, 3, 270, 90, NOW()),
    (7, 4, 270, 55, NOW()),
    (8, 4, 280, 45, NOW()),
    (9, 5, 265, 80, NOW()),
    (10, 5, 275, 60, NOW()),
    (11, 6, 270, 35, NOW()),
    (12, 6, 280, 25, NOW()),
    (13, 11, 270, 100, NOW()),
(14, 12, 230, 50, NOW());
;

-- Balance
INSERT INTO balance (id, user_id, amount, created_at, updated_at)
VALUES
    (1, 100, 500000, NOW(), NOW()),
    (2, 101, 300000, NOW(), NOW());

-- Coupon
INSERT INTO coupon (id, code, type, discount_rate, total_quantity, remaining_quantity, valid_from, valid_until)
VALUES
    (1, 'WELCOME10', 'PERCENTAGE', 10, 100, 98, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    (2, 'FLAT5000', 'FIXED', 5000, 50, 49, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    (3, 'TESTONLY1000', 'FIXED', 1000, 10, 10, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));


-- Coupon Issue
INSERT INTO coupon_issue (id, user_id, coupon_id, issued_at, is_used)
VALUES
    (1, 100, 1, NOW(), false),
    (2, 100, 2, NOW(), false);

-- Orders
INSERT INTO orders (id, user_id, total_amount, status, created_at)
VALUES
    ('order-1', 100, 398000, 'CONFIRMED', NOW()),
    ('order-2', 101, 169000, 'CREATED', NOW());


-- Order Item
INSERT INTO order_item (id, product_id, quantity, size, price, order_id)
VALUES
    (1, 1, 1, 270, 199000, 'order-1'),
    (2, 2, 1, 275, 169000, 'order-1');

-- Payment
INSERT INTO payment (id, order_id, amount, status, method, created_at)
VALUES
    ('pay-1', 'order-1', 398000, 'SUCCESS', 'CARD', NOW());

-- Order Event
INSERT INTO order_event (id, aggregate_type, event_type, payload, status, created_at)
VALUES
    (UUID_TO_BIN(UUID()), 'ORDER', 'PAYMENT_COMPLETED', '{"orderId":"order-1"}', 'PENDING', NOW());

-- Product Statistics
INSERT INTO product_statistics (product_id, stat_date, sales_count, sales_amount)
VALUES
    (1, CURRENT_DATE, 1, 199000),
    (2, CURRENT_DATE, 1, 169000);





-- =========================
-- 대용량 더미 데이터 (100,000건)
-- =========================
DELIMITER $$

CREATE PROCEDURE populate_stats()
BEGIN
  DECLARE i INT DEFAULT 0;
  WHILE i < 100000 DO
BEGIN
      DECLARE pid INT DEFAULT FLOOR(1 + RAND() * 100);
      DECLARE sdate DATE DEFAULT DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 30) DAY);
      DECLARE scount INT DEFAULT FLOOR(1 + RAND() * 50);
      DECLARE samount BIGINT DEFAULT FLOOR(10000 + RAND() * 50000);
      INSERT IGNORE INTO product_statistics (product_id, stat_date, sales_count, sales_amount)
      VALUES (pid, sdate, scount, samount);
      SET i = i + 1;
END;
END WHILE;
END$$

DELIMITER ;

-- ✅ 세미콜론 필요!
CALL populate_stats();





-- Products (1만 개)

-- 13번 ~ 10000번
-- 기존 12개 이후 ID 13부터 시작
INSERT INTO product (id, name, brand, price, release_date, image_url, description, created_at, updated_at)
SELECT
    id,
    CONCAT('상품-', id),
    '브랜드',
    FLOOR(10000 + RAND() * 100000),
    CURDATE(),
    CONCAT('http://example.com/image', id, '.jpg'),
    '대용량 테스트용 상품',
    NOW(),
    NOW()
FROM (
         SELECT @rownum := @rownum + 1 AS id
         FROM information_schema.tables t1,
             information_schema.tables t2,
             (SELECT @rownum := 12) r
             LIMIT 99988
     ) tmp;



-- Product Stock (3만 건: 상품당 평균 3개 사이즈)
INSERT IGNORE INTO product_stock (product_id, size, stock_quantity, updated_at)
SELECT
    p.id,
    s.size,
    FLOOR(10 + RAND() * 100),
    NOW()
FROM product p
         JOIN (
    SELECT 200 AS size UNION ALL SELECT 205 UNION ALL SELECT 210 UNION ALL SELECT 215 UNION ALL
    SELECT 220 UNION ALL SELECT 225 UNION ALL SELECT 230 UNION ALL SELECT 235 UNION ALL
    SELECT 240 UNION ALL SELECT 245 UNION ALL SELECT 250 UNION ALL SELECT 255 UNION ALL
    SELECT 260 UNION ALL SELECT 265 UNION ALL SELECT 270 UNION ALL SELECT 275 UNION ALL
    SELECT 280 UNION ALL SELECT 285 UNION ALL SELECT 290 UNION ALL SELECT 295 UNION ALL
    SELECT 300
) s
WHERE RAND() < 0.2;


-- Orders (10만 건)
INSERT INTO orders (id, user_id, total_amount, status, created_at)
SELECT
    CONCAT('order-', UUID()),
    FLOOR(1 + RAND() * 50000), -- 5만 명 유저
    FLOOR(50000 + RAND() * 200000),
    ELT(FLOOR(1 + RAND() * 3), 'CREATED', 'CONFIRMED', 'CANCELLED'),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
FROM (
         SELECT 1 FROM information_schema.tables t1, information_schema.tables t2 LIMIT 100000
     ) dummy;



-- Order Items (평균 2~3개씩 → 약 25만 개)
INSERT INTO order_item (product_id, quantity, size, price, order_id)
SELECT
    FLOOR(1 + RAND() * 100000),
    FLOOR(1 + RAND() * 3),
    ELT(FLOOR(1 + RAND() * 4), 250, 260, 270, 280),
    FLOOR(10000 + RAND() * 90000),
    o.id
FROM orders o
         JOIN (
    SELECT 1 AS dummy UNION ALL SELECT 2 UNION ALL SELECT 3
) AS repeater;



-- Coupons (500개)
INSERT INTO coupon (code, type, discount_rate, total_quantity, remaining_quantity, valid_from, valid_until)
SELECT
    CONCAT('COUPON-', LPAD(id, 6, '0')),
    'FIXED',
    1000 + (id % 10) * 500,
    100,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY)
FROM (
         SELECT @cid := @cid + 1 AS id
         FROM information_schema.tables t1, information_schema.tables t2, (SELECT @cid := 3) r
             LIMIT 99997
     ) tmp;


-- Coupon Issues (50만 건)
INSERT INTO coupon_issue (user_id, coupon_id, issued_at, is_used)
SELECT
    FLOOR(1 + RAND() * 50000), -- 다양한 유저
    FLOOR(1 + RAND() * 100000),
    NOW(),
    IF(RAND() < 0.7, false, true)
FROM (
         SELECT 1 FROM information_schema.tables t1, information_schema.tables t2 LIMIT 500000
     ) dummy;

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;