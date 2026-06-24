CREATE TABLE timesheets (
    id            CHAR(36)    NOT NULL,
    resource_id   CHAR(36)    NOT NULL,
    assignment_id CHAR(36)    NOT NULL,
    work_date     DATE        NOT NULL,
    hours         DECIMAL(5,2) NOT NULL,
    status        VARCHAR(20) NOT NULL,

    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    version     BIGINT      NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    deleted     BIT         NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_timesheets_resource_date (resource_id, work_date),
    INDEX idx_timesheets_assignment_id (assignment_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
