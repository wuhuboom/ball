-- update ball_admin set updated_at = 7;
-- INSERT INTO `ball_admin` (`id`, `admin_name`, `admin_password`, `nickname`, `token`, `google_code`, `status`, `menu_group_id`, `created_at`, `updated_at`) VALUES ('1', 'admin', 'b51c30722c0aaa05c7f12e16a82555ab', 'super_admin', '', NULL, '1', '1', '0', '0');
-- ALTER TABLE `ball_system_config`
-- ADD COLUMN `version`  bigint NULL AFTER `open_white`;
ALTER TABLE `ball_balance_change`
ADD COLUMN `frozen_status`  tinyint NULL DEFAULT 1 AFTER `account_type`;