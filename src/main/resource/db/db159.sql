CREATE TABLE `ball_sim_currency` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` bigint(20) DEFAULT NULL,
  `sim` varchar(255) DEFAULT NULL,
  `sim_name` varchar(30) DEFAULT NULL,
  `created_at` bigint(20) DEFAULT '0' COMMENT '创建时间',
  `updated_at` bigint(20) DEFAULT '0' COMMENT '更新时间',
  `status_check` tinyint(4) DEFAULT NULL,
  `checker` varchar(30) DEFAULT NULL,
  `check_time` bigint(20) DEFAULT '0',
  `oper_user` varchar(30) DEFAULT NULL,
  `username` varchar(30) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `addr_unique` (`sim`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('240', '67', '会员SIM', '/ball/finance/sim', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('241', '240', '查看', '/ball/finance/sim/info', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('242', '240', '修改', '/ball/finance/sim/edit', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('243', '240', '禁/启用', '/ball/finance/sim/status', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('244', '240', '审核', '/ball/finance/sim/check', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '240');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '241');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '242');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '243');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '244');

ALTER TABLE `ball_withdraw_management`
MODIFY COLUMN `rate`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `language`;

ALTER TABLE `ball_pay_behalf`
MODIFY COLUMN `rate`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `area_id`;

ALTER TABLE `ball_logger_rebate`
MODIFY COLUMN `rate`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `rate_usdt`;

ALTER TABLE `ball_logger_withdrawal`
MODIFY COLUMN `usdt_rate`  varchar(20) NULL DEFAULT 0 AFTER `behalf_no`;

ALTER TABLE `ball_logger_rebate`
MODIFY COLUMN `rate_usdt`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `money_usdt`;

ALTER TABLE `ball_logger_back_recharge`
MODIFY COLUMN `rate`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `money_recharge`;

