-- DB 스키마. 01-schema.sql

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ProductStock
CREATE TABLE product_stock (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id BIGINT NOT NULL,
                               size INT NOT NULL,
                               stock_quantity INT NOT NULL,
                               updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Balance
CREATE TABLE balance (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         amount BIGINT NOT NULL,
                         created_at DATETIME NOT NULL,
                         updated_at DATETIME NOT NULL,
                         version BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- CouponIssue (FK 제거됨)
CREATE TABLE coupon_issue (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              coupon_id BIGINT NOT NULL,
                              issued_at DATETIME NOT NULL,
                              is_used BOOLEAN NOT NULL
    -- CONSTRAINT fk_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(id) 제거
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- Orders
CREATE TABLE orders (
                        id VARCHAR(64) PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        total_amount BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL, -- ENUM: CREATED, CONFIRMED, CANCELLED
                        created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- OrderItem
CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL,
                            size INT NOT NULL,
                            price BIGINT NOT NULL,
                            order_id VARCHAR(64) -- FK 제거됨
    --  CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- OrderHistory
CREATE TABLE order_history (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id VARCHAR(64) NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               memo TEXT,
                               changed_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Payment
CREATE TABLE payment (
                         id VARCHAR(64) PRIMARY KEY,
                         order_id VARCHAR(64) NOT NULL,
                         amount BIGINT NOT NULL,
                         status VARCHAR(20) NOT NULL, -- ENUM: SUCCESS, FAILURE
                         method VARCHAR(50) NOT NULL,
                         created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- OrderEvent (Outbox)
CREATE TABLE order_event (
                             id BINARY(16) PRIMARY KEY,
                             aggregate_type VARCHAR(50) NOT NULL,
                             event_type VARCHAR(50) NOT NULL,
                             payload LONGTEXT NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- BalanceHistory
CREATE TABLE balance_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 request_id VARCHAR(64) NOT NULL UNIQUE,
                                 amount BIGINT NOT NULL,
                                 type VARCHAR(20) NOT NULL, -- ENUM: CHARGE, DEDUCT
                                 reason TEXT,
                                 created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ProductStatistics
CREATE TABLE product_statistics (
                                    product_id BIGINT NOT NULL,
                                    stat_date DATE NOT NULL,
                                    sales_count INT NOT NULL,
                                    sales_amount BIGINT NOT NULL,
                                    PRIMARY KEY (product_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
ALTER TABLE product_stock ADD CONSTRAINT uq_product_size UNIQUE (product_id, size);
ALTER DATABASE hhplus
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;
