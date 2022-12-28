ALTER TABLE `ball_pay_behalf`
MODIFY COLUMN `merchant_no`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `callback_path`;

