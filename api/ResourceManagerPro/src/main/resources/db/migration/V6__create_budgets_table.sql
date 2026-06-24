CREATE TABLE budgets (
    id               CHAR(36)      NOT NULL,
    project_id       CHAR(36)      NOT NULL,
    currency         VARCHAR(3)    NOT NULL,
    total_amount     DECIMAL(19,4) NOT NULL,
    allocated_amount DECIMAL(19,4) NOT NULL,
    spent_amount     DECIMAL(19,4) NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_budgets_project_id UNIQUE (project_id),
    CONSTRAINT fk_budgets_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
