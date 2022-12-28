ALTER TABLE `ball_bank_area`
ADD COLUMN `code`  tinyint NULL AFTER `status`;

ALTER TABLE `ball_bank_area`
ADD UNIQUE INDEX `code_unique` (`code`) ;

update ball_bank_area set code = 1 where id=1;
update ball_bank_area set code = 2 where id=2;

ALTER TABLE `ball_bank_card`
ADD COLUMN `identity_card`  varchar(50) NULL AFTER `user_id`,
ADD COLUMN `phone`  varchar(30) NULL AFTER `identity_card`;

