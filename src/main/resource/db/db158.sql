ALTER TABLE `ball_payment_management`
MODIFY COLUMN `merchant_no`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `area_code`,
MODIFY COLUMN `payment_code`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `merchant_no`;

