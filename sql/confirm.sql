CREATE DATABASE IF NOT EXISTS `confirm` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE `confirm`;

--
-- Table structure for table `confirm`
--

DROP TABLE IF EXISTS `confirm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `confirm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_number` varchar(100) NOT NULL,
  `agency_fee` decimal(20,5) NOT NULL,
  `unit` decimal(20,5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `confirm_un` (`order_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `retry_mq_message`
--

DROP TABLE IF EXISTS `retry_mq_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `retry_mq_message` (
  `id` varchar(36) NOT NULL,
  `message` varbinary(4000) NOT NULL,
  `status` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  `retry` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;