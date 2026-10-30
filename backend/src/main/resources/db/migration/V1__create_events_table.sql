CREATE TABLE events (
                        id UUID PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        description TEXT,
                        occasion_type VARCHAR(50) NOT NULL,
                        event_date DATE,
                        status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_event_date ON events(event_date);