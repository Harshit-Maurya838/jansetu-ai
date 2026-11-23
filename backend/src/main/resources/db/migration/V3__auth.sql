-- Module 01: Authentication & User Management (docs/modules/01-authentication.md).
-- Extends the V1 auth tables; safe on a fresh DB and on one that already has V1/V2 data.

-- users: account lifecycle + lockout. `active` is replaced by `status`.
ALTER TABLE users
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION'
        COMMENT 'PENDING_ACTIVATION | ACTIVE | LOCKED | DISABLED' AFTER ngo_id,
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN locked_until DATETIME(6) NULL AFTER failed_login_attempts;

UPDATE users SET status = IF(active, 'ACTIVE', 'DISABLED');

ALTER TABLE users
    DROP COLUMN active,
    ALTER COLUMN status DROP DEFAULT,
    ADD KEY ix_users_role_status (role, status);

-- refresh_tokens: rotation chain (family), device label, link to the token that replaced this one.
ALTER TABLE refresh_tokens
    ADD COLUMN family_id CHAR(36) NULL COMMENT 'one rotation chain per login; reuse revokes the family' AFTER user_id,
    ADD COLUMN device_name VARCHAR(100) NULL AFTER device_id,
    ADD COLUMN replaced_by CHAR(36) NULL COMMENT 'id of the token issued when this one was rotated' AFTER revoked_at;

UPDATE refresh_tokens SET family_id = id WHERE family_id IS NULL;

ALTER TABLE refresh_tokens
    MODIFY COLUMN family_id CHAR(36) NOT NULL COMMENT 'one rotation chain per login; reuse revokes the family',
    ADD KEY ix_refresh_tokens_user_device (user_id, device_id),
    ADD KEY ix_refresh_tokens_family (family_id);

-- otp_challenges: OTPs can go to a mobile number or an email address.
ALTER TABLE otp_challenges
    CHANGE COLUMN mobile target VARCHAR(160) NOT NULL COMMENT 'E.164 mobile or lower-cased email',
    ADD COLUMN channel VARCHAR(10) NOT NULL DEFAULT 'SMS' COMMENT 'SMS | EMAIL' AFTER target,
    ADD COLUMN user_id CHAR(36) NULL COMMENT 'null for REGISTER (user does not exist yet)' AFTER channel,
    ADD KEY ix_otp_challenges_target (target, purpose, created_at),
    ADD CONSTRAINT fk_otp_challenges_user_id FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE otp_challenges ALTER COLUMN channel DROP DEFAULT;

-- audit_logs: auth events record the client IP.
ALTER TABLE audit_logs
    ADD COLUMN ip VARCHAR(45) NULL AFTER device_id;

-- supervisor_jurisdictions: centres a GOVT_SUPERVISOR is responsible for (docs/rbac-matrix.md#data-scoping).
CREATE TABLE supervisor_jurisdictions (
    id          CHAR(36)    NOT NULL,
    user_id     CHAR(36)    NOT NULL,
    centre_id   CHAR(36)    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    created_by  CHAR(36)    NULL,
    updated_by  CHAR(36)    NULL,
    version     BIGINT      NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_supervisor_jurisdictions_user_centre (user_id, centre_id),
    CONSTRAINT fk_supervisor_jurisdictions_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_supervisor_jurisdictions_centre_id FOREIGN KEY (centre_id) REFERENCES anganwadi_centres (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
