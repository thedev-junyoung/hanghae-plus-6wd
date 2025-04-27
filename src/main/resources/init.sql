-- 공통 VO: Money는 long으로 저장됩니다

-- Product
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

-- ProductStock
CREATE TABLE product_stock (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id BIGINT NOT NULL,
                               size INT NOT NULL,
                               stock_quantity INT NOT NULL,
                               updated_at DATETIME NOT NULL
);

-- Balance
CREATE TABLE balance (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         amount BIGINT NOT NULL,
                         created_at DATETIME NOT NULL,
                         updated_at DATETIME NOT NULL
);

-- Coupon
CREATE TABLE coupon (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        code VARCHAR(100) NOT NULL UNIQUE,
                        type VARCHAR(20) NOT NULL, -- ENUM: FIXED, PERCENTAGE
                        discount_rate INT NOT NULL,
                        total_quantity INT NOT NULL,
                        remaining_quantity INT NOT NULL,
                        valid_from DATETIME NOT NULL,
                        valid_until DATETIME NOT NULL
);

-- CouponIssue
CREATE TABLE coupon_issue (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              coupon_id BIGINT NOT NULL,
                              issued_at DATETIME NOT NULL,
                              is_used BOOLEAN NOT NULL,
                              CONSTRAINT fk_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(id)
);

-- Orders
CREATE TABLE orders (
                        id VARCHAR(64) PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        total_amount BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL, -- ENUM: CREATED, CONFIRMED, CANCELLED
                        created_at DATETIME NOT NULL
);

-- OrderItem
CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            size INT NOT NULL,
                            price BIGINT NOT NULL,
                            order_id VARCHAR(64),
                            CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- OrderHistory
CREATE TABLE order_history (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id VARCHAR(64) NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               memo TEXT,
                               changed_at DATETIME NOT NULL
);

-- Payment
CREATE TABLE payment (
                         id VARCHAR(64) PRIMARY KEY,
                         order_id VARCHAR(64) NOT NULL,
                         amount BIGINT NOT NULL,
                         status VARCHAR(20) NOT NULL, -- ENUM: SUCCESS, FAILURE
                         method VARCHAR(50) NOT NULL,
                         created_at DATETIME NOT NULL
);

-- OrderEvent (Outbox)
CREATE TABLE order_event (
                             id BINARY(16) PRIMARY KEY,
                             aggregate_type VARCHAR(50) NOT NULL,
                             event_type VARCHAR(50) NOT NULL,
                             payload TEXT NOT NULL,
                             status VARCHAR(20) NOT NULL, -- ENUM: PENDING, COMPLETED
                             created_at DATETIME NOT NULL
);

-- BalanceHistory
CREATE TABLE balance_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 amount BIGINT NOT NULL,
                                 type VARCHAR(20) NOT NULL, -- ENUM: CHARGE, DEDUCT
                                 reason TEXT,
                                 created_at DATETIME NOT NULL
);

-- ProductStatistics
CREATE TABLE product_statistics (
                                    product_id BIGINT NOT NULL,
                                    stat_date DATE NOT NULL,
                                    sales_count INT NOT NULL,
                                    sales_amount BIGINT NOT NULL,
                                    PRIMARY KEY (product_id, stat_date)
);


-- Users는 별도로 관리한다고 가정
-- Products
INSERT INTO product (id, name, brand, price, release_date, image_url, description, created_at, updated_at)
VALUES
    (1, 'Nike Air Max', 'Nike', 120000, '2025-04-14', 'http://example.com/nike.jpg', '편안한 쿠셔닝', '2025-04-14 13:09:24.187936', '2025-04-14 13:09:24.187936'),
    (2, 'Adidas Ultra Boost', 'Adidas', 150000, '2025-04-14', 'http://example.com/adidas.jpg', '달리기용 최고급 스니커즈', '2025-04-14 13:09:24.187936', '2025-04-14 13:09:24.187936');

-- Product Stocks
INSERT INTO product_stock (id, product_id, size, stock_quantity, updated_at)
VALUES
    (1, 1, 270, 100, '2025-04-14 13:09:24.187936'),
    (2, 1, 280, 50, '2025-04-14 13:09:24.187936'),
    (3, 2, 270, 120, '2025-04-14 13:09:24.187936'),
    (4, 2, 280, 80, '2025-04-14 13:09:24.187936');

-- Balance
INSERT INTO balance (id, user_id, amount, created_at, updated_at)
VALUES
    (1, 100, 500000, '2025-04-14 13:09:24.187936', '2025-04-14 13:09:24.187936');

-- Coupon
INSERT INTO coupon (id, code, type, discount_rate, total_quantity, remaining_quantity, valid_from, valid_until)
VALUES
    (1, 'WELCOME10', 'PERCENTAGE', 10, 100, 100, '2025-04-14 13:09:24.187936', '2025-05-14 13:09:24.187936'),
    (2, 'FLAT5000', 'FIXED', 5000, 50, 50, '2025-04-14 13:09:24.187936', '2025-05-14 13:09:24.187936');

-- Coupon Issue
INSERT INTO coupon_issue (id, user_id, coupon_id, issued_at, is_used)
VALUES
    (1, 100, 1, '2025-04-14 13:09:24.187936', false),
    (2, 100, 2, '2025-04-14 13:09:24.187936', false);

-- Order & OrderItem
INSERT INTO orders (id, user_id, total_amount, status, created_at)
VALUES
    ('order-1', 100, 240000, 'CREATED', '2025-04-14 13:09:24.187936');

INSERT INTO order_item (id, product_id, quantity, size, price, order_id)
VALUES
    (1, 1, 2, 270, 120000, 'order-1');

-- Payment
INSERT INTO payment (id, order_id, amount, status, method, created_at)
VALUES
    ('pay-1', 'order-1', 240000, 'SUCCESS', 'CARD', '2025-04-14 13:09:24.187936');

-- Order Event
INSERT INTO order_event (id, aggregate_type, event_type, payload, status, created_at)
VALUES
    (UUID_TO_BIN(UUID()), 'ORDER', 'PAYMENT_COMPLETED', '{"orderId":"order-1"}', 'PENDING', '2025-04-14 13:09:24.187936');

-- Product Statistics
INSERT INTO product_statistics (product_id, stat_date, sales_count, sales_amount)
VALUES
    (1, '2025-04-14', 2, 240000);
