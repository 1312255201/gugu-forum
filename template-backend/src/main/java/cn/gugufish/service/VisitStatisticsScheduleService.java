package cn.gugufish.service;

import cn.gugufish.entity.dto.VisitStatistics;
import cn.gugufish.mapper.VisitStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import net.agkn.hll.HLL;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

/**
 * 访问统计定时任务服务
 * 负责将Redis中的统计数据定期同步到数据库
 */
@Slf4j
@Service
public class VisitStatisticsScheduleService {
    
    @Resource
    private VisitStatisticsMapper visitStatisticsMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    // Redis缓存键前缀
    private static final String REDIS_PV_KEY = "visit:pv:";
    private static final String REDIS_UV_HLL_KEY = "visit:uv:hll:";
    
    /**
     * 每天凌晨1点执行，同步前一天的统计数据到数据库
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void syncDailyStatistics() {
        log.info("开始执行每日统计数据同步任务");
        
        try {
            // 获取昨天的日期
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Date statisticsDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String dateKey = yesterday.toString();
            
            // 同步昨天的数据
            syncStatisticsForDate(statisticsDate, dateKey);
            
            log.info("每日统计数据同步任务完成");
        } catch (Exception e) {
            log.error("每日统计数据同步任务执行失败", e);
        }
    }
    
    /**
     * 每小时执行，同步当天的统计数据到数据库（实时更新）
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void syncHourlyStatistics() {
        log.debug("开始执行每小时统计数据同步任务");
        
        try {
            // 获取今天的日期
            LocalDate today = LocalDate.now();
            Date statisticsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String dateKey = today.toString();
            
            // 同步今天的数据
            syncStatisticsForDate(statisticsDate, dateKey);
            
            log.debug("每小时统计数据同步任务完成");
        } catch (Exception e) {
            log.error("每小时统计数据同步任务执行失败", e);
        }
    }
    
    /**
     * 每周日凌晨2点执行，清理过期的Redis数据
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void cleanupExpiredRedisData() {
        log.info("开始执行Redis过期数据清理任务");
        
        try {
            // 清理7天前的Redis数据
            LocalDate cutoffDate = LocalDate.now().minusDays(7);
            
            // 查找所有PV相关的键
            Set<String> pvKeys = stringRedisTemplate.keys(REDIS_PV_KEY + "*");
            if (pvKeys != null) {
                for (String key : pvKeys) {
                    String dateStr = key.substring(REDIS_PV_KEY.length());
                    try {
                        LocalDate keyDate = LocalDate.parse(dateStr);
                        if (keyDate.isBefore(cutoffDate)) {
                            stringRedisTemplate.delete(key);
                            log.debug("删除过期PV数据: {}", key);
                        }
                    } catch (Exception e) {
                        log.warn("解析日期失败，跳过键: {}", key);
                    }
                }
            }
            
            // 查找所有UV相关的键
            Set<String> uvKeys = stringRedisTemplate.keys(REDIS_UV_HLL_KEY + "*");
            if (uvKeys != null) {
                for (String key : uvKeys) {
                    String dateStr = key.substring(REDIS_UV_HLL_KEY.length());
                    try {
                        LocalDate keyDate = LocalDate.parse(dateStr);
                        if (keyDate.isBefore(cutoffDate)) {
                            stringRedisTemplate.delete(key);
                            log.debug("删除过期UV数据: {}", key);
                        }
                    } catch (Exception e) {
                        log.warn("解析日期失败，跳过键: {}", key);
                    }
                }
            }
            
            log.info("Redis过期数据清理任务完成");
        } catch (Exception e) {
            log.error("Redis过期数据清理任务执行失败", e);
        }
    }
    
    /**
     * 同步指定日期的统计数据
     */
    private void syncStatisticsForDate(Date statisticsDate, String dateKey) {
        try {
            // 获取PV数据
            String pvKey = REDIS_PV_KEY + dateKey;
            String pvValue = stringRedisTemplate.opsForValue().get(pvKey);
            long pageViews = pvValue != null ? Long.parseLong(pvValue) : 0L;
            
            // 获取UV数据
            String uvKey = REDIS_UV_HLL_KEY + dateKey;
            String hllValue = stringRedisTemplate.opsForValue().get(uvKey);
            
            byte[] hllBytes = null;
            long uniqueVisitors = 0L;
            
            if (hllValue != null) {
                try {
                    hllBytes = Base64.getDecoder().decode(hllValue);
                    HLL hll = HLL.fromBytes(hllBytes);
                    uniqueVisitors = hll.cardinality();
                } catch (Exception e) {
                    log.error("解析HyperLogLog数据失败: {}", dateKey, e);
                }
            }
            
            // 检查数据库中是否已存在该日期的记录
            VisitStatistics existingStats = visitStatisticsMapper.selectByDate(statisticsDate);
            
            if (existingStats != null) {
                // 更新现有记录
                visitStatisticsMapper.incrementPageViews(statisticsDate, pageViews);
                if (hllBytes != null) {
                    visitStatisticsMapper.updateUniqueVisitors(statisticsDate, hllBytes, uniqueVisitors);
                }
                log.debug("更新统计数据: 日期={}, PV={}, UV={}", dateKey, pageViews, uniqueVisitors);
            } else {
                // 插入新记录
                VisitStatistics newStats = new VisitStatistics();
                newStats.setStatisticsDate(statisticsDate);
                newStats.setPageViews(pageViews);
                newStats.setUniqueVisitorsHll(hllBytes);
                newStats.setUniqueVisitors(uniqueVisitors);
                newStats.setCreateTime(new Date());
                newStats.setUpdateTime(new Date());
                
                visitStatisticsMapper.insert(newStats);
                log.debug("插入统计数据: 日期={}, PV={}, UV={}", dateKey, pageViews, uniqueVisitors);
            }
            
        } catch (Exception e) {
            log.error("同步统计数据失败: 日期={}", dateKey, e);
        }
    }
    
    /**
     * 手动触发数据同步（用于测试或紧急情况）
     */
    public void manualSyncStatistics(Date date) {
        log.info("手动触发统计数据同步: {}", date);
        
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String dateKey = localDate.toString();
        
        syncStatisticsForDate(date, dateKey);
    }
}