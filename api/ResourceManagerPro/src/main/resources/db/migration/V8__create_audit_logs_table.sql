CREATE TABLE audit_logs (
    id          CHAR(36)      NOT NULL,
    entity_type VARCHAR(30)   NOT NULL,
    entity_id   CHAR(36)      NOT NULL,
    action      VARCHAR(20)   NOT NULL,
    conflict    BIT           NOT NULL,
    message     VARCHAR(1000) NULL,
    occurred_at DATETIME(6)   NOT NULL,

    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    version     BIGINT        NOT NULL,
    sync_status VARCHAR(20)   NOT NULL,
    deleted     BIT           NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_conflict (conflict)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
