CREATE TABLE `ball_logger_back_recharge` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` bigint(255) NOT NULL,
  `from_id` bigint(20) DEFAULT '0',
  `money` bigint(20) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `account_type` tinyint(4) DEFAULT NULL,
  `order_no` bigint(20) DEFAULT '0',
  `player_name` varchar(30) DEFAULT NULL,
  `super_tree` text,
  `from_name` varchar(30) DEFAULT NULL,
  `money_recharge` bigint(20) DEFAULT NULL,
  `rate` varchar(10) DEFAULT NULL,
  `fixed` varchar(10) DEFAULT NULL,
  `vip_rank` int(11) DEFAULT NULL,
  `remark` varchar(100) DEFAULT '',
  `updated_at` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('220', '67', '充值返佣结算', '/ball/finance/rebate/back', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('221', '220', '结算', '/ball/finance/rebate/back/do', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '220');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '221');

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('222', '54', '结算', '/ball/bets/settle', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '222');

ALTER TABLE `ball_deposit_policy`
ADD COLUMN `week`  tinyint NULL DEFAULT 8 AFTER `pay_id`;

