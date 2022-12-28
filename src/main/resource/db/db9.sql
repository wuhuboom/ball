ALTER TABLE `ball_game`
ADD COLUMN `settlement_time`  bigint NULL DEFAULT 0 AFTER `total_bet`;

ALTER TABLE `ball_logger_withdrawal`
ADD COLUMN `usdt_rate`  int NULL DEFAULT 0 AFTER `behalf_no`,
ADD COLUMN `usdt_money`  bigint NULL DEFAULT 0 AFTER `usdt_rate`;

delete from ball_bank where id in (
select tmp.id from(
(select id,bank_cname,count(id) as count from ball_bank group by bank_cname HAVING count>1) tmp))