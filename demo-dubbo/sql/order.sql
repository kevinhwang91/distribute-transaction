CREATE DATABASE IF NOT EXISTS `ORDER` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE `ORDER`;

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  order_number varchar(100) NOT NULL,
  account_number varchar(100) NOT NULL,
  status int(11) NOT NULL,
  updated_date datetime(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY order_un (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS TCC_TRANSACTION_ORDER;
CREATE TABLE TCC_TRANSACTION_ORDER (
  TRANSACTION_ID int(11) NOT NULL AUTO_INCREMENT,
  DOMAIN varchar(100) DEFAULT NULL,
  GLOBAL_TX_ID varbinary(32) NOT NULL,
  BRANCH_QUALIFIER varbinary(32) NOT NULL,
  CONTENT varbinary(8000) DEFAULT NULL,
  STATUS int(11) DEFAULT NULL,
  TRANSACTION_TYPE int(11) DEFAULT NULL,
  RETRIED_COUNT int(11) DEFAULT NULL,
  CREATE_TIME datetime DEFAULT NULL,
  LAST_UPDATE_TIME datetime DEFAULT NULL,
  VERSION int(11) DEFAULT NULL,
  PRIMARY KEY (TRANSACTION_ID),
  UNIQUE KEY UX_TX_BQ (GLOBAL_TX_ID,BRANCH_QUALIFIER)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP PROCEDURE IF EXISTS `ORDER`.add_order;
DELIMITER //
CREATE PROCEDURE `ORDER`.add_order(
in pre_acc_number varchar(100),
in pre_order_number varchar(100),
in status int,
in acc_quantity int,
in order_quantity_per_acc int)
begin
	declare i int default 0;
	declare j int default 0;
    declare k int default 0;
	declare order_number varchar(100);
	declare account_number varchar(100);
	while i < acc_quantity do
		while j < order_quantity_per_acc do
			set order_number = concat(pre_order_number, lpad(k, 6, '0'));
			set account_number = concat(pre_acc_number, lpad(i, 6, '0'));
			set j = j + 1;
            set k = k + 1;
			insert into `order`(order_number, account_number, status, updated_date) values(order_number, account_number,
			status, current_timestamp);
		end while;
        set j = 0;
		set i = i + 1;
	end while;
end
//
DELIMITER ;

call `ORDER`.add_order('acc', 'ord', 1, 100, 10)
