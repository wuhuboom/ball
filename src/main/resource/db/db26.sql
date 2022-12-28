-- 提现失败配置
DROP TABLE IF EXISTS `ball_config_wfail`;
CREATE TABLE `ball_config_wfail` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT '',
  `content` varchar(255) DEFAULT '',
  `status` tinyint(4) DEFAULT 1,
  `created_at` bigint(20) DEFAULT 0,
  `updated_at` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜单
INSERT IGNORE INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('188', '117', '提现失败配置', '/ball/merchant/wfail', '0', '1', '0', '0');
INSERT IGNORE INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('189', '188', '增加', '/ball/merchant/wfail/add', '0', '1', '0', '0');
INSERT IGNORE INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('190', '188', '修改', '/ball/merchant/wfail/edit', '0', '1', '0', '0');
INSERT IGNORE INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('191', '188', '禁/启用', '/ball/merchant/wfail/status', '0', '1', '0', '0');
INSERT IGNORE INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('192', '188', '删除', '/ball/merchant/wfail/del', '0', '1', '0', '0');
-- 权限
INSERT IGNORE INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '188');
INSERT IGNORE INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '189');
INSERT IGNORE INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '190');
INSERT IGNORE INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '191');
INSERT IGNORE INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '192');


