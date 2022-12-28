CREATE TABLE `ball_logger_bind_card` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` bigint(20) NOT NULL COMMENT '玩家ID',
  `username` varchar(50) DEFAULT NULL,
  `card_number` varchar(100) NOT NULL DEFAULT '' COMMENT '卡号',
  `bank_id` bigint(20) DEFAULT NULL,
  `bank_name` varchar(100) NOT NULL DEFAULT '' COMMENT '银行名字',
  `back_encoding` varchar(100) DEFAULT '' COMMENT '银行编码',
  `card_name` varchar(100) DEFAULT '' COMMENT '持卡人姓名',
  `country` varchar(100) DEFAULT '' COMMENT '国际',
  `province` varchar(100) DEFAULT '' COMMENT '省份',
  `city` varchar(100) NOT NULL DEFAULT '' COMMENT '城市',
  `sub_branch` varchar(100) NOT NULL DEFAULT '' COMMENT '支行',
  `created_at` bigint(20) DEFAULT '0' COMMENT '创建时间',
  `identity_card` varchar(50) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `updated_at` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1426 DEFAULT CHARSET=utf8 COMMENT='银行卡';

