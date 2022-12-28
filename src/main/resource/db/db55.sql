ALTER TABLE `ball_system_config`
ADD COLUMN `switch_no_recharge`  tinyint NULL DEFAULT 0 AFTER `switch_rebate_every`;

ALTER TABLE `ball_api_config`
ADD COLUMN `sms_unhold`  tinyint NULL DEFAULT 0 COMMENT '0关1开' AFTER `sms_message`;

ALTER TABLE `ball_api_config`
ADD COLUMN `sms_unhold_message`  varchar(255) NULL AFTER `sms_unhold`;

ALTER TABLE `ball_payment_management`
ADD COLUMN `unhold`  tinyint NULL DEFAULT 0 AFTER `goods_name`,
ADD COLUMN `unhold_message`  varchar(255) NULL AFTER `unhold`;

