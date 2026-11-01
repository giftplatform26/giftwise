-- A selection is private planning; only a verified creator can reserve the wishlist item.
ALTER TABLE gift_groups ADD COLUMN selected_gift_id UUID;
ALTER TABLE gift_groups ADD CONSTRAINT fk_selected_group_gift FOREIGN KEY(selected_gift_id,event_id) REFERENCES gifts(id,event_id);
UPDATE gift_groups SET selected_gift_id=reserved_gift_id;
UPDATE gift_groups g SET reserved_gift_id=NULL FROM gift_group_accounts a
 WHERE g.admin_id=a.id AND a.email_verified_at IS NULL;

CREATE TABLE gift_group_activity (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 group_id UUID NOT NULL REFERENCES gift_groups(id),
 actor_id UUID,
 action VARCHAR(60) NOT NULL,
 details JSONB NOT NULL DEFAULT '{}',
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX gift_group_activity_history ON gift_group_activity(group_id,created_at,id);

ALTER TABLE gift_group_guest_reports ADD COLUMN gift_id_snapshot UUID REFERENCES gifts(id);
ALTER TABLE gift_group_payment_reports ADD COLUMN gift_id_snapshot UUID REFERENCES gifts(id);
UPDATE gift_group_guest_reports r SET gift_id_snapshot=g.selected_gift_id FROM gift_groups g WHERE g.id=r.group_id;
UPDATE gift_group_payment_reports r SET gift_id_snapshot=g.selected_gift_id FROM gift_groups g WHERE g.id=r.group_id;

CREATE FUNCTION record_group_activity() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE gid UUID; creator UUID; label TEXT; info JSONB; before_row JSONB; after_row JSONB;
BEGIN
 after_row=to_jsonb(NEW);
 IF TG_OP='UPDATE' THEN before_row=to_jsonb(OLD); ELSE before_row='{}'; END IF;
 IF TG_TABLE_NAME='gift_group_accounts' THEN
  FOR gid IN SELECT id FROM gift_groups WHERE admin_id=NEW.id LOOP
   INSERT INTO gift_group_activity(group_id,actor_id,action,details)
    VALUES(gid,NEW.id,'CREATOR_ACCOUNT_UPDATED',jsonb_build_object('emailVerified',NEW.email_verified_at IS NOT NULL,
     'mobileVerified',NEW.mobile_verified_at IS NOT NULL,'credentialsChanged',
     (before_row->'password_hash' IS DISTINCT FROM after_row->'password_hash') OR (before_row->'pin_hash' IS DISTINCT FROM after_row->'pin_hash')));
  END LOOP;
  RETURN NEW;
 ELSIF TG_TABLE_NAME='gift_groups' THEN
  gid=NEW.id; creator=NEW.admin_id;
  IF TG_OP='INSERT' THEN label='GROUP_CREATED';
  ELSIF OLD.collection_published_at IS NULL AND NEW.collection_published_at IS NOT NULL THEN label='GROUP_PUBLISHED';
  ELSIF OLD.selected_gift_id IS DISTINCT FROM NEW.selected_gift_id THEN
   IF NEW.selected_gift_id IS NULL THEN label='GIFT_RELEASED'; ELSE label='GIFT_SELECTED'; END IF;
  ELSIF OLD.reserved_gift_id IS DISTINCT FROM NEW.reserved_gift_id THEN label='GIFT_RESERVATION_CHANGED';
  ELSE label='GROUP_SETTINGS_UPDATED'; END IF;
  info=jsonb_build_object('previousGiftId',before_row->'selected_gift_id','giftId',NEW.selected_gift_id,
   'previousReservedGiftId',before_row->'reserved_gift_id','reservedGiftId',NEW.reserved_gift_id,
   'name',NEW.name,'targetPaise',NEW.target_paise,'published',NEW.collection_published_at IS NOT NULL,
   'previous',before_row-'creator_mobile','current',after_row-'creator_mobile');
 ELSE
  gid=NEW.group_id;
  IF TG_TABLE_NAME IN ('gift_group_guest_reports','gift_group_payment_reports') THEN
   creator=COALESCE((after_row->>'creator_admin_id')::uuid,(after_row->>'member_id')::uuid);
   label=CASE WHEN TG_OP='INSERT' THEN 'CONTRIBUTION_REPORTED' ELSE 'CONTRIBUTION_STATUS_CHANGED' END;
   info=jsonb_build_object('recordId',NEW.id,'source',TG_TABLE_NAME,'amountPaise',after_row->'amount_paise',
    'method',after_row->'method','reference',after_row->'transaction_reference','state',after_row->'state',
    'previousState',before_row->'state','giftId',after_row->'gift_id_snapshot');
  ELSIF TG_TABLE_NAME='gift_group_members' THEN
   IF TG_OP='INSERT' THEN creator=NEW.id; ELSE SELECT admin_id INTO creator FROM gift_groups WHERE id=gid; END IF;
   label=CASE WHEN TG_OP='INSERT' THEN 'MEMBER_JOINED' ELSE 'MEMBER_UPDATED' END;
   info=jsonb_build_object('memberId',NEW.id,'previousStatus',before_row->'status','status',NEW.status);
  ELSIF TG_TABLE_NAME='gift_group_payment_destinations' THEN
   SELECT admin_id INTO creator FROM gift_groups WHERE id=gid;
   label='PAYMENT_DESTINATION_UPDATED';
   info=jsonb_build_object('destinationId',NEW.id,'kind',after_row->'kind','status',after_row->'status','enabled',after_row->'enabled');
  ELSE
   SELECT admin_id INTO creator FROM gift_groups WHERE id=gid;
   label='INVITATION_UPDATED'; info=jsonb_build_object('invitationId',NEW.id,'revoked',after_row->'revoked_at');
  END IF;
 END IF;
 INSERT INTO gift_group_activity(group_id,actor_id,action,details) VALUES(gid,creator,label,info);
 RETURN NEW;
END $$;
CREATE TRIGGER group_creator_history AFTER UPDATE ON gift_group_accounts FOR EACH ROW EXECUTE FUNCTION record_group_activity();
CREATE TRIGGER group_history AFTER INSERT OR UPDATE ON gift_groups FOR EACH ROW EXECUTE FUNCTION record_group_activity();
CREATE TRIGGER group_member_history AFTER INSERT OR UPDATE OF status ON gift_group_members FOR EACH ROW EXECUTE FUNCTION record_group_activity();
CREATE TRIGGER group_destination_history AFTER INSERT OR UPDATE ON gift_group_payment_destinations FOR EACH ROW EXECUTE FUNCTION record_group_activity();
CREATE TRIGGER group_invitation_history AFTER INSERT OR UPDATE ON gift_group_invitations FOR EACH ROW EXECUTE FUNCTION record_group_activity();

CREATE FUNCTION snapshot_group_contribution_gift() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 SELECT selected_gift_id INTO NEW.gift_id_snapshot FROM gift_groups WHERE id=NEW.group_id FOR SHARE;
 RETURN NEW;
END $$;
CREATE TRIGGER group_guest_gift_snapshot BEFORE INSERT ON gift_group_guest_reports FOR EACH ROW EXECUTE FUNCTION snapshot_group_contribution_gift();
CREATE TRIGGER group_member_gift_snapshot BEFORE INSERT ON gift_group_payment_reports FOR EACH ROW EXECUTE FUNCTION snapshot_group_contribution_gift();
CREATE TRIGGER group_guest_report_history AFTER INSERT OR UPDATE ON gift_group_guest_reports FOR EACH ROW EXECUTE FUNCTION record_group_activity();
CREATE TRIGGER group_member_report_history AFTER INSERT OR UPDATE ON gift_group_payment_reports FOR EACH ROW EXECUTE FUNCTION record_group_activity();

CREATE VIEW group_collection_transactions AS
 SELECT r.id,r.group_id,r.guest_name,r.guest_contact,r.amount_paise,r.method,r.transaction_reference,r.state,r.created_at,r.gift_id_snapshot,
  CASE WHEN r.creator_admin_id IS NOT NULL THEN 'CREATOR' WHEN r.member_id IS NOT NULL THEN 'MEMBER' ELSE 'GUEST' END AS contributor_type
 FROM gift_group_guest_reports r
 UNION ALL
 SELECT r.id,r.group_id,m.display_name,NULL::varchar,r.amount_paise,r.method,r.transaction_reference,r.state,r.created_at,r.gift_id_snapshot,'MEMBER'::text
 FROM gift_group_payment_reports r JOIN gift_group_members m ON m.id=r.member_id;

INSERT INTO gift_group_activity(group_id,actor_id,action,details)
 SELECT id,admin_id,'HISTORY_STARTED',jsonb_build_object('giftId',selected_gift_id,'reservedGiftId',reserved_gift_id,'published',collection_published_at IS NOT NULL) FROM gift_groups;
CREATE FUNCTION protect_group_activity() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Group activity is append-only'; END $$;
CREATE TRIGGER immutable_group_activity BEFORE UPDATE OR DELETE ON gift_group_activity FOR EACH ROW EXECUTE FUNCTION protect_group_activity();
