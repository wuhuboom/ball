ALTER TABLE `ball_system_config`
ADD COLUMN `auto_check`  tinyint NULL DEFAULT 0 AFTER `bank_list_swtich`,
ADD COLUMN `auto_check_time`  varchar(20) NULL DEFAULT '' AFTER `auto_check`;

