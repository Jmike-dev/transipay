CREATE TABLE staff_password (
    id              UUID PRIMARY KEY,
    staff_id        UUID NOT NULL UNIQUE REFERENCES staff(id) ON DELETE CASCADE,
    password_hash   VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);