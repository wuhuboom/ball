ALTER TABLE `ball_logger_rebate`
ADD COLUMN `pay_type_onff`  tinyint NULL DEFAULT 0 AFTER `pay_type`,
ADD COLUMN `rate_usdt`  varchar(10) NULL AFTER `money_usdt`;

ALTER TABLE `ball_logger_rebate`
MODIFY COLUMN `fixed`  varchar(10) NULL DEFAULT 0 AFTER `rate`;

CREATE TABLE `ball_timezone` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  `time_id` varchar(100) DEFAULT '1',
  `status` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `ball_timezone`
ADD UNIQUE INDEX `timeid_unique` (`time_id`) ;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('204', '9', '时区管理', '/ball/timezone', '1', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('205', '204', '增加', '/ball/timezone/add', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('206', '204', '修改', '/ball/timezone/edit', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('207', '204', '删除', '/ball/timezone/del', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('208', '204', '设置', '/ball/timezone/status', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '204');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '205');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '206');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '207');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '208');

INSERT INTO `ball_timezone` (`id`, `name`, `time_id`, `status`) VALUES ('1', '印度', 'Asia/Kolkata', '1');
INSERT INTO `ball_timezone` (`id`, `name`, `time_id`, `status`) VALUES ('2', '加纳', 'Africa/Accra', '0');


