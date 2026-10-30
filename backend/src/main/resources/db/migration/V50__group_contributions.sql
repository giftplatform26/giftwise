CREATE TABLE gift_group_accounts (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id),
 username VARCHAR(40) NOT NULL, password_hash VARCHAR(100) NOT NULL,
 failed_attempts INT NOT NULL DEFAULT 0, locked_until TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT gift_group_username_unique UNIQUE(event_id,username),
 CONSTRAINT gift_group_username_lower CHECK(username=lower(username))
);
CREATE TABLE gift_groups (
 id UUID PRIMARY KEY, event_id UUID NOT NULL REFERENCES events(id),
 admin_id UUID NOT NULL UNIQUE REFERENCES gift_group_accounts(id),
 name VARCHAR(120) NOT NULL, creator_name VARCHAR(120) NOT NULL,
 creator_mobile VARCHAR(20) NOT NULL, description VARCHAR(500),
 gift_name VARCHAR(160), gift_description VARCHAR(500), gift_link VARCHAR(1000),
 target_paise BIGINT, status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT gift_group_target_positive CHECK(target_paise IS NULL OR target_paise>0),
 CONSTRAINT gift_group_status_valid CHECK(status IN ('DRAFT','PUBLISHED'))
);
CREATE INDEX gift_group_event_idx ON gift_groups(event_id);
