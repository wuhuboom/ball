ALTER TABLE `ball_payment_management`
ADD COLUMN `white_ip`  varchar(255) NULL AFTER `sort`;

ALTER TABLE `ball_pay_behalf`
ADD COLUMN `white_ip`  varchar(255) NULL AFTER `status`;

ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `behalf_id`  bigint NULL AFTER `remark_fail`;

