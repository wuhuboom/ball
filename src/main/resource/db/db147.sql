ALTER TABLE `ball_api_config`
ADD COLUMN `first_recharge`  text NULL AFTER `player_chat`,
ADD COLUMN `second_recharge`  text NULL AFTER `first_recharge`,
ADD COLUMN `fixed_recharge`  text NULL AFTER `second_recharge`;

