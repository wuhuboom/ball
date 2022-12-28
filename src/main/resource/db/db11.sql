ALTER TABLE `ball_logger_recharge`
MODIFY COLUMN `money_real`  bigint(20) NULL DEFAULT 0 AFTER `money`,
MODIFY COLUMN `money_sys`  bigint(20) NULL DEFAULT 0 AFTER `money_real`,
MODIFY COLUMN `money_discount`  bigint(20) NULL DEFAULT 0 AFTER `money_sys`;

ALTER TABLE `ball_logger_recharge`
MODIFY COLUMN `account_type`  tinyint(4) NULL DEFAULT 0 AFTER `pay_id`,
ADD COLUMN `top_username`  varchar(30) NULL DEFAULT '' AFTER `account_type`,
ADD COLUMN `first_username`  varchar(30) NULL DEFAULT '' AFTER `top_username`;

ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `top_username`  varchar(30) NULL DEFAULT '' AFTER `usdt_money`,
ADD COLUMN `first_username`  varchar(30) NULL DEFAULT '' AFTER `top_username`;
