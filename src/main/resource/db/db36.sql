CREATE TABLE `ball_logger_rebate` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` bigint(20) DEFAULT NULL,
  `account_type` tinyint(4) DEFAULT NULL,
  `player_name` varchar(30) DEFAULT NULL,
  `order_no` bigint(20) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL,
  `money` bigint(20) DEFAULT NULL,
  `money_real` bigint(20) DEFAULT NULL,
  `rate` varchar(10) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `created_at` bigint(20) DEFAULT NULL,
  `updated_at` bigint(20) DEFAULT NULL,
  `super_tree` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `ball_deposit_policy`
CHANGE COLUMN `preferential_standard` `min`  int(255) NULL DEFAULT 0 COMMENT '优惠区间开始' AFTER `end_time`,
CHANGE COLUMN `preferential_top` `max`  int(4) NOT NULL DEFAULT 0 COMMENT '优惠区间结束' AFTER `preferential_per`;

ALTER TABLE `ball_deposit_policy`
ADD UNIQUE INDEX `minmaxtype` (`deposit_policy_type`, `min`, `max`) ;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('196', '67', '充值优惠结算', '/ball/finance/rebate/recharge', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('197', '196', '结算', '/ball/finance/rebate/recharge/do', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('198', '59', '禁/启用', '/ball/tactics/inout/status', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('199', '67', '奖金结算', '/ball/finance/bonus', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('200', '199', '结算', '/ball/finance/bonus/do', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '196');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '197');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '198');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '199');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '200');
