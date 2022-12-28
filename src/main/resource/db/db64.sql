ALTER TABLE `ball_system_config`
ADD COLUMN `re_max`  tinyint NULL default 5 AFTER `switch_no_recharge`,
ADD COLUMN `re_time`  tinyint NULL default 10 AFTER `re_max`;

ALTER TABLE `ball_commission_recharge`
ADD COLUMN `auto_settle_first`  tinyint NULL DEFAULT 0 AFTER `automatic_distribution`;

ALTER TABLE `ball_vip`
ADD COLUMN `balance`  bigint NULL DEFAULT 0 AFTER `level_name`;

ALTER TABLE `ball_player`
ADD COLUMN `vip_level_max`  tinyint NULL AFTER `vip_level`;

update ball_player set vip_level_max = vip_level;

CREATE TABLE `ball_country` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

INSERT INTO `ball_country` (`id`, `name`) VALUES ('1', '印度');
INSERT INTO `ball_country` (`id`, `name`) VALUES ('2', '加纳');

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('223', '9', '国家管理', '/ball/country', '1', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('224', '204', '增加', '/ball/country/add', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('225', '204', '修改', '/ball/country/edit', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('226', '204', '删除', '/ball/country/del', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '223');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '224');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '225');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '226');

ALTER TABLE `ball_payment_management`
ADD COLUMN `country`  varchar(20) NULL AFTER `pay_type`;

ALTER TABLE `ball_system_config`
ADD COLUMN `check_level_time`  char(5) NULL DEFAULT '00:00' AFTER `re_time`;

ALTER TABLE `ball_withdraw_management`
ADD COLUMN `country`  varchar(20) NULL AFTER `name`;

ALTER TABLE `ball_pay_behalf`
ADD COLUMN `country`  varchar(20) NULL AFTER `name`;

