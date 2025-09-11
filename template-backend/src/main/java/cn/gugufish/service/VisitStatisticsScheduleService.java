package cn.gugufish.service;

import cn.gugufish.entity.dto.VisitStatistics;
import cn.gugufish.mapper.VisitStatisticsMapper;
import cn.gugufish.utils.Const;
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
 * 
 * 主要功能：
 * 1. 定期将Redis中缓存的访问统计数据同步到MySQL数据库
 * 2. 清理过期的Redis缓存数据，避免内存占用过多
 * 3. 支持手动触发数据同步功能
 * 
 * 统计指标说明：
 * - PV (Page View): 页面访问量，每次页面访问都会计数
 * - UV (Unique Visitor): 独立访客数，使用HyperLogLog算法进行去重统计
 * 
 * 数据流向：用户访问 -> Redis缓存 -> 定时同步 -> MySQL数据库
 */
@Slf4j
@Service
public class VisitStatisticsScheduleService {
    
    /**
     * 访问统计数据库操作接口
     * 用于对visit_statistics表进行增删改查操作
     */
    @Resource
    private VisitStatisticsMapper visitStatisticsMapper;
    
    /**
     * Redis字符串操作模板
     * 用于操作Redis中的字符串类型数据（PV计数、HLL序列化数据等）
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * Redis缓存键前缀定义
     * 
     * PV键格式: visit:pv:2024-01-01 (存储某日的页面访问总数)
     * UV键格式: visit:uv:hll:2024-01-01 (存储某日的HyperLogLog序列化数据)
     */
    private static final String REDIS_PV_KEY = "visit:pv:";
    private static final String REDIS_UV_HLL_KEY = "visit:uv:hll:";
    
