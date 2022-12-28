ALTER TABLE `ball_admin`
ADD COLUMN `todo_all`  tinyint NULL DEFAULT 0 AFTER `player_name`;

ALTER TABLE `ball_admin`
MODIFY COLUMN `player_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `updated_at`,
ADD COLUMN `tg_name`  varchar(30) NULL DEFAULT '' AFTER `todo_all`;

ALTER TABLE `ball_api_config`
MODIFY COLUMN `tg_token`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `sms_unhold_message`,
MODIFY COLUMN `tg_chat`  varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `tg_token`,
ADD COLUMN `todo_token`  varchar(100) NULL AFTER `tg_chat`,
ADD COLUMN `todo_chat`  varchar(30) NULL AFTER `todo_token`;

ALTER TABLE `ball_todo`
ADD COLUMN `top_username`  varchar(30) NULL AFTER `super_tree`,
ADD COLUMN `first_username`  varchar(30) NULL AFTER `top_username`;

ALTER TABLE `ball_logger_back`
ADD COLUMN `top_username`  varchar(30) NULL AFTER `super_tree`,
ADD COLUMN `first_username`  varchar(30) NULL AFTER `top_username`;

update ball_admin set todo_all=1 where admin_name='admin';