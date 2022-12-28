ALTER TABLE `ball_bank_card`
DROP INDEX `player_id` ,
ADD UNIQUE INDEX `player_id` (`player_id`) USING BTREE ;

