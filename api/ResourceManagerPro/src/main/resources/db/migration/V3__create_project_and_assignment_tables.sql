CREATE TABLE projects (
    id          CHAR(36)      NOT NULL,
    org_id      CHAR(36)      NOT NULL,
    manager_id  CHAR(36)      NOT NULL,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NULL,
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    status      VARCHAR(20)   NOT NULL,

    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    version     BIGINT        NOT NULL,
    sync_status VARCHAR(20)   NOT NULL,
    deleted     BIT           NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_projects_org_id (org_id),
    INDEX idx_projects_manager_id (manager_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE assignments (
    id             CHAR(36)     NOT NULL,
    project_id     CHAR(36)     NOT NULL,
    resource_id    CHAR(36)     NOT NULL,
    title          VARCHAR(255) NOT NULL,
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    allocation_pct INT          NOT NULL,
    status         VARCHAR(20)  NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_assignments_project_id (project_id),
    INDEX idx_assignments_resource_id (resource_id),
    CONSTRAINT fk_assignments_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_assignments_resource FOREIGN KEY (resource_id) REFERENCES resources (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
