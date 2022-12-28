ALTER TABLE `ball_deposit_policy`
ADD COLUMN `pay_type`  tinyint NULL DEFAULT 0 AFTER `deposit_policy_type`,
DROP INDEX `type_unique` ,
ADD UNIQUE INDEX `type_unique` (`deposit_policy_type`, `pay_type`) USING BTREE ;

ALTER TABLE `ball_logger_rebate`
ADD COLUMN `pay_type`  tinyint NULL DEFAULT 0 AFTER `type`,
ADD COLUMN `money_usdt`  bigint NULL DEFAULT 0 AFTER `money_real`;

