
ALTER TABLE users
    ADD COLUMN username VARCHAR(50);

-- Existing accounts may not yet have usernames.
-- They can choose one later through a verified account update.

ALTER TABLE users
    ADD CONSTRAINT chk_users_username_format
        CHECK (
            username IS NULL
                OR username ~ '^[a-z][a-z0-9_]{3,29}$'
    );

CREATE UNIQUE INDEX ux_users_username_normalized
    ON users (lower(username))
    WHERE username IS NOT NULL;

-- Usernames cannot contain @, preventing
-- ambiguity with email-based login.
