ALTER TABLE `ball_logger_back`
ADD COLUMN `ymd`  char(10) NULL AFTER `first_username`,
ADD INDEX `ymd_index` (`ymd`) USING BTREE ;

UPDATE ball_logger_back SET ymd = FROM_UNIXTIME(created_at / 1000, '%Y-%m-%d');