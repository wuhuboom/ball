ALTER TABLE `ball_logger_back`
ADD COLUMN `player_name`  varchar(255) NULL AFTER `account_type`,
ADD COLUMN `super_tree`  text NULL AFTER `player_name`;

ALTER TABLE `ball_logger_back`
ADD COLUMN `order_no`  bigint NULL DEFAULT 0 AFTER `account_type`;

