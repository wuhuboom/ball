ALTER TABLE `ball_player`
ADD COLUMN `proxy_player`  tinyint NULL DEFAULT 0 AFTER `frozen_withdrawal`;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('236', '39', '代理报表', '/ball/report/proxy2', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '236');
