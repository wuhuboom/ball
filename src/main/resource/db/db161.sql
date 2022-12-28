ALTER TABLE `ball_country`
ADD COLUMN `area_code`  varchar(255) NULL AFTER `name`;

ALTER TABLE `ball_pay_behalf`
ADD COLUMN `country_id`  bigint NULL AFTER `white_ip`;

