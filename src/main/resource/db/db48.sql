ALTER TABLE `ball_payment_management`
ADD COLUMN `goods_name`  varchar(100) NULL AFTER `payment_key`;

CREATE TABLE `ball_player_notice` (
  `player_id` bigint(20) NOT NULL,
  `notice_id` bigint(20) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

update ball_logger_back set status=3 where status=1;