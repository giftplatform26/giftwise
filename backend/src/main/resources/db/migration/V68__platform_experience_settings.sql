-- Presentation only. Module permissions, ownership and payment policy remain independent.
CREATE TABLE platform_experience_settings (
 id SMALLINT PRIMARY KEY CHECK (id=1),
 view_mode VARCHAR(16) NOT NULL DEFAULT 'CLASSIC' CHECK (view_mode IN ('CLASSIC','FOCUSED')),
 changed_by UUID REFERENCES users(id),
 changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO platform_experience_settings(id,view_mode) VALUES (1,'CLASSIC');
