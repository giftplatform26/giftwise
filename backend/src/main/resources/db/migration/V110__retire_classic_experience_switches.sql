-- GiftWise now has a single (guided "Focused") experience. Guided creation is always on and the
-- Classic-only polished guest view no longer exists, so their admin switches are retired.
-- platform_experience_settings is left in place (unused) so existing audit history is preserved.
DELETE FROM platform_growth_features WHERE code IN ('GUIDED_CREATION','POLISHED_GUEST_VIEW');
