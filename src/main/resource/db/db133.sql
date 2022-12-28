ALTER TABLE `ball_api_config`
ADD COLUMN `player_token`  varchar(100) NULL DEFAULT '' AFTER `todo_chat`,
ADD COLUMN `player_chat`  varchar(30) NULL DEFAULT '' AFTER `player_token`;

