ALTER TABLE guest_gifts
 ADD COLUMN guest_name VARCHAR(120), ADD COLUMN guest_email VARCHAR(254),
 ADD COLUMN guest_mobile VARCHAR(25), ADD COLUMN contribution_paise BIGINT CHECK(contribution_paise > 0),
 ADD COLUMN delivery_status VARCHAR(40) NOT NULL DEFAULT 'PLANNED'
 CHECK(delivery_status IN ('PLANNED','GIFT_PURCHASED','TRACKING_SHARED','COMPLETED')),
 ADD COLUMN tracking_details VARCHAR(1000);
ALTER TABLE gift_groups
 ADD COLUMN gift_published_at TIMESTAMPTZ,
 ADD COLUMN fulfilment_status VARCHAR(40) NOT NULL DEFAULT 'PLANNED'
 CHECK(fulfilment_status IN ('PLANNED','GIFT_PURCHASED','TRACKING_SHARED','AMOUNT_TRANSFERRED_TO_HOST','COMPLETED')),
 ADD COLUMN tracking_details VARCHAR(1000);
-- Creator receipt acknowledgement does not verify or settle a payment.
ALTER TABLE gift_group_guest_reports ADD COLUMN creator_received_at TIMESTAMPTZ;
ALTER TABLE gift_group_payment_reports ADD COLUMN creator_received_at TIMESTAMPTZ;
CREATE VIEW group_gift_receipts AS
 SELECT id,group_id,amount_paise,state,creator_received_at FROM gift_group_guest_reports
 UNION ALL
 SELECT id,group_id,amount_paise,state,creator_received_at FROM gift_group_payment_reports;
-- A single progress definition avoids counting duplicate reports or unverified transfers.
CREATE VIEW group_gift_progress AS
 SELECT g.id, COALESCE(SUM(t.amount_paise) FILTER(WHERE t.state='VERIFIED' OR (t.state='REPORTED_UNVERIFIED' AND t.creator_received_at IS NOT NULL)),0)::bigint AS collected_paise,
 COALESCE(SUM(t.amount_paise) FILTER(WHERE t.state='REPORTED_UNVERIFIED'),0)::bigint AS reported_paise,
 CASE WHEN g.target_paise IS NULL THEN 0 ELSE
 LEAST(100,COALESCE(SUM(t.amount_paise) FILTER(WHERE t.state='VERIFIED' OR (t.state='REPORTED_UNVERIFIED' AND t.creator_received_at IS NOT NULL)),0)*100.0/g.target_paise) END AS completion_percentage
 FROM gift_groups g LEFT JOIN group_gift_receipts t ON t.group_id=g.id GROUP BY g.id;
