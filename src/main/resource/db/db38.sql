ALTER TABLE `ball_deposit_policy`
ADD COLUMN `rules`  text NULL AFTER `updated_at`;

ALTER TABLE `ball_deposit_policy`
DROP INDEX `minmaxtype`;


INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('201', '9', 'API管理', '/ball/api_config', '1', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('202', '201', '修改', '/ball/api_config/edit', '0', '1', '0', '0');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '201');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '202');

CREATE TABLE `ball_api_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ball_api_key` varchar(50) DEFAULT NULL,
  `sms_server` varchar(255) DEFAULT NULL,
  `sms_app_key` varchar(255) DEFAULT NULL,
  `sms_secret_key` varchar(255) DEFAULT NULL,
  `sms_message` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;