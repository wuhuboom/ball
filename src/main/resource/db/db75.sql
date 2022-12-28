ALTER TABLE `ball_system_config`
ADD COLUMN `player_bet_min`  int NULL DEFAULT 0 AFTER `player_bet_max`;

ALTER TABLE `ball_player`
MODIFY COLUMN `the_new_ip`  varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最新的一次登录ip' AFTER `balance`,
MODIFY COLUMN `the_last_ip`  varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上一次登录的ip' AFTER `cumulative_reflect`;

ALTER TABLE `ball_player`
MODIFY COLUMN `phone`  varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码' AFTER `email`;

ALTER TABLE `ball_logger_rebate`
ADD COLUMN `from_name`  varchar(30) NULL AFTER `first_username`;

ALTER TABLE `ball_bank_card`
ADD COLUMN `bank_id`  bigint NULL AFTER `card_number`;

