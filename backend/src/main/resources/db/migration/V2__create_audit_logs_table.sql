
CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,

                            actor_id UUID,
                            actor_type VARCHAR(30) NOT NULL,

                            entity_type VARCHAR(50) NOT NULL,
                            entity_id UUID NOT NULL,

                            action VARCHAR(30) NOT NULL,

                            old_values JSONB,
                            new_values JSONB,

                            occurred_at TIMESTAMPTZ NOT NULL
                                DEFAULT CURRENT_TIMESTAMP,

                            request_id VARCHAR(100),

                            CONSTRAINT chk_audit_actor_type
                                CHECK (actor_type IN (
                                                      'CUSTOMER', 'ADMIN', 'SYSTEM', 'GUEST'
                                    )),

                            CONSTRAINT chk_audit_action
                                CHECK (action IN (
                                    'CREATE', 'UPDATE', 'DELETE',
                                    'STATUS_CHANGE'
                                    ))
    );

CREATE INDEX idx_audit_entity
    ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_actor
    ON audit_logs(actor_id);

CREATE INDEX idx_audit_occurred_at
    ON audit_logs(occurred_at DESC);
