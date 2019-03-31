CREATE DATABASE IF NOT EXISTS HOLDINGS DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE HOLDINGS;

DROP TABLE IF EXISTS holdings;
CREATE TABLE holdings (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  account_number varchar(100) NOT NULL,
  unit decimal(20,5) NOT NULL,
  freeze_unit varchar(100) NOT NULL,
  version int(11) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY holdings_acc (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS holdings_resource;

CREATE TABLE holdings_resource (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  order_number varchar(100) NOT NULL,
  status bigint(20) NOT NULL,
  created_date datetime(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY holdings_resource_un (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS TCC_TRANSACTION_HOLDINGS;
CREATE TABLE TCC_TRANSACTION_HOLDINGS (
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

DROP PROCEDURE IF EXISTS HOLDINGS.add_holdings;
DELIMITER //
CREATE PROCEDURE HOLDINGS.add_holdings(
    in pre_acc_number varchar(100),
    in acc_quantity int,
    in unit decimal(20,5),
    in freeze_unit decimal(20,5)
)
begin
    declare i int default 0;
    declare account_number varchar(100);
    while i < acc_quantity do
        set account_number = concat(pre_acc_number, lpad(i, 6, '0'));
        set i = i + 1;
        insert into holdings(account_number, unit, freeze_unit, version) values(account_number, unit, freeze_unit, 1);
    end while;
end
//
DELIMITER ;

call HOLDINGS.add_holdings('acc', 100, 0, 0)
