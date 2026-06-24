CREATE TABLE requests (
    id             CHAR(36)      NOT NULL,
    requester_id   CHAR(36)      NOT NULL,
    approver_id    CHAR(36)      NULL,
    resource_id    CHAR(36)      NOT NULL,
    project_id     CHAR(36)      NOT NULL,
    title          VARCHAR(255)  NOT NULL,
    start_date     DATE          NOT NULL,
    end_date       DATE          NOT NULL,
    allocation_pct INT           NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    comments       VARCHAR(1000) NULL,
    decided_at     DATETIME(6)   NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_requests_status (status),
    INDEX idx_requests_requester_id (requester_id),
    INDEX idx_requests_resource_id (resource_id),
    INDEX idx_requests_project_id (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
