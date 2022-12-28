CREATE TABLE `ball_bank_area` (
`id`  bigint UNSIGNED NOT NULL AUTO_INCREMENT ,
`contry`  varchar(100) NULL ,
`status`  tinyint NULL DEFAULT 0 ,
PRIMARY KEY (`id`)
);

ALTER TABLE `ball_bank`
ADD COLUMN `area_id`  bigint NULL AFTER `bank_code`;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('209', '9', '银行管理', '/ball/bank', '1', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('210', '210', '增加地区', '/ball/bank/add', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('211', '210', '修改地区', '/ball/bank/edit', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('212', '210', '删除地区', '/ball/bank/del', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('213', '210', '切换地区', '/ball/bank/status', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('214', '210', '增加银行', '/ball/bank/bank/add', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('215', '210', '修改银行', '/ball/bank/bank/edit', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('216', '210', '删除银行', '/ball/bank/bank/del', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('217', '210', '银行列表', '/ball/bank/bank', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '209');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '210');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '211');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '212');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '213');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '214');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '215');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '216');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '217');
