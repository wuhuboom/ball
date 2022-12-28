ALTER TABLE `ball_payment_management`
ADD COLUMN `sort`  tinyint NULL DEFAULT 0 AFTER `unhold_message`;

ALTER TABLE `ball_deposit_policy`
ADD COLUMN `country`  varchar(20) NULL AFTER `pay_type`;

