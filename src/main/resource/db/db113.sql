ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `behalf_time`  bigint NULL DEFAULT 0 AFTER `behalf_id`;

