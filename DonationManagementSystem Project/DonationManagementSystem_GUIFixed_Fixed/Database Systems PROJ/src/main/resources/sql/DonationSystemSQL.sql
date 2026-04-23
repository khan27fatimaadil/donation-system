CREATE DATABASE  IF NOT EXISTS `donation_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `donation_system`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: donation_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allocations`
--

DROP TABLE IF EXISTS `allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `allocations` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donation_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fund_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_allocations_donation_fund` (`donation_id`,`fund_id`),
  KEY `idx_allocations_donation_id` (`donation_id`),
  KEY `idx_allocations_fund_id` (`fund_id`),
  CONSTRAINT `fk_allocations_donation` FOREIGN KEY (`donation_id`) REFERENCES `donations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_allocations_fund` FOREIGN KEY (`fund_id`) REFERENCES `funds` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_allocation_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `allocations`
--

LOCK TABLES `allocations` WRITE;
/*!40000 ALTER TABLE `allocations` DISABLE KEYS */;
INSERT INTO `allocations` VALUES ('ALC-0001-0000-0000-000000000001','DON-0001-0000-0000-000000000001','FND-0001-0000-0000-000000000002',4000.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000002','DON-0001-0000-0000-000000000001','FND-0001-0000-0000-000000000001',1000.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000003','DON-0001-0000-0000-000000000002','FND-0001-0000-0000-000000000002',10000.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000004','DON-0001-0000-0000-000000000003','FND-0001-0000-0000-000000000003',250.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000005','DON-0001-0000-0000-000000000004','FND-0001-0000-0000-000000000002',100.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000006','DON-0001-0000-0000-000000000005','FND-0001-0000-0000-000000000002',100.00,'2026-04-18 23:19:41'),('ALC-0001-0000-0000-000000000007','DON-0001-0000-0000-000000000007','FND-0001-0000-0000-000000000004',3000.00,'2026-04-18 23:19:41');
/*!40000 ALTER TABLE `allocations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campaign_status_log`
--

DROP TABLE IF EXISTS `campaign_status_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign_status_log` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `campaign_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `changed_by` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_status` enum('draft','active','paused','completed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_status` enum('draft','active','paused','completed','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_csl_changed_by` (`changed_by`),
  KEY `idx_csl_campaign_id` (`campaign_id`),
  KEY `idx_csl_changed_at` (`changed_at`),
  CONSTRAINT `fk_csl_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_csl_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campaign_status_log`
--

LOCK TABLES `campaign_status_log` WRITE;
/*!40000 ALTER TABLE `campaign_status_log` DISABLE KEYS */;
INSERT INTO `campaign_status_log` VALUES ('2b44b3f7-3b53-11f1-b0bf-a0291920b686','CMP-0001-0000-0000-000000000003',NULL,'draft','active',NULL,'2026-04-18 23:19:41');
/*!40000 ALTER TABLE `campaign_status_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campaigns`
--

DROP TABLE IF EXISTS `campaigns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaigns` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `organization_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `goal_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` enum('draft','active','paused','completed','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_campaigns_organization_id` (`organization_id`),
  KEY `idx_campaigns_status` (`status`),
  KEY `idx_campaigns_start_date` (`start_date`),
  KEY `idx_campaigns_end_date` (`end_date`),
  KEY `idx_campaigns_currency_code` (`currency_code`),
  CONSTRAINT `fk_campaigns_currency` FOREIGN KEY (`currency_code`) REFERENCES `currencies` (`code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_campaigns_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_campaign_dates` CHECK (((`end_date` is null) or (`end_date` >= `start_date`))),
  CONSTRAINT `chk_goal_amount` CHECK ((`goal_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campaigns`
--

LOCK TABLES `campaigns` WRITE;
/*!40000 ALTER TABLE `campaigns` DISABLE KEYS */;
INSERT INTO `campaigns` VALUES ('CMP-0001-0000-0000-000000000001','ORG-0001-0000-0000-000000000001','Clean Water for All','Provide clean drinking water to 10,000 families in rural Pakistan.',500000.00,'USD','2025-01-01','2025-12-31','active',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('CMP-0001-0000-0000-000000000002','ORG-0001-0000-0000-000000000001','Education Fund Drive 2025','Raise funds to award 50 scholarships to underprivileged students.',200000.00,'USD','2025-03-01','2025-09-30','active',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('CMP-0001-0000-0000-000000000003','ORG-0001-0000-0000-000000000002','Reforestation Project','Plant 100,000 trees across degraded land in South Asia.',150000.00,'USD','2025-06-01','2025-12-31','active',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `campaigns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `communications`
--

DROP TABLE IF EXISTS `communications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `communications` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donor_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `donation_id` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_by` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` enum('email','sms','letter','phone','in_app') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subject` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `body` text COLLATE utf8mb4_unicode_ci,
  `status` enum('draft','queued','sent','delivered','failed','bounced') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `sent_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_communications_donation` (`donation_id`),
  KEY `fk_communications_sent_by` (`sent_by`),
  KEY `idx_communications_donor_id` (`donor_id`),
  KEY `idx_communications_status` (`status`),
  KEY `idx_communications_sent_at` (`sent_at`),
  CONSTRAINT `fk_communications_donation` FOREIGN KEY (`donation_id`) REFERENCES `donations` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_communications_donor` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_communications_sent_by` FOREIGN KEY (`sent_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `communications`
--

LOCK TABLES `communications` WRITE;
/*!40000 ALTER TABLE `communications` DISABLE KEYS */;
INSERT INTO `communications` VALUES ('COM-0001-0000-0000-000000000001','DNR-0001-0000-0000-000000000002','DON-0001-0000-0000-000000000001','USR-0001-0000-0000-000000000001','email','Thank you for your generous donation!','Dear Emily, thank you for your $5,000 donation to our Clean Water for All campaign...','delivered','2025-01-15 12:10:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('COM-0001-0000-0000-000000000002','DNR-0001-0000-0000-000000000004','DON-0001-0000-0000-000000000002','USR-0001-0000-0000-000000000001','email','Receipt for your donation — RCPT-2025-0002','Dear TechCorp Giving Fund, please find your official receipt attached...','delivered','2025-02-01 10:15:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('COM-0001-0000-0000-000000000003','DNR-0001-0000-0000-000000000003','DON-0001-0000-0000-000000000006','USR-0001-0000-0000-000000000002','email','Action required: your donation could not be processed','Dear Usman, unfortunately your recent donation of $500 failed. Please update your payment...','bounced','2025-03-20 12:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `communications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `countries`
--

DROP TABLE IF EXISTS `countries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `countries` (
  `code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `region` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `countries`
--

LOCK TABLES `countries` WRITE;
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` VALUES ('AE','United Arab Emirates','Asia',1),('AU','Australia','Oceania',1),('CA','Canada','Americas',1),('DE','Germany','Europe',1),('FR','France','Europe',1),('GB','United Kingdom','Europe',1),('IN','India','Asia',1),('NG','Nigeria','Africa',1),('PK','Pakistan','Asia',1),('US','United States','Americas',1);
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `currencies`
--

DROP TABLE IF EXISTS `currencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `currencies` (
  `code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `symbol` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `currencies`
--

LOCK TABLES `currencies` WRITE;
/*!40000 ALTER TABLE `currencies` DISABLE KEYS */;
INSERT INTO `currencies` VALUES ('AUD','Australian Dollar','$',1),('CAD','Canadian Dollar','$',1),('EUR','Euro','€',1),('GBP','British Pound','£',1),('PKR','Pakistani Rupee','₨',1),('USD','US Dollar','$',1);
/*!40000 ALTER TABLE `currencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donation_status_log`
--

DROP TABLE IF EXISTS `donation_status_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donation_status_log` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donation_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `changed_by` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_status` enum('pending','completed','failed','refunded','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_status` enum('pending','completed','failed','refunded','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_dsl_changed_by` (`changed_by`),
  KEY `idx_dsl_donation_id` (`donation_id`),
  KEY `idx_dsl_changed_at` (`changed_at`),
  KEY `idx_dsl_new_status` (`new_status`),
  CONSTRAINT `fk_dsl_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_dsl_donation` FOREIGN KEY (`donation_id`) REFERENCES `donations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donation_status_log`
--

LOCK TABLES `donation_status_log` WRITE;
/*!40000 ALTER TABLE `donation_status_log` DISABLE KEYS */;
INSERT INTO `donation_status_log` VALUES ('2b4537f5-3b53-11f1-b0bf-a0291920b686','DON-0001-0000-0000-000000000003','USR-0001-0000-0000-000000000002','completed','refunded',NULL,'2026-04-18 23:19:41');
/*!40000 ALTER TABLE `donation_status_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donations`
--

DROP TABLE IF EXISTS `donations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donations` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donor_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `campaign_id` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recurring_plan_id` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `processed_by` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD',
  `type` enum('one_time','recurring','pledge','in_kind') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'one_time',
  `status` enum('pending','completed','failed','refunded','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `anonymous` tinyint NOT NULL DEFAULT '0',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `donated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_donations_donor_id` (`donor_id`),
  KEY `idx_donations_campaign_id` (`campaign_id`),
  KEY `idx_donations_recurring_plan_id` (`recurring_plan_id`),
  KEY `idx_donations_processed_by` (`processed_by`),
  KEY `idx_donations_status` (`status`),
  KEY `idx_donations_type` (`type`),
  KEY `idx_donations_donated_at` (`donated_at`),
  KEY `idx_donations_currency_code` (`currency_code`),
  CONSTRAINT `fk_donations_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_donations_currency` FOREIGN KEY (`currency_code`) REFERENCES `currencies` (`code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_donations_donor` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_donations_processed_by` FOREIGN KEY (`processed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_donations_recurring_plan` FOREIGN KEY (`recurring_plan_id`) REFERENCES `recurring_plans` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_donation_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donations`
--

LOCK TABLES `donations` WRITE;
/*!40000 ALTER TABLE `donations` DISABLE KEYS */;
INSERT INTO `donations` VALUES ('DON-0001-0000-0000-000000000001','DNR-0001-0000-0000-000000000002','CMP-0001-0000-0000-000000000001',NULL,'USR-0001-0000-0000-000000000002',5000.00,'USD','one_time','completed',0,NULL,'2025-01-15 10:30:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000002','DNR-0001-0000-0000-000000000004','CMP-0001-0000-0000-000000000001',NULL,'USR-0001-0000-0000-000000000002',10000.00,'USD','one_time','completed',0,NULL,'2025-02-01 09:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000003','DNR-0001-0000-0000-000000000003','CMP-0001-0000-0000-000000000002',NULL,'USR-0001-0000-0000-000000000002',250.00,'USD','one_time','refunded',1,NULL,'2025-03-10 14:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000004','DNR-0001-0000-0000-000000000001','CMP-0001-0000-0000-000000000001','RCP-0001-0000-0000-000000000001','USR-0001-0000-0000-000000000001',100.00,'USD','recurring','completed',0,NULL,'2025-01-01 00:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000005','DNR-0001-0000-0000-000000000001','CMP-0001-0000-0000-000000000001','RCP-0001-0000-0000-000000000001','USR-0001-0000-0000-000000000001',100.00,'USD','recurring','completed',0,NULL,'2025-02-01 00:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000006','DNR-0001-0000-0000-000000000003','CMP-0001-0000-0000-000000000002',NULL,'USR-0001-0000-0000-000000000002',500.00,'USD','one_time','failed',0,NULL,'2025-03-20 11:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('DON-0001-0000-0000-000000000007','DNR-0001-0000-0000-000000000005',NULL,NULL,'USR-0001-0000-0000-000000000001',3000.00,'USD','in_kind','completed',0,NULL,'2025-04-01 08:00:00','2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `donations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donor_tags`
--

DROP TABLE IF EXISTS `donor_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donor_tags` (
  `donor_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tag_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `assigned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`donor_id`,`tag_id`),
  KEY `idx_donor_tags_tag_id` (`tag_id`),
  CONSTRAINT `fk_donor_tags_donor` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_donor_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donor_tags`
--

LOCK TABLES `donor_tags` WRITE;
/*!40000 ALTER TABLE `donor_tags` DISABLE KEYS */;
INSERT INTO `donor_tags` VALUES ('DNR-0001-0000-0000-000000000001','TAG-0001-0000-0000-000000000002','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000001','TAG-0001-0000-0000-000000000005','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000002','TAG-0001-0000-0000-000000000001','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000002','TAG-0001-0000-0000-000000000006','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000004','TAG-0001-0000-0000-000000000001','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000004','TAG-0001-0000-0000-000000000003','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000005','TAG-0001-0000-0000-000000000003','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `donor_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donors`
--

DROP TABLE IF EXISTS `donors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donors` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organization_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` enum('individual','organization','foundation','corporate') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'individual',
  `address` text COLLATE utf8mb4_unicode_ci,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country_code` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `is_active` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_donors_email` (`email`),
  KEY `idx_donors_email` (`email`),
  KEY `idx_donors_type` (`type`),
  KEY `idx_donors_is_active` (`is_active`),
  KEY `idx_donors_country_code` (`country_code`),
  CONSTRAINT `fk_donors_country` FOREIGN KEY (`country_code`) REFERENCES `countries` (`code`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_donor_name` CHECK (((`first_name` is not null) or (`organization_name` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donors`
--

LOCK TABLES `donors` WRITE;
/*!40000 ALTER TABLE `donors` DISABLE KEYS */;
INSERT INTO `donors` VALUES ('DNR-0001-0000-0000-000000000001','Ali','Hassan',NULL,'ali.hassan@email.com','+92-300-9999999','individual','25 Saddar Road','Rawalpindi','PK','46000',NULL,1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000002','Emily','Johnson',NULL,'emily.j@email.com','+1-646-555-0101','individual','88 Park Avenue','New York','US','10016',NULL,1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000003','Usman','Malik',NULL,'usman.malik@email.com','+92-321-9876543','individual','5 Model Town','Lahore','PK','54000',NULL,0,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000004',NULL,NULL,'TechCorp Giving Fund','giving@techcorp.com','+1-408-555-0300','corporate','1 Silicon Way','San Jose','US','95110',NULL,1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('DNR-0001-0000-0000-000000000005',NULL,NULL,'Al-Noor Foundation','info@alnoor.org','+971-4-555-0400','foundation','PO Box 1234','Dubai','AE','00000',NULL,1,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `donors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `funds`
--

DROP TABLE IF EXISTS `funds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funds` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `type` enum('unrestricted','restricted','endowment','quasi_endowment') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unrestricted',
  `is_active` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `funds`
--

LOCK TABLES `funds` WRITE;
/*!40000 ALTER TABLE `funds` DISABLE KEYS */;
INSERT INTO `funds` VALUES ('FND-0001-0000-0000-000000000001','General Operations','Covers day-to-day operational costs of the organization.','unrestricted',1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('FND-0001-0000-0000-000000000002','Clean Water Initiative','Restricted fund for clean water projects in South Asia.','restricted',1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('FND-0001-0000-0000-000000000003','Education Endowment','Endowment fund to provide scholarships in perpetuity.','endowment',1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('FND-0001-0000-0000-000000000004','Emergency Relief','Rapid-response fund for natural disaster relief.','restricted',0,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `funds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `organizations`
--

DROP TABLE IF EXISTS `organizations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organizations` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tax_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_organizations_tax_id` (`tax_id`),
  KEY `idx_organizations_tax_id` (`tax_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organizations`
--

LOCK TABLES `organizations` WRITE;
/*!40000 ALTER TABLE `organizations` DISABLE KEYS */;
INSERT INTO `organizations` VALUES ('ORG-0001-0000-0000-000000000001','Hope Foundation','EIN-12-3456789','admin@hopefoundation.org','123 Charity Lane, New York, NY 10001','+1-212-555-0100','https://www.hopefoundation.org','2026-04-18 23:19:41','2026-04-18 23:19:41'),('ORG-0001-0000-0000-000000000002','Green Future NGO','EIN-98-7654321','contact@greenfuture.org','456 Eco Street, San Francisco, CA 94102','+1-415-555-0200','https://www.greenfuture.org','2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `organizations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_transactions`
--

DROP TABLE IF EXISTS `payment_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_transactions` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donation_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gateway` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gateway_tx_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `method` enum('credit_card','debit_card','bank_transfer','paypal','stripe','cash','check','crypto') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('pending','processing','succeeded','failed','refunded','disputed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `amount` decimal(15,2) NOT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD',
  `fee_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `metadata` json DEFAULT NULL,
  `processed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payment_transactions_gateway_tx_id` (`gateway_tx_id`),
  KEY `idx_payment_transactions_donation_id` (`donation_id`),
  KEY `idx_payment_transactions_gateway_tx_id` (`gateway_tx_id`),
  KEY `idx_payment_transactions_status` (`status`),
  KEY `idx_payment_transactions_currency_code` (`currency_code`),
  CONSTRAINT `fk_payment_transactions_currency` FOREIGN KEY (`currency_code`) REFERENCES `currencies` (`code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_transactions_donation` FOREIGN KEY (`donation_id`) REFERENCES `donations` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_tx_amount` CHECK ((`amount` > 0)),
  CONSTRAINT `chk_tx_fee_amount` CHECK ((`fee_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transactions`
--

LOCK TABLES `payment_transactions` WRITE;
/*!40000 ALTER TABLE `payment_transactions` DISABLE KEYS */;
INSERT INTO `payment_transactions` VALUES ('TXN-0001-0000-0000-000000000001','DON-0001-0000-0000-000000000001','Stripe','ch_stripe_001','credit_card','succeeded',5000.00,'USD',147.50,NULL,NULL,'2025-01-15 10:30:45','2026-04-18 23:19:41','2026-04-18 23:19:41'),('TXN-0001-0000-0000-000000000002','DON-0001-0000-0000-000000000002','Stripe','ch_stripe_002','bank_transfer','succeeded',10000.00,'USD',25.00,NULL,NULL,'2025-02-01 09:01:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('TXN-0001-0000-0000-000000000003','DON-0001-0000-0000-000000000003','PayPal','pp_txn_003','paypal','succeeded',250.00,'USD',9.55,NULL,NULL,'2025-03-10 14:00:30','2026-04-18 23:19:41','2026-04-18 23:19:41'),('TXN-0001-0000-0000-000000000004','DON-0001-0000-0000-000000000004','Stripe','ch_stripe_004','credit_card','succeeded',100.00,'USD',3.20,NULL,NULL,'2025-01-01 00:01:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('TXN-0001-0000-0000-000000000005','DON-0001-0000-0000-000000000005','Stripe','ch_stripe_005','credit_card','succeeded',100.00,'USD',3.20,NULL,NULL,'2025-02-01 00:01:00','2026-04-18 23:19:41','2026-04-18 23:19:41'),('TXN-0001-0000-0000-000000000006','DON-0001-0000-0000-000000000006','Stripe','ch_stripe_006','credit_card','failed',500.00,'USD',0.00,'Card declined: insufficient funds.',NULL,NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `payment_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipts`
--

DROP TABLE IF EXISTS `receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipts` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donation_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `issued_by` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receipt_number` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `issued_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_receipts_donation_id` (`donation_id`),
  UNIQUE KEY `uq_receipts_receipt_number` (`receipt_number`),
  KEY `fk_receipts_issued_by` (`issued_by`),
  KEY `idx_receipts_issued_at` (`issued_at`),
  CONSTRAINT `fk_receipts_donation` FOREIGN KEY (`donation_id`) REFERENCES `donations` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_receipts_issued_by` FOREIGN KEY (`issued_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipts`
--

LOCK TABLES `receipts` WRITE;
/*!40000 ALTER TABLE `receipts` DISABLE KEYS */;
INSERT INTO `receipts` VALUES ('RCT-0001-0000-0000-000000000001','DON-0001-0000-0000-000000000001','USR-0001-0000-0000-000000000001','RCPT-2025-0001','2025-01-15 12:00:00',NULL,'2025-01-15 12:05:00','2026-04-18 23:19:41'),('RCT-0001-0000-0000-000000000002','DON-0001-0000-0000-000000000002','USR-0001-0000-0000-000000000001','RCPT-2025-0002','2025-02-01 10:00:00',NULL,'2025-02-01 10:10:00','2026-04-18 23:19:41'),('RCT-0001-0000-0000-000000000003','DON-0001-0000-0000-000000000003','USR-0001-0000-0000-000000000002','RCPT-2025-0003','2025-03-10 15:00:00',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('RCT-0001-0000-0000-000000000004','DON-0001-0000-0000-000000000004','USR-0001-0000-0000-000000000001','RCPT-2025-0004','2025-01-01 01:00:00',NULL,'2025-01-01 01:05:00','2026-04-18 23:19:41'),('RCT-0001-0000-0000-000000000005','DON-0001-0000-0000-000000000005','USR-0001-0000-0000-000000000001','RCPT-2025-0005','2025-02-01 01:00:00',NULL,'2025-02-01 01:05:00','2026-04-18 23:19:41'),('RCT-0001-0000-0000-000000000006','DON-0001-0000-0000-000000000007','USR-0001-0000-0000-000000000001','RCPT-2025-0006','2025-04-01 09:00:00',NULL,'2025-04-01 09:15:00','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `receipts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recurring_plans`
--

DROP TABLE IF EXISTS `recurring_plans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recurring_plans` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `donor_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `campaign_id` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD',
  `frequency` enum('weekly','monthly','quarterly','annually') COLLATE utf8mb4_unicode_ci NOT NULL,
  `next_charge_date` date NOT NULL,
  `start_date` date NOT NULL DEFAULT (curdate()),
  `end_date` date DEFAULT NULL,
  `status` enum('active','paused','cancelled','completed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `gateway_plan_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_recurring_plans_campaign` (`campaign_id`),
  KEY `idx_recurring_plans_donor_id` (`donor_id`),
  KEY `idx_recurring_plans_status` (`status`),
  KEY `idx_recurring_plans_next_charge_date` (`next_charge_date`),
  KEY `idx_recurring_plans_currency_code` (`currency_code`),
  CONSTRAINT `fk_recurring_plans_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_recurring_plans_currency` FOREIGN KEY (`currency_code`) REFERENCES `currencies` (`code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_recurring_plans_donor` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_recurring_amount` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recurring_plans`
--

LOCK TABLES `recurring_plans` WRITE;
/*!40000 ALTER TABLE `recurring_plans` DISABLE KEYS */;
INSERT INTO `recurring_plans` VALUES ('RCP-0001-0000-0000-000000000001','DNR-0001-0000-0000-000000000001','CMP-0001-0000-0000-000000000001',100.00,'USD','monthly','2025-06-01','2025-01-01',NULL,'active',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('RCP-0001-0000-0000-000000000002','DNR-0001-0000-0000-000000000002','CMP-0001-0000-0000-000000000002',500.00,'USD','quarterly','2025-06-01','2025-03-01',NULL,'paused',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('RCP-0001-0000-0000-000000000003','DNR-0001-0000-0000-000000000004','CMP-0001-0000-0000-000000000001',2000.00,'USD','annually','2026-01-01','2025-01-01',NULL,'active',NULL,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `recurring_plans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `color` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#6366f1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tags_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` VALUES ('TAG-0001-0000-0000-000000000001','major-donor','#7C3AED','2026-04-18 23:19:41'),('TAG-0001-0000-0000-000000000002','recurring','#059669','2026-04-18 23:19:41'),('TAG-0001-0000-0000-000000000003','corporate','#2563EB','2026-04-18 23:19:41'),('TAG-0001-0000-0000-000000000004','lapsed','#DC2626','2026-04-18 23:19:41'),('TAG-0001-0000-0000-000000000005','newsletter','#D97706','2026-04-18 23:19:41'),('TAG-0001-0000-0000-000000000006','vip','#DB2777','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT (uuid()),
  `organization_id` char(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','manager','fundraiser','auditor','readonly') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'readonly',
  `is_active` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  KEY `idx_users_organization_id` (`organization_id`),
  KEY `idx_users_email` (`email`),
  KEY `idx_users_is_active` (`is_active`),
  CONSTRAINT `fk_users_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('USR-0001-0000-0000-000000000001','ORG-0001-0000-0000-000000000001','Sarah','Ahmed','sarah.ahmed@hopefoundation.org','admin',1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('USR-0001-0000-0000-000000000002','ORG-0001-0000-0000-000000000001','James','Carter','james.carter@hopefoundation.org','manager',1,'2026-04-18 23:19:41','2026-04-18 23:19:41'),('USR-0001-0000-0000-000000000003','ORG-0001-0000-0000-000000000002','Ayesha','Khan','ayesha.khan@greenfuture.org','manager',1,'2026-04-18 23:19:41','2026-04-18 23:19:41');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vw_allocation_check`
--

DROP TABLE IF EXISTS `vw_allocation_check`;
/*!50001 DROP VIEW IF EXISTS `vw_allocation_check`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_allocation_check` AS SELECT 
 1 AS `donation_id`,
 1 AS `donation_amount`,
 1 AS `allocated_amount`,
 1 AS `unallocated_amount`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_campaign_totals`
--

DROP TABLE IF EXISTS `vw_campaign_totals`;
/*!50001 DROP VIEW IF EXISTS `vw_campaign_totals`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_campaign_totals` AS SELECT 
 1 AS `campaign_id`,
 1 AS `title`,
 1 AS `goal_amount`,
 1 AS `currency_code`,
 1 AS `raised_amount`,
 1 AS `donation_count`,
 1 AS `progress_pct`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `vw_allocation_check`
--

/*!50001 DROP VIEW IF EXISTS `vw_allocation_check`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_allocation_check` AS select `d`.`id` AS `donation_id`,`d`.`amount` AS `donation_amount`,coalesce(sum(`a`.`amount`),0) AS `allocated_amount`,(`d`.`amount` - coalesce(sum(`a`.`amount`),0)) AS `unallocated_amount` from (`donations` `d` left join `allocations` `a` on((`a`.`donation_id` = `d`.`id`))) where (`d`.`status` = 'completed') group by `d`.`id`,`d`.`amount` having (abs((`d`.`amount` - coalesce(sum(`a`.`amount`),0))) > 0.01) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_campaign_totals`
--

/*!50001 DROP VIEW IF EXISTS `vw_campaign_totals`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_campaign_totals` AS select `c`.`id` AS `campaign_id`,`c`.`title` AS `title`,`c`.`goal_amount` AS `goal_amount`,`c`.`currency_code` AS `currency_code`,coalesce(sum(`d`.`amount`),0) AS `raised_amount`,count(`d`.`id`) AS `donation_count`,round(((coalesce(sum(`d`.`amount`),0) / nullif(`c`.`goal_amount`,0)) * 100),2) AS `progress_pct` from (`campaigns` `c` left join `donations` `d` on(((`d`.`campaign_id` = `c`.`id`) and (`d`.`status` = 'completed')))) group by `c`.`id`,`c`.`title`,`c`.`goal_amount`,`c`.`currency_code` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-20 20:19:51
