CREATE DATABASE IF NOT EXISTS `holdings` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE `holdings`;

--
-- Table structure for table `holdings`
--

DROP TABLE IF EXISTS `holdings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `holdings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_number` varchar(100) NOT NULL,
  `unit` decimal(20,5) NOT NULL,
  `freeze_unit` varchar(100) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `holdings_acc` (`account_number`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `holdings_resource`
--

DROP TABLE IF EXISTS `holdings_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `holdings_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_number` varchar(100) NOT NULL,
  `status` bigint(20) NOT NULL,
  `created_date` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `holdings_resource_un` (`order_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tcc_transaction_holdings`
--

DROP TABLE IF EXISTS `tcc_transaction_holdings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tcc_transaction_holdings` (
  `TRANSACTION_ID` int(11) NOT NULL AUTO_INCREMENT,
  `DOMAIN` varchar(100) DEFAULT NULL,
  `GLOBAL_TX_ID` varbinary(32) NOT NULL,
  `BRANCH_QUALIFIER` varbinary(32) NOT NULL,
  `CONTENT` varbinary(8000) DEFAULT NULL,
  `STATUS` int(11) DEFAULT NULL,
  `TRANSACTION_TYPE` int(11) DEFAULT NULL,
  `RETRIED_COUNT` int(11) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `LAST_UPDATE_TIME` datetime DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`TRANSACTION_ID`),
  UNIQUE KEY `UX_TX_BQ` (`GLOBAL_TX_ID`,`BRANCH_QUALIFIER`)
) ENGINE=InnoDB AUTO_INCREMENT=60634 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

DELIMITER //
CREATE PROCEDURE `holdings`.`add_holdings`(
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
        insert into `holdings`(account_number, unit, freeze_unit, version) values(account_number, unit, freeze_unit, 1);
    end while;
end
//
DELIMITER ;

call `holdings`.`add_holdings`('acc', 100, 0, 0)