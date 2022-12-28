ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `to_bank`  varchar(100) NULL DEFAULT '' AFTER `ip_addr`,
ADD COLUMN `to_bank_account`  varchar(100) NULL DEFAULT '' AFTER `to_bank`;

