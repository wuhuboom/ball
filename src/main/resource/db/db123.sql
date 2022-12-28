ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `ip_addr`  varchar(100) NULL DEFAULT '' AFTER `behalf_time`;

