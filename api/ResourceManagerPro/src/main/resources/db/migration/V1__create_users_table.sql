CREATE TABLE users (
    id            CHAR(36)     NOT NULL,
    org_id        CHAR(36)     NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    active        BIT          NOT NULL,

    -- BaseJpaEntity columns
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    version       BIGINT       NOT NULL,
    sync_status   VARCHAR(20)  NOT NULL,
    deleted       BIT          NOT NULL,

    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Lookup index for email (uniqueness among non-deleted rows is enforced in the adapter,
-- so no DB unique constraint — soft-deleted emails may be reused).
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_org_id ON users (org_id);
