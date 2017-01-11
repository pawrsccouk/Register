/* This will auto-generate the table with some sample data in it */


USE `register`;

--
-- Table structure for table `children`
--

/*
CREATE TABLE `children` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `childName` varchar(50) DEFAULT NULL,
  `parentName` varchar(50) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email`   varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
*/

--
-- Dumping data for table `children`
--

LOCK TABLES `children` WRITE;
INSERT INTO `children` VALUES 
       (16,'Michael Smith','Ron and Mildred Smith','221b Privet Drive\r\nHampshire\r\nH3 2LA', NULL),
       (17,'Tracey Jones','Bettie and Lucy Jones','32 Pillar Gardens\r\nRenchley\r\nBR2 3LA', NULL);
UNLOCK TABLES;
