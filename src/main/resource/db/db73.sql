ALTER TABLE `ball_admin`
ADD COLUMN `player_name`  varchar(30) NULL AFTER `updated_at`;

INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('229', '173', '存款优惠', '/ball/todo/recharge', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('230', '173', '存款返佣', '/ball/todo/recharge/back', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('231', '173', '六级返佣', '/ball/todo/bet', '0', '1', '0', '0');
INSERT INTO `ball_menu` (`id`, `parent_id`, `menu_name`, `path`, `is_menu`, `status`, `created_at`, `updated_at`) VALUES ('232', '173', '奖金', '/ball/todo/bonus', '0', '1', '0', '0');

INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '229');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '230');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '231');
INSERT INTO `ball_group_menu` (`role_id`, `auth_id`) VALUES ('1', '232');

delete from ball_menu where id in(174,175,176,199,200,196,197);
delete from ball_menu where id in(193,194,220,221);
delete from ball_group_menu where auth_id in(174,175,176,199,200,196,197);
delete from ball_group_menu where auth_id in(193,194,220,221);

ALTER TABLE `ball_system_config`
ADD COLUMN `todo_model`  tinyint NULL DEFAULT 0 AFTER `close_notice`;

ALTER TABLE `ball_todo`
ADD COLUMN `super_tree`  text NULL AFTER `oper_user`;


