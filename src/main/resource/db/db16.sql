ALTER TABLE `ball_player`
ADD COLUMN `frozen_bet`  bigint NULL DEFAULT 0 AFTER `login_contry`,
ADD COLUMN `frozen_withdrawal`  bigint NULL DEFAULT 0 AFTER `frozen_bet`;

