-- 货币
ALTER TABLE `ball_payment_management`
ADD COLUMN `currency_symbol`  varchar(20) NULL DEFAULT '' AFTER `pay_type`;

-- 存款策略增加支付渠道
ALTER TABLE `ball_deposit_policy`
ADD COLUMN `pay_id`  bigint NULL AFTER `pay_type`;

-- 提现方式汇率 货币
ALTER TABLE `ball_withdraw_management`
ADD COLUMN `rate`  varchar(10) NULL AFTER `language`,
ADD COLUMN `currency_symbol`  varchar(20) NULL AFTER `rate`;

-- 存款策略约束改变
ALTER TABLE `ball_deposit_policy`
DROP INDEX `type_unique` ,
ADD UNIQUE INDEX `type_unique` (`deposit_policy_type`, `pay_id`) USING BTREE ;

-- 充值优惠日志关联充值渠道
ALTER TABLE `ball_logger_rebate`
ADD COLUMN `pay_id`  bigint NULL AFTER `type`;

-- 玩家增加次充标记
ALTER TABLE `ball_player`
ADD COLUMN `second_top_up`  bigint NULL DEFAULT 0 AFTER `first_top_up_time`,
ADD COLUMN `second_top_up_time`  bigint NULL DEFAULT 0 AFTER `second_top_up`;

-- 自动结算充值优惠开关
ALTER TABLE `ball_deposit_policy`
ADD COLUMN `auto_settlement`  tinyint NULL default 0 AFTER `pay_type`;

-- 充值返佣
CREATE TABLE `ball_commission_recharge` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '名称',
  `commission_level` tinyint(4) DEFAULT '0' COMMENT '返佣层级',
  `automatic_distribution` tinyint(4) DEFAULT '0' COMMENT '自动发放 1 自动 2不自动',
  `status` tinyint(20) DEFAULT '1' COMMENT '状态 1开启 2关闭',
  `created_at` bigint(20) DEFAULT '0' COMMENT '创建时间',
  `updated_at` bigint(20) DEFAULT '0' COMMENT '更新时间',
  `oper_user` varchar(30) DEFAULT NULL,
  `rules` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_all` (`commission_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='反佣策略';

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('218', '58', '返佣策略', '/ball/tactics/recharge', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('219', '218', '修改', '/ball/tactics/recharge/edit', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '218');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '219');
