CREATE DATABASE IF NOT EXISTS CONFIRM DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE CONFIRM;

DROP TABLE IF EXISTS confirm;
CREATE TABLE confirm (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  order_number varchar(100) NOT NULL,
  agency_fee decimal(20,5) NOT NULL,
  unit decimal(20,5) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY confirm_un (order_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS retry_mq_message;
CREATE TABLE retry_mq_message (
  id varchar(36) NOT NULL,
  message varbinary(4000) NOT NULL,
  status int(11) NOT NULL,
  created_date datetime NOT NULL,
  retry int(11) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
