ALTER TABLE `ball_logger_rebate`
ADD COLUMN `top_username`  varchar(30) NULL DEFAULT '' AFTER `player_name`,
ADD COLUMN `first_username`  varchar(30) NULL DEFAULT '' AFTER `top_username`;

ALTER TABLE `ball_logger_rebate`
ADD COLUMN `fixed`  bigint NULL DEFAULT 0 AFTER `rate`;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('203', '10', '活跃会员查询', '/ball/player/active', '1', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '203');
