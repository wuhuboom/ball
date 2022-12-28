ALTER TABLE `ball_system_config`
MODIFY COLUMN `usdt_withdraw_per`  varchar(20) NULL DEFAULT '86' COMMENT 'usdt 提现提现汇率' AFTER `withdrawal_rate_min`;

update ball_system_config set usdt_withdraw_per = usdt_withdraw_per/100;

ALTER TABLE `ball_bank_area`
ADD COLUMN `area_code` text NULL AFTER `code`;

ALTER TABLE `ball_system_config`
ADD COLUMN `euro_rate` text NULL AFTER `sms_interval`;

update ball_bank_area set `status`=1;

ALTER TABLE `ball_system_config`
ADD COLUMN `bank_list_swtich`  tinyint NULL DEFAULT 0 AFTER `euro_rate`;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('239', '118', '汇率配置', '/ball/merchant/param/rate', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '239');

