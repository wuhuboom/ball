ALTER TABLE `ball_api_config`
ADD COLUMN `tg_token`  varchar(50) NULL AFTER `sms_unhold_message`,
ADD COLUMN `tg_chat`  varchar(20) NULL AFTER `tg_token`;

