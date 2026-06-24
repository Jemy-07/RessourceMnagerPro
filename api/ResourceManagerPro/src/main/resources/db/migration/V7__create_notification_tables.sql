CREATE TABLE notifications (
    id          CHAR(36)      NOT NULL,
    user_id     CHAR(36)      NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    message     VARCHAR(1000) NOT NULL,
    is_read     BIT           NOT NULL,

    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    version     BIGINT        NOT NULL,
    sync_status VARCHAR(20)   NOT NULL,
    deleted     BIT           NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_notifications_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE device_tokens (
    id          CHAR(36)     NOT NULL,
    user_id     CHAR(36)     NOT NULL,
    fcm_token   VARCHAR(512) NOT NULL,
    platform    VARCHAR(20)  NOT NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    version     BIGINT       NOT NULL,
    sync_status VARCHAR(20)  NOT NULL,
    deleted     BIT          NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_device_tokens_fcm_token UNIQUE (fcm_token),
    INDEX idx_device_tokens_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
