CREATE TABLE wx_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    status VARCHAR(32) NOT NULL,
    activated_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_wx_user_openid UNIQUE (openid)
);

CREATE TABLE invitation_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code_hash VARCHAR(100) NOT NULL,
    code_label VARCHAR(80) NOT NULL,
    target_role VARCHAR(32) NOT NULL,
    max_uses INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE invite_attempt (
    openid VARCHAR(128) PRIMARY KEY,
    failed_count INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    icon_url VARCHAR(500) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NULL,
    cover_url VARCHAR(500) NULL,
    virtual_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    stock INT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    recommended BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    search_keywords VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE INDEX idx_product_category_enabled ON product(category_id, enabled);
CREATE INDEX idx_product_name ON product(name);

CREATE TABLE product_option_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    required_selection BOOLEAN NOT NULL DEFAULT FALSE,
    min_select INT NOT NULL DEFAULT 0,
    max_select INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_option_group_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE product_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    option_group_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    extra_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_option_group FOREIGN KEY (option_group_id) REFERENCES product_option_group(id) ON DELETE CASCADE
);

CREATE TABLE customer_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_virtual_price DECIMAL(12, 2) NOT NULL,
    remark VARCHAR(500) NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_customer_order_no UNIQUE (order_no),
    CONSTRAINT uk_customer_order_idempotency UNIQUE (customer_id, idempotency_key),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES wx_user(id)
);

CREATE INDEX idx_order_status_created ON customer_order(status, created_at);
CREATE INDEX idx_order_customer_created ON customer_order(customer_id, created_at);

CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name_snapshot VARCHAR(120) NOT NULL,
    product_image_snapshot VARCHAR(500) NULL,
    option_snapshot VARCHAR(1000) NULL,
    unit_price_snapshot DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order(id) ON DELETE CASCADE
);

CREATE TABLE order_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    old_status VARCHAR(32) NULL,
    new_status VARCHAR(32) NOT NULL,
    message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_event_order FOREIGN KEY (order_id) REFERENCES customer_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_event_operator FOREIGN KEY (operator_id) REFERENCES wx_user(id)
);

CREATE TABLE notification_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_user_id BIGINT NOT NULL,
    receiver_openid VARCHAR(128) NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    payload_json VARCHAR(8000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    last_error VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_outbox_receiver FOREIGN KEY (receiver_user_id) REFERENCES wx_user(id)
);

CREATE INDEX idx_outbox_due ON notification_outbox(status, next_attempt_at);

CREATE TABLE notification_consent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    granted_count INT NOT NULL DEFAULT 0,
    sent_count INT NOT NULL DEFAULT 0,
    last_result VARCHAR(32) NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_consent_user_type UNIQUE (user_id, notification_type),
    CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES wx_user(id)
);

CREATE TABLE file_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    storage_key VARCHAR(255) NOT NULL,
    public_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploader_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_file_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES wx_user(id)
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NULL,
    target_id VARCHAR(80) NULL,
    details VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_audit_operator FOREIGN KEY (operator_id) REFERENCES wx_user(id)
);
