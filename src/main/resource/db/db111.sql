ALTER TABLE `ball_system_config`
ADD COLUMN `statis_time`  tinyint NULL DEFAULT 0 AFTER `game_finish_min`;

ALTER TABLE `ball_vip`
ADD COLUMN `day_reward`  varchar(255) NULL DEFAULT '0' AFTER `updated_at`;

ALTER TABLE `ball_system_config`
ADD COLUMN `vip_reward_time`  char(5) NULL DEFAULT '00:00' AFTER `statis_time`;

