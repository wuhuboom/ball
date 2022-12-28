ALTER TABLE `ball_api_config`
ADD COLUMN `auto_send`  tinyint NULL DEFAULT 0 COMMENT '0否1是' AFTER `first_recharge2`,
ADD COLUMN `min_max`  varchar(20) NULL DEFAULT '5000-10000' AFTER `auto_send`,
ADD COLUMN `hour_per`  int NULL DEFAULT 20 AFTER `min_max`;

ALTER TABLE `ball_api_config`
ADD COLUMN `type_send`  tinyint NULL DEFAULT 0 COMMENT '0随机' AFTER `hour_per`;

