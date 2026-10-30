
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(254) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_users_role
                           CHECK (role IN ('CUSTOMER', 'PLATFORM_ADMIN')),

                       CONSTRAINT chk_users_email_not_blank
                           CHECK (length(trim(email)) > 0),

                       CONSTRAINT chk_users_password_not_blank
                           CHECK (length(trim(password_hash)) > 0)
);

CREATE UNIQUE INDEX ux_users_email_normalized
    ON users (lower(email));
