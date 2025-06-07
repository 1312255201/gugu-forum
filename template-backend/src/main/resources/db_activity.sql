-- 校园活动表
CREATE TABLE `db_activity` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `title` varchar(100) NOT NULL COMMENT '活动标题',
  `content` text NOT NULL COMMENT '活动内容（富文本）',
  `location` varchar(100) NOT NULL COMMENT '活动地点',
  `activity_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片URL',
  `max_participants` int DEFAULT 0 COMMENT '最大参与人数（0表示不限制）',
  `current_participants` int DEFAULT 0 COMMENT '当前参与人数',
  `status` int DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_activity_time` (`activity_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园活动表'; 