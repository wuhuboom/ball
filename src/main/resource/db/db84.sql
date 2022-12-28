INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('233', '143', '首充日志', '/ball/log/first', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '233');

ALTER TABLE `ball_logger_recharge`
ADD COLUMN `phone`  varchar(30) NULL AFTER `first_username`,
ADD COLUMN `country`  varchar(30) NULL AFTER `phone`,
ADD COLUMN `ip`  varchar(130) NULL AFTER `country`;

ALTER TABLE `ball_logger_recharge`
ADD COLUMN `first`  tinyint NULL DEFAULT 0 AFTER `ip`;

