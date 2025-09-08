-- 访问统计表
CREATE TABLE IF NOT EXISTS `db_visit_statistics` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `statistics_date` date NOT NULL COMMENT '统计日期',
    `page_views` bigint NOT NULL DEFAULT '0' COMMENT '页面访问量(PV)',
    `unique_visitors_hll` longblob COMMENT '独立访客数HyperLogLog序列化数据',
    `unique_visitors` bigint NOT NULL DEFAULT '0' COMMENT '独立访客数估算值',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_statistics_date` (`statistics_date`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问统计表';

-- 创建定时任务存储过程（可选）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `sync_visit_statistics`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_date DATE;
    DECLARE v_pv BIGINT;
    DECLARE v_uv_hll LONGBLOB;
    DECLARE v_uv_count BIGINT;
    
    -- 这里可以添加从Redis同步数据到MySQL的逻辑
    -- 由于Redis操作需要在应用层处理，这个存储过程主要用于数据清理和维护
    
    -- 清理超过90天的统计数据（可根据需要调整）
    DELETE FROM `db_visit_statistics` 
    WHERE `statistics_date` < DATE_SUB(CURDATE(), INTERVAL 90 DAY);
    
END //
DELIMITER ;

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS `idx_date_range` ON `db_visit_statistics` (`statistics_date`, `page_views`, `unique_visitors`);

-- 插入示例数据（可选，用于测试）
-- INSERT INTO `db_visit_statistics` (`statistics_date`, `page_views`, `unique_visitors`, `create_time`, `update_time`) 
-- VALUES 
-- (CURDATE() - INTERVAL 1 DAY, 1500, 800, NOW(), NOW()),
-- (CURDATE() - INTERVAL 2 DAY, 1200, 650, NOW(), NOW()),
-- (CURDATE() - INTERVAL 3 DAY, 1800, 900, NOW(), NOW());