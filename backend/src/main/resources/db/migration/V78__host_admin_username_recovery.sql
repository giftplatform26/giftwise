-- Extend the existing reminder flow; retain all previous attempts and limits.
ALTER TABLE username_recovery_attempts DROP CONSTRAINT username_recovery_attempts_account_kind_check;
ALTER TABLE username_recovery_attempts ADD CONSTRAINT username_recovery_attempts_account_kind_check
 CHECK(account_kind IN ('HOST','PLATFORM_ADMIN','GROUP_ADMIN','GROUP_MEMBER'));
