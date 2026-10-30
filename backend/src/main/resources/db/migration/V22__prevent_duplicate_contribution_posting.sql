-- Batch 4A.13: Idempotent contribution allocation posting

CREATE TABLE contribution_allocation_postings (
    contribution_id UUID PRIMARY KEY
        REFERENCES contributions(id),

    entry_group_id UUID NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);
