CREATE DATABASE IF NOT EXISTS BTH5_REPORT
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE BTH5_REPORT;

CREATE TABLE IF NOT EXISTS product_costs (
    product_id BIGINT PRIMARY KEY,
    unit_cost DECIMAL(15,2) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT chk_product_cost_nonnegative CHECK (unit_cost >= 0)
);

CREATE TABLE IF NOT EXISTS report_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    generated_at DATETIME(6) NOT NULL,
    catalog_products INT NOT NULL,
    total_orders INT NOT NULL,
    completed_orders INT NOT NULL,
    stock_quantity BIGINT NOT NULL,
    sold_quantity BIGINT NOT NULL,
    revenue DECIMAL(19,2) NOT NULL,
    cost DECIMAL(19,2) NOT NULL,
    profit DECIMAL(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS report_product_rows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    in_catalog BOOLEAN NOT NULL,
    stock_quantity INT NULL,
    sold_quantity BIGINT NOT NULL,
    unit_cost DECIMAL(15,2) NULL,
    revenue DECIMAL(19,2) NOT NULL,
    cost DECIMAL(19,2) NOT NULL,
    profit DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_product_report FOREIGN KEY (report_id) REFERENCES report_runs(id) ON DELETE CASCADE,
    INDEX idx_product_report (report_id, product_id)
);

CREATE TABLE IF NOT EXISTS report_order_rows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    order_created_at DATETIME(6) NULL,
    order_value DECIMAL(19,2) NOT NULL,
    counted_as_sale BOOLEAN NOT NULL,
    sold_quantity BIGINT NOT NULL,
    revenue DECIMAL(19,2) NOT NULL,
    cost DECIMAL(19,2) NOT NULL,
    profit DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_order_report FOREIGN KEY (report_id) REFERENCES report_runs(id) ON DELETE CASCADE,
    INDEX idx_order_report (report_id, order_id)
);

CREATE TABLE IF NOT EXISTS report_order_item_rows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_row_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    unit_cost DECIMAL(15,2) NULL,
    order_value DECIMAL(19,2) NOT NULL,
    recognized_revenue DECIMAL(19,2) NOT NULL,
    recognized_cost DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_order_item_report FOREIGN KEY (order_row_id) REFERENCES report_order_rows(id) ON DELETE CASCADE
);
