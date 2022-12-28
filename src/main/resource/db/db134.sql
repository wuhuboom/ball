ALTER TABLE `ball_bank_card`
ADD COLUMN `has_withdrawal`  tinyint NULL DEFAULT 0 AFTER `phone`;

ALTER TABLE `ball_bank_card`
DROP INDEX `player_id`;

ALTER TABLE `ball_bank_card`
MODIFY COLUMN `player_id`  bigint(20) NULL COMMENT '玩家ID' AFTER `id`;

update ball_bank_card set has_withdrawal=1 where card_number in(
select to_bank from ball_logger_withdrawal where status = 4 and to_bank !=''
);

