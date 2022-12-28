INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('234', '11', '补充值', '/ball/player/repair_re', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('235', '11', '补提现', '/ball/player/repair_wi', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '234');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '235');
