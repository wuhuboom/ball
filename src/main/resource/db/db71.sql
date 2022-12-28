ALTER TABLE `ball_system_config`
ADD COLUMN `close_notice`  tinyint NULL DEFAULT 0 AFTER `check_level_time`;

ALTER TABLE `ball_game`
ADD COLUMN `game_status_remark`  varchar(50) NULL AFTER `settlement_time`;

