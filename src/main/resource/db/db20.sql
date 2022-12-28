ALTER TABLE `ball_bet`
ADD COLUMN `top_username`  varchar(30) NULL DEFAULT '' AFTER `game_type`,
ADD COLUMN `first_username`  varchar(30) NULL DEFAULT '' AFTER `top_username`;
