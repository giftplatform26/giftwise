-- Group joining policy: allow link-based auto approval, retain manual approval and restricted invite-only.
DO $$
DECLARE r record;
BEGIN
 FOR r IN SELECT conname FROM pg_constraint WHERE conrelid='gift_groups'::regclass AND contype='c' AND pg_get_constraintdef(oid) ILIKE '%join_policy%' LOOP
  EXECUTE format('ALTER TABLE gift_groups DROP CONSTRAINT %I',r.conname);
 END LOOP;
END $$;
ALTER TABLE gift_groups ADD CONSTRAINT gift_groups_join_policy_check CHECK (join_policy IN ('APPROVAL','AUTO_JOIN','INVITE_ONLY'));
