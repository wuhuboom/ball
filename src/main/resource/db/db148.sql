ALTER TABLE `ball_api_config`
ADD COLUMN `first_recharge2`  text NULL AFTER `fixed_recharge`;

UPDATE `ball_api_config`
SET
 `first_recharge` = 'Congrats member bc {0}\nInvite a new member of a first recharge of {1}\nNew member {2} get {3} bonus\nThe superior gets {4} rewards\nAt the same time, get a 10% commission on the daily income of new members.\nHere I encourage everyone to work hard and hope that the future will be better.',
 `second_recharge` = 'Congratulations member {0}\nRecharge again and get {1} reward\nHere I encourage everyone to work hard and hope that the future will be better.',
 `fixed_recharge` = 'Congratulations member {0}\nGet {1} bonus on the recharge day\nHere I encourage everyone to work hard and hope that the future will be better.',
 `first_recharge2` = 'Congrats member {0}\nInvite a new member of a first recharge of {1}\nNew member {2} get {3} bonus\nAt the same time, get a 10% commission on the daily income of new members.\nHere I encourage everyone to work hard and hope that the future will be better.';

