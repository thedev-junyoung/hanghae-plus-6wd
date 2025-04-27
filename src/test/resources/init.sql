


CREATE TABLE product (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         brand VARCHAR(255) NOT NULL,
                         price BIGINT NOT NULL,
                         release_date DATE NOT NULL,
                         image_url VARCHAR(500),
                         description VARCHAR(500),
                         created_at DATETIME NOT NULL,
                         updated_at DATETIME NOT NULL
);


CREATE TABLE product_stock (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id BIGINT NOT NULL,
                               size INT NOT NULL,
                               stock_quantity INT NOT NULL,
                               updated_at DATETIME NOT NULL
);


CREATE TABLE balance (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         amount BIGINT NOT NULL,
                         created_at DATETIME NOT NULL,
                         updated_at DATETIME NOT NULL,
                         version BIGINT NOT NULL DEFAULT 0
);


CREATE TABLE coupon (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        code VARCHAR(100) NOT NULL UNIQUE,
                        type VARCHAR(20) NOT NULL,
                        discount_rate INT NOT NULL,
                        total_quantity INT NOT NULL,
                        remaining_quantity INT NOT NULL,
                        valid_from DATETIME NOT NULL,
                        valid_until DATETIME NOT NULL
);


CREATE TABLE coupon_issue (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              coupon_id BIGINT NOT NULL,
                              issued_at DATETIME NOT NULL,
                              is_used BOOLEAN NOT NULL

);

CREATE TABLE orders (
                        id VARCHAR(64) PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        total_amount BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        created_at DATETIME NOT NULL
);


CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            size INT NOT NULL,
                            price BIGINT NOT NULL,
                            order_id VARCHAR(64)

);


CREATE TABLE order_history (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id VARCHAR(64) NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               memo TEXT,
                               changed_at DATETIME NOT NULL
);


CREATE TABLE payment (
                         id VARCHAR(64) PRIMARY KEY,
                         order_id VARCHAR(64) NOT NULL,
                         amount BIGINT NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         method VARCHAR(50) NOT NULL,
                         created_at DATETIME NOT NULL
);


CREATE TABLE order_event (
                             id BINARY(16) PRIMARY KEY,
                             aggregate_type VARCHAR(50) NOT NULL,
                             event_type VARCHAR(50) NOT NULL,
                             payload LONGTEXT NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             created_at DATETIME NOT NULL
);


CREATE TABLE balance_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 request_id VARCHAR(64) NOT NULL UNIQUE,
                                 amount BIGINT NOT NULL,
                                 type VARCHAR(20) NOT NULL,
                                 reason TEXT,
                                 created_at DATETIME NOT NULL
);


CREATE TABLE product_statistics (
                                    product_id BIGINT NOT NULL,
                                    stat_date DATE NOT NULL,
                                    sales_count INT NOT NULL,
                                    sales_amount BIGINT NOT NULL,
                                    PRIMARY KEY (product_id, stat_date)
);



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

INSERT INTO balance (id, user_id, amount, created_at, updated_at)
VALUES
    (1, 100, 500000, NOW(), NOW()),
    (2, 101, 300000, NOW(), NOW());

INSERT INTO coupon (id, code, type, discount_rate, total_quantity, remaining_quantity, valid_from, valid_until)
VALUES
    (1, 'WELCOME10', 'PERCENTAGE', 10, 100, 98, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    (2, 'FLAT5000', 'FIXED', 5000, 50, 49, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    (3, 'TESTONLY1000', 'FIXED', 1000, 10, 10, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));


INSERT INTO coupon_issue (id, user_id, coupon_id, issued_at, is_used)
VALUES
    (1, 100, 1, NOW(), false),
    (2, 100, 2, NOW(), false);

INSERT INTO orders (id, user_id, total_amount, status, created_at)
VALUES
    ('order-1', 100, 398000, 'CONFIRMED', NOW()),
    ('order-2', 101, 169000, 'CREATED', NOW());


INSERT INTO order_item (id, product_id, quantity, size, price, order_id)
VALUES
    (1, 1, 1, 270, 199000, 'order-1'),
    (2, 2, 1, 275, 169000, 'order-1');

INSERT INTO payment (id, order_id, amount, status, method, created_at)
VALUES
    ('pay-1', 'order-1', 398000, 'SUCCESS', 'CARD', NOW());

INSERT INTO order_event (id, aggregate_type, event_type, payload, status, created_at)
VALUES
    (UUID_TO_BIN(UUID()), 'ORDER', 'PAYMENT_COMPLETED', '{"orderId":"order-1"}', 'PENDING', NOW());

INSERT INTO product_statistics (product_id, stat_date, sales_count, sales_amount)
VALUES
    (1, CURRENT_DATE, 1, 199000),
    (2, CURRENT_DATE, 1, 169000);
