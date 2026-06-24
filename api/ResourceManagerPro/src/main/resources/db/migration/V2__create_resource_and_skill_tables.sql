CREATE TABLE skills (
    id          CHAR(36)     NOT NULL,
    org_id      CHAR(36)     NOT NULL,
    name        VARCHAR(255) NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_skills_org_id (org_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE resources (
    id                   CHAR(36)      NOT NULL,
    org_id               CHAR(36)      NOT NULL,
    user_id              CHAR(36)      NULL,
    name                 VARCHAR(255)  NOT NULL,
    type                 VARCHAR(20)   NOT NULL,
    hourly_rate_amount   DECIMAL(19,4) NOT NULL,
    hourly_rate_currency VARCHAR(3)    NOT NULL,
    availability_status  VARCHAR(20)   NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_resources_org_id (org_id),
    INDEX idx_resources_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE resource_skills (
    id          CHAR(36)    NOT NULL,
    resource_id CHAR(36)    NOT NULL,
    skill_id    CHAR(36)    NOT NULL,
    proficiency INT         NOT NULL,

    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    version     BIGINT      NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    deleted     BIT         NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_resource_skills_resource_id (resource_id),
    INDEX idx_resource_skills_skill_id (skill_id),
    CONSTRAINT fk_resource_skills_resource FOREIGN KEY (resource_id) REFERENCES resources (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE time_offs (
    id          CHAR(36)     NOT NULL,
    resource_id CHAR(36)     NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    reason      VARCHAR(500) NULL,
    status      VARCHAR(20)  NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_time_offs_resource_id (resource_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
