CREATE TABLE application_interactions (
 id UUID PRIMARY KEY,
 parent_id UUID,
 visitor_key VARCHAR(64) NOT NULL,
 kind VARCHAR(16) NOT NULL CHECK(kind IN ('CLICK','PAGE_VIEW','REQUEST','OPERATION')),
 source VARCHAR(16) NOT NULL CHECK(source IN ('BROWSER','SERVER')),
 actor_id UUID,
 actor_role VARCHAR(16) NOT NULL,
 host_id UUID,
 event_id UUID,
 action VARCHAR(64) NOT NULL,
 page VARCHAR(240) NOT NULL,
 control VARCHAR(160) NOT NULL DEFAULT '',
 destination VARCHAR(240) NOT NULL DEFAULT '',
 outcome VARCHAR(32) NOT NULL,
 detail VARCHAR(120) NOT NULL DEFAULT '',
 http_status INTEGER,
 duration_ms BIGINT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX application_interactions_host_date ON application_interactions(host_id,created_at DESC);
CREATE INDEX application_interactions_event_date ON application_interactions(event_id,created_at DESC);
CREATE INDEX application_interactions_date ON application_interactions(created_at DESC);
CREATE INDEX application_interactions_parent ON application_interactions(parent_id) WHERE parent_id IS NOT NULL;
