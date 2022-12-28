-- 盈利结算添加赛事ID
ALTER TABLE `ball_logger_back`
ADD COLUMN `game_id`  bigint NULL DEFAULT 0 AFTER `player_id`;

-- 盈利结算菜单
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('193', '67', '盈利返利结算', '/ball/finance/rebate', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('194', '193', '结算', '/ball/finance/rebate/do', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('195', '10', '短信验证码', '/ball/player/sms', '1', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '193');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '194');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '195');

