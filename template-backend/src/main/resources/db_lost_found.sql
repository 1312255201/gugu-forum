-- 失物招领表
CREATE TABLE `db_lost_found` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` int NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `description` text NOT NULL COMMENT '详细描述',
  `location` varchar(100) NOT NULL COMMENT '丢失位置',
  `lost_time` datetime NOT NULL COMMENT '丢失时间',
  `contact_info` varchar(200) NOT NULL COMMENT '联系方式',
  `images` text COMMENT '图片URLs（JSON格式）',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态：0-寻找中，1-已找到，2-已过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_location` (`location`),
  KEY `idx_lost_time` (`lost_time`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='失物招领表'; 