    /**
     * 每日统计数据同步任务
     * 
     * 执行时间：每天凌晨1点 (cron表达式: 0 0 1 * * ?)
     * 功能说明：将Redis中前一天的访问统计数据同步到MySQL数据库
     * 
     * 为什么选择凌晨1点执行：
     * 1. 避开业务高峰期，减少对系统性能的影响
     * 2. 确保前一天的数据已经完整收集
     * 3. 为数据分析和报表生成提供及时的数据支持
     * 
     * 事务保证：使用@Transactional确保数据同步的原子性
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void syncDailyStatistics() {
        log.info("开始执行每日统计数据同步任务");
        
        try {
            // 计算昨天的日期（避免跨时区问题，使用系统默认时区）
            LocalDate yesterday = LocalDate.now().minusDays(1);
            // 将LocalDate转换为Date类型，设置为当天的00:00:00
            Date statisticsDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            // 生成Redis键的日期部分，格式如：2024-01-01
            String dateKey = yesterday.toString();
            
            // 调用核心同步方法，处理昨天的统计数据
            syncStatisticsForDate(statisticsDate, dateKey);
            
            log.info("每日统计数据同步任务完成");
        } catch (Exception e) {
            log.error("每日统计数据同步任务执行失败", e);
        }
    }
    
    /**
     * 每小时统计数据同步任务（实时数据更新）
     * 
     * 执行时间：每小时的整点 (cron表达式: 0 0 * * * ?)
     * 功能说明：将Redis中当天的访问统计数据实时同步到MySQL数据库
     * 
     * 设计目的：
     * 1. 提供当天访问数据的实时查询能力
     * 2. 避免Redis数据丢失导致的统计缺失
     * 3. 支持实时数据分析和监控需求
     * 
     * 与每日同步的区别：
     * - 每日同步：处理前一天的完整数据，数据稳定不变
     * - 每小时同步：处理当天的增量数据，数据持续更新
     * 
     * 日志级别：使用debug级别，避免产生过多日志
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void syncHourlyStatistics() {
        log.debug("开始执行每小时统计数据同步任务");
        
        try {
            // 获取当天的日期
            LocalDate today = LocalDate.now();
            // 将当天日期转换为Date类型，时间设置为00:00:00
            Date statisticsDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            // 生成Redis键的日期部分
            String dateKey = today.toString();
            
            // 调用核心同步方法，处理当天的统计数据
            syncStatisticsForDate(statisticsDate, dateKey);
            
            log.debug("每小时统计数据同步任务完成");
        } catch (Exception e) {
            log.error("每小时统计数据同步任务执行失败", e);
        }
    }
    
    /**
     * Redis过期数据清理任务
     * 
     * 执行时间：每周日凌晨2点 (cron表达式: 0 0 2 ? * SUN)
     * 功能说明：清理Redis中超过7天的访问统计缓存数据
     * 
     * 清理策略：
     * 1. 保留最近7天的数据，满足短期数据查询需求
     * 2. 清理7天前的数据，避免Redis内存无限增长
     * 3. 只清理缓存数据，数据库中的历史数据保持完整
     * 
     * 执行频率说明：
     * - 选择每周执行：平衡清理效果和系统开销
     * - 选择周日凌晨：业务访问量最低的时间段
     * - 选择2点执行：避开每日同步任务（1点）的时间冲突
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void cleanupExpiredRedisData() {
        log.info("开始执行Redis过期数据清理任务");
        
        try {
            // 计算截止日期：当前日期减去7天，7天前的数据将被清理
            LocalDate cutoffDate = LocalDate.now().minusDays(7);
            
            // 第一步：清理PV（页面访问量）相关的过期数据
            // 使用通配符查找所有PV键，格式如：visit:pv:2024-01-01
            Set<String> pvKeys = stringRedisTemplate.keys(Const.REDIS_PV_KEY + "*");
            if (pvKeys != null) {
                for (String key : pvKeys) {
                    // 从键名中提取日期字符串（去掉前缀部分）
                    String dateStr = key.substring(REDIS_PV_KEY.length());
                    try {
                        // 将日期字符串解析为LocalDate对象
                        LocalDate keyDate = LocalDate.parse(dateStr);
                        // 如果该日期早于截止日期，则删除对应的Redis键
                        if (keyDate.isBefore(cutoffDate)) {
                            stringRedisTemplate.delete(key);
                            log.debug("删除过期PV数据: {}", key);
                        }
                    } catch (Exception e) {
                        // 日期解析失败可能是因为键名格式不正确，记录警告并跳过
                        log.warn("解析日期失败，跳过键: {}", key);
                    }
                }
            }
            
            // 第二步：清理UV（独立访客数）相关的过期数据
            // 使用通配符查找所有UV键，格式如：visit:uv:hll:2024-01-01
            Set<String> uvKeys = stringRedisTemplate.keys(REDIS_UV_HLL_KEY + "*");
            if (uvKeys != null) {
                for (String key : uvKeys) {
                    // 从键名中提取日期字符串（去掉前缀部分）
                    String dateStr = key.substring(REDIS_UV_HLL_KEY.length());
                    try {
                        // 将日期字符串解析为LocalDate对象
                        LocalDate keyDate = LocalDate.parse(dateStr);
                        // 如果该日期早于截止日期，则删除对应的Redis键
                        if (keyDate.isBefore(cutoffDate)) {
                            stringRedisTemplate.delete(key);
                            log.debug("删除过期UV数据: {}", key);
                        }
                    } catch (Exception e) {
                        // 日期解析失败可能是因为键名格式不正确，记录警告并跳过
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
     * 同步指定日期的统计数据（核心业务方法）
     * 
     * 功能说明：将Redis中指定日期的访问统计数据同步到MySQL数据库
     * 
     * 处理流程：
     * 1. 从Redis获取PV和UV数据
     * 2. 解析HyperLogLog数据计算独立访客数
     * 3. 检查数据库中是否已存在该日期的记录
     * 4. 根据情况选择插入新记录或更新现有记录
     * 
     * @param statisticsDate 统计日期（Date类型，用于数据库操作）
     * @param dateKey 日期键（String类型，用于Redis键名，格式如：2024-01-01）
     */
    private void syncStatisticsForDate(Date statisticsDate, String dateKey) {
        try {
            // 第一步：从Redis获取PV（页面访问量）数据
            String pvKey = REDIS_PV_KEY + dateKey;  // 构造Redis键：visit:pv:2024-01-01
            String pvValue = stringRedisTemplate.opsForValue().get(pvKey);
            // 如果Redis中没有数据，默认为0（可能是新的一天或者没有访问）
            long pageViews = pvValue != null ? Long.parseLong(pvValue) : 0L;
            
            // 第二步：从Redis获取UV（独立访客数）的HyperLogLog数据
            String uvKey = REDIS_UV_HLL_KEY + dateKey;  // 构造Redis键：visit:uv:hll:2024-01-01
            String hllValue = stringRedisTemplate.opsForValue().get(uvKey);
            
            // 初始化UV相关变量
            byte[] hllBytes = null;      // HyperLogLog的二进制数据，用于存储到数据库
            long uniqueVisitors = 0L;    // 计算出的独立访客数
            
            // 第三步：处理HyperLogLog数据
            if (hllValue != null) {
                try {
                    // 将Base64编码的字符串解码为字节数组
                    hllBytes = Base64.getDecoder().decode(hllValue);
                    // 从字节数组重建HyperLogLog对象
                    HLL hll = HLL.fromBytes(hllBytes);
                    // 计算基数（独立访客数的估算值）
                    uniqueVisitors = hll.cardinality();
                } catch (Exception e) {
                    // HyperLogLog数据损坏或格式错误时的异常处理
                    log.error("解析HyperLogLog数据失败: {}", dateKey, e);
                }
            }
            
            // 第四步：检查数据库中是否已存在该日期的统计记录
            VisitStatistics existingStats = visitStatisticsMapper.selectByDate(statisticsDate);
            
            // 第五步：根据记录是否存在，选择更新或插入操作
            if (existingStats != null) {
                // 情况1：数据库中已存在该日期的记录，执行更新操作
                // 注意：这里使用增量更新PV，因为可能存在并发访问
                visitStatisticsMapper.incrementPageViews(statisticsDate, pageViews);
                
                // 更新UV数据（如果HyperLogLog数据存在）
                if (hllBytes != null) {
                    // 直接替换HyperLogLog数据和计算出的独立访客数
                    visitStatisticsMapper.updateUniqueVisitors(statisticsDate, hllBytes, uniqueVisitors);
                }
                log.debug("更新统计数据: 日期={}, PV={}, UV={}", dateKey, pageViews, uniqueVisitors);
            } else {
                // 情况2：数据库中不存在该日期的记录，执行插入操作
                VisitStatistics newStats = new VisitStatistics();
                newStats.setStatisticsDate(statisticsDate);           // 统计日期
                newStats.setPageViews(pageViews);                    // 页面访问量
                newStats.setUniqueVisitorsHll(hllBytes);             // HyperLogLog二进制数据
                newStats.setUniqueVisitors(uniqueVisitors);          // 独立访客数
                newStats.setCreateTime(new Date());                  // 创建时间
                newStats.setUpdateTime(new Date());                  // 更新时间
                
                visitStatisticsMapper.insert(newStats);
                log.debug("插入统计数据: 日期={}, PV={}, UV={}", dateKey, pageViews, uniqueVisitors);
            }
            
        } catch (Exception e) {
            log.error("同步统计数据失败: 日期={}", dateKey, e);
        }
    }
    
    /**
     * 手动触发数据同步（用于测试或紧急情况）
     * 
     * 使用场景：
     * 1. 开发测试：验证数据同步功能是否正常
     * 2. 紧急修复：定时任务失败后的手动补偿
     * 3. 数据补全：历史数据缺失时的主动同步
     * 4. 运维操作：系统维护后的数据一致性检查
     * 
     * 注意事项：
     * - 该方法不受定时任务调度限制，可随时调用
     * - 建议在业务低峰期执行，避免影响系统性能
     * - 执行前请确认Redis中存在对应日期的数据
     * 
     * @param date 需要同步的日期
     */
    public void manualSyncStatistics(Date date) {
        log.info("手动触发统计数据同步: {}", date);
        
        // 将Date类型转换为LocalDate，用于生成Redis键名
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 生成日期键，格式如：2024-01-01
        String dateKey = localDate.toString();
        
        // 调用核心同步方法执行数据同步
        syncStatisticsForDate(date, dateKey);
    }
}