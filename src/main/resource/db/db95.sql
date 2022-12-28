ALTER TABLE `ball_game`
ADD COLUMN `finish_time`  bigint NULL DEFAULT 0 AFTER `game_status_remark`;

ALTER TABLE `ball_system_config`
ADD COLUMN `game_finish_min`  int NULL DEFAULT 140 AFTER `todo_model`;

delete from ball_bank_area where id=4;
delete from ball_bank where area_id=4;