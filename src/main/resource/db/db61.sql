ALTER TABLE `ball_bank_area`
DROP INDEX `code_unique`;

ALTER TABLE `ball_pay_behalf`
ADD COLUMN `area_id`  bigint NULL AFTER `pay_type`;

INSERT INTO `ball_bank_area` (`id`, `contry`, `status`, `code`) VALUES ('3', '加纳2', '0', '2');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('90', ' Agricultural Development Bank of Ghana', 'GHSADB', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('91', ' uniBank（Ghana）Limited', 'GHSCBG', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('92', ' Ecobank Ghana', 'GHSECO', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('93', ' GCB Bank Limited', 'GHSGCB', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('94', ' MTN', 'GHSMTN', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('95', ' National Investment Bank', 'GHSNIB', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('96', ' Société Générale Ghana Ltd', 'GHSSGG', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('97', ' Universal Merchant Bank', 'GHSUMB', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('98', ' Zenith Bank of Ghana', 'GHSZENITH', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('99', ' Barclays Bank of Ghana', 'GHSBARCLAYS', '3');
INSERT INTO `ball_bank` (`id`, `bank_cname`, `bank_code`, `area_id`) VALUES ('100', ' Fidelity Bank Ghana', 'GHSFIDELITY', '3');

ALTER TABLE `ball_logger_back_recharge`
ADD COLUMN `top_username`  varchar(30) NULL AFTER `player_name`,
ADD COLUMN `first_username`  varchar(30) NULL AFTER `top_username`,
ADD COLUMN `pay_type_onff`  tinyint NULL DEFAULT 0 AFTER `first_username`;

ALTER TABLE `ball_pay_behalf`
ADD COLUMN `pay_type2`  tinyint NULL AFTER `pay_type`;

