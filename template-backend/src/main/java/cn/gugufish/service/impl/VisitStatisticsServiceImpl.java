package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.VisitStatistics;
import cn.gugufish.entity.vo.response.VisitStatisticsVO;
import cn.gugufish.entity.vo.response.VisitStatisticsSummaryVO;
import cn.gugufish.mapper.VisitStatisticsMapper;
import cn.gugufish.service.VisitStatisticsService;
import cn.gugufish.utils.Const;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.agkn.hll.HLL;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访问统计服务实现类
 * 
 * 核心功能：
 * 1. 页面访问量(PV)统计：记录每次页面访问
 * 2. 独立访客数(UV)统计：使用HyperLogLog算法进行去重统计
 * 3. 实时数据查询：支持当天实时数据和历史数据查询
 * 4. 统计数据汇总：提供日、周、月等维度的数据汇总
 * 
 * 技术特点：
 * - 使用Redis缓存提高性能，减少数据库压力
 * - 采用HyperLogLog算法实现高效的UV统计
 * - 支持数据库和Redis的双写模式，保证数据一致性
 * - 提供灵活的时间范围查询功能
 * 
 * 数据存储策略：
 * - Redis：存储实时数据，设置2天过期时间
 * - MySQL：存储历史数据，用于长期统计分析
 */
@Slf4j
@Service
public class VisitStatisticsServiceImpl implements VisitStatisticsService {
    
    /**
     * 访问统计数据库操作接口
     * 用于访问统计数据的持久化操作
     */
    @Resource
    private VisitStatisticsMapper visitStatisticsMapper;
    
    /**
     * Redis字符串操作模板
     * 用于操作Redis中的PV计数和HyperLogLog序列化数据
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * HyperLogLog参数配置
     * 
     * HLL_LOG2M = 14: 表示使用2^14 = 16384个桶，影响精度和内存使用
     * - 桶数越多，精度越高，但内存占用也越大
     * - 16384个桶在精度和内存之间取得良好平衡
     * 
     * HLL_REGWIDTH = 5: 每个桶使用5位存储，影响可统计的最大基数
     * - 5位可以存储0-31的值，支持统计更大的数据集
     */
    private static final int HLL_LOG2M = 14; // 2^14 = 16384 buckets
    private static final int HLL_REGWIDTH = 5; // 5 bits per bucket

    /**
     * 记录页面访问量(PV)
     * 
     * 功能说明：每当用户访问页面时调用此方法，同时记录PV和UV
     * 
     * 处理流程：
     * 1. 在Redis中增加PV计数（原子操作，支持高并发）
     * 2. 调用UV记录方法，统计独立访客
     * 3. 在数据库中更新PV计数（确保数据持久化）
     * 
     * 性能优化：
     * - Redis操作：O(1)时间复杂度，支持高并发访问
     * - 数据库操作：使用事务保证数据一致性
     * - 双写策略：Redis提供实时查询，数据库提供持久化
     * 
     * @param userIp 用户IP地址，用于UV统计的去重标识
     * @param userAgent 用户代理字符串，增强去重的准确性
     */
    @Override
    @Transactional
    public void recordPageView(String userIp, String userAgent) {
        Date today = new Date();
        String dateKey = getDateKey(today);  // 生成日期键，格式：2024-01-01
        
        // 第一步：在Redis中原子性地增加PV计数
        String pvKey = Const.REDIS_PV_KEY + dateKey;  // 构造Redis键：visit:pv:2024-01-01
        stringRedisTemplate.opsForValue().increment(pvKey);  // 原子递增操作
        stringRedisTemplate.expire(pvKey, 2, TimeUnit.DAYS);  // 设置2天过期时间
        
        // 第二步：同时记录独立访客数据
        recordUniqueVisitor(userIp, userAgent);
        
        // 第三步：确保数据库中存在当天记录，然后更新PV计数
        visitStatisticsMapper.insertOrUpdateRecord(today);  // 如果记录不存在则插入
        visitStatisticsMapper.incrementPageViews(today, 1L);  // 增加PV计数
        
        log.debug("记录页面访问: IP={}, UserAgent={}", userIp, userAgent);
    }
    
    /**
     * 记录独立访客数(UV)
     * 
     * 功能说明：使用HyperLogLog算法统计独立访客，实现高效的去重计数
     * 
     * HyperLogLog算法优势：
     * - 内存占用固定：无论数据量多大，内存占用都是固定的（约12KB）
     * - 高精度估算：标准误差约为0.81%，满足大部分业务需求
     * - 高性能：添加元素和计算基数都是O(1)时间复杂度
     * 
     * 处理流程：
     * 1. 生成用户唯一标识（IP + UserAgent的组合）
     * 2. 从Redis获取或创建HyperLogLog对象
     * 3. 将用户标识添加到HyperLogLog中
     * 4. 序列化并保存到Redis，同时更新数据库
     * 
     * 去重策略：
     * - 使用IP和UserAgent组合作为用户标识
     * - 同一IP+UserAgent在同一天只计算一次UV
     * 
     * @param userIp 用户IP地址
     * @param userAgent 用户代理字符串
     */
    @Override
    @Transactional
    public void recordUniqueVisitor(String userIp, String userAgent) {
        Date today = new Date();
        String dateKey = getDateKey(today);  // 生成日期键
        String uvKey = Const.REDIS_UV_HLL_KEY + dateKey;  // 构造Redis键：visit:uv:hll:2024-01-01
        
        try {
            // 第一步：生成用户唯一标识（IP + UserAgent的哈希值）
            String userIdentifier = generateUserIdentifier(userIp, userAgent);
            
            // 第二步：从Redis获取或创建当天的HyperLogLog对象
            HLL hll = getOrCreateHLL(uvKey);
            
            // 第三步：将用户标识添加到HyperLogLog中（自动去重）
            hll.addRaw(hash(userIdentifier));  // HyperLogLog内部处理重复数据
            
            // 第四步：序列化HyperLogLog并保存到Redis
            byte[] hllBytes = hll.toBytes();  // 序列化为字节数组
            stringRedisTemplate.opsForValue().set(uvKey, Base64.getEncoder().encodeToString(hllBytes));  // Base64编码后保存
            stringRedisTemplate.expire(uvKey, 2, TimeUnit.DAYS);  // 设置过期时间
            
            // 第五步：计算当前UV数量并同步到数据库
            visitStatisticsMapper.insertOrUpdateRecord(today);  // 确保记录存在
            visitStatisticsMapper.updateUniqueVisitors(today, hllBytes, hll.cardinality());  // 更新UV计数和HLL数据
            
            log.debug("记录独立访客: IP={}, UserAgent={}, Identifier={}", userIp, userAgent, userIdentifier);
            
        } catch (Exception e) {
            log.error("记录独立访客失败", e);
        }
    }
    
    /**
     * 根据指定日期获取访问统计数据
     * 
     * 功能说明：获取指定日期的PV和UV统计数据，支持历史数据和当日实时数据
     * 
     * 数据获取策略：
     * 1. 优先从数据库获取历史数据（已持久化的完整数据）
     * 2. 如果是当天且数据库无记录，从Redis获取实时数据
     * 3. 确保当天数据的实时性和历史数据的完整性
     * 
     * 适用场景：
     * - 历史数据查询：获取过往某天的完整统计数据
     * - 当日数据查询：获取今天的实时统计数据
     * - 数据分析：为报表和分析提供准确的日维度数据
     * 
     * @param date 要查询的日期
     * @return VisitStatisticsVO 指定日期的统计数据，无数据时返回null
     */
    @Override
    public VisitStatisticsVO getStatisticsByDate(Date date) {
        // 第一步：优先从数据库获取持久化数据
        VisitStatistics statistics = visitStatisticsMapper.selectByDate(date);
        
        if (statistics == null) {
            // 第二步：如果数据库中没有记录，判断是否为当天
            if (isSameDay(date, new Date())) {
                // 当天数据从Redis获取实时统计
                return getCurrentDayStatistics();
            }
            // 非当天且数据库无记录，返回null
            return null;
        }
        
        // 第三步：将数据库实体转换为VO对象
        VisitStatisticsVO vo = new VisitStatisticsVO();
        BeanUtils.copyProperties(statistics, vo);  // 复制属性值
        return vo;
    }
    
    /**
     * 获取指定时间范围内的访问统计数据
     * 
     * 功能说明：查询指定日期范围内的所有访问统计数据，用于趋势分析和报表生成
     * 
     * 数据来源：
     * - 主要从数据库获取历史数据（已持久化的完整数据）
     * - 数据库中的数据经过定时任务同步，保证数据完整性
     * 
     * 适用场景：
     * - 生成访问趋势报表
     * - 进行数据分析和统计
     * - 查看历史访问情况
     * - 对比不同时间段的访问数据
     * 
     * 性能考虑：
     * - 使用数据库范围查询，支持索引优化
     * - 返回结果按日期排序，便于前端展示
     * - 使用Stream API进行高效的数据转换
     * 
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return List<VisitStatisticsVO> 时间范围内的统计数据列表，按日期排序
     */
    @Override
    public List<VisitStatisticsVO> getStatisticsByDateRange(Date startDate, Date endDate) {
        // 从数据库查询指定日期范围内的统计数据
        List<VisitStatistics> statisticsList = visitStatisticsMapper.selectByDateRange(startDate, endDate);
        
        // 使用Stream API将实体对象转换为VO对象
        return statisticsList.stream().map(statistics -> {
            VisitStatisticsVO vo = new VisitStatisticsVO();
            BeanUtils.copyProperties(statistics, vo);  // 复制属性值
            return vo;
        }).collect(Collectors.toList());  // 收集为List返回
    }
    
    @Override
    public VisitStatisticsSummaryVO getStatisticsSummary() {
        VisitStatisticsSummaryVO summary = new VisitStatisticsSummaryVO();
        Date now = new Date();
        
        // 今日数据
        VisitStatisticsVO today = getCurrentDayStatistics();
        summary.setTodayPv(today.getPageViews());
        summary.setTodayUv(today.getUniqueVisitors());
        
        // 昨日数据
        Date yesterday = getDateBefore(now, 1);
        VisitStatisticsVO yesterdayStats = getStatisticsByDate(yesterday);
        if (yesterdayStats != null) {
            summary.setYesterdayPv(yesterdayStats.getPageViews());
            summary.setYesterdayUv(yesterdayStats.getUniqueVisitors());
        }
        
        // 本周数据
        Date weekStart = getWeekStart(now);
        List<VisitStatisticsVO> weekStats = getStatisticsByDateRange(weekStart, now);
        long weekPv = weekStats.stream().mapToLong(VisitStatisticsVO::getPageViews).sum();
        long weekUv = weekStats.stream().mapToLong(VisitStatisticsVO::getUniqueVisitors).sum();
        summary.setWeekPv(weekPv);
        summary.setWeekUv(weekUv);
        
        // 本月数据
        Date monthStart = getMonthStart(now);
        List<VisitStatisticsVO> monthStats = getStatisticsByDateRange(monthStart, now);
        long monthPv = monthStats.stream().mapToLong(VisitStatisticsVO::getPageViews).sum();
        long monthUv = monthStats.stream().mapToLong(VisitStatisticsVO::getUniqueVisitors).sum();
        summary.setMonthPv(monthPv);
        summary.setMonthUv(monthUv);
        
        // 最近7天和30天数据
        summary.setRecentDays(getRecentStatistics(7));
        summary.setRecentMonth(getRecentStatistics(30));
        
        return summary;
    }
    
    @Override
    public List<VisitStatisticsVO> getRecentStatistics(int days) {
        Date endDate = new Date();
        Date startDate = getDateBefore(endDate, days - 1);
        
        List<VisitStatisticsVO> result = getStatisticsByDateRange(startDate, endDate);
        
        // 如果包含今天，需要从Redis获取今天的实时数据
        if (days >= 1) {
            VisitStatisticsVO todayStats = getCurrentDayStatistics();
            // 移除可能存在的今天数据，添加实时数据
            result.removeIf(vo -> isSameDay(vo.getStatisticsDate(), new Date()));
            result.add(0, todayStats);
        }
        
        return result.stream()
                .sorted((a, b) -> b.getStatisticsDate().compareTo(a.getStatisticsDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取当天实时统计数据
     * 
     * 功能说明：从Redis获取当天的实时PV和UV数据，用于实时统计展示
     * 
     * 数据来源：
     * - PV数据：直接从Redis计数器获取，反映实时页面访问量
     * - UV数据：从Redis中的HyperLogLog序列化数据计算得出
     * 
     * 处理流程：
     * 1. 构造当天的Redis键名
     * 2. 获取PV计数（简单的数值获取）
     * 3. 获取并解析HyperLogLog数据计算UV
     * 4. 异常处理：HLL解析失败时返回0
     * 
     * 性能特点：
     * - 直接从内存获取，响应速度快
     * - 数据实时性强，无延迟
     * - 适用于实时监控和当日统计展示
     * 
     * @return VisitStatisticsVO 包含当天实时PV和UV数据的统计对象
     */
    private VisitStatisticsVO getCurrentDayStatistics() {
        Date today = new Date();
        String dateKey = getDateKey(today);  // 生成日期键：2024-01-01
        
        VisitStatisticsVO vo = new VisitStatisticsVO();
        vo.setStatisticsDate(today);
        
        // 第一步：获取PV数据（页面访问量）
        String pvKey = Const.REDIS_PV_KEY + dateKey;  // 构造PV键：visit:pv:2024-01-01
        String pvValue = stringRedisTemplate.opsForValue().get(pvKey);
        vo.setPageViews(pvValue != null ? Long.parseLong(pvValue) : 0L);  // 解析PV计数，无数据时返回0
        
        // 第二步：获取UV数据（独立访客数）
        String uvKey = Const.REDIS_UV_HLL_KEY + dateKey;  // 构造UV键：visit:uv:hll:2024-01-01
        String hllValue = stringRedisTemplate.opsForValue().get(uvKey);
        if (hllValue != null) {
            try {
                // 解码Base64编码的HyperLogLog数据
                byte[] hllBytes = Base64.getDecoder().decode(hllValue);
                HLL hll = HLL.fromBytes(hllBytes);  // 重建HyperLogLog对象
                vo.setUniqueVisitors(hll.cardinality());  // 计算基数（独立访客数）
            } catch (Exception e) {
                log.error("解析HyperLogLog数据失败", e);
                vo.setUniqueVisitors(0L);  // 解析失败时设为0，保证系统稳定性
            }
        } else {
            vo.setUniqueVisitors(0L);  // Redis中无UV数据时返回0
        }
        
        return vo;
    }
    
    /**
     * 获取或创建HyperLogLog对象
     * 
     * 功能说明：从Redis获取已存在的HLL数据，如果不存在或解析失败则创建新的HLL
     * 
     * 处理流程：
     * 1. 尝试从Redis获取Base64编码的HLL数据
     * 2. 解码并重建HLL对象
     * 3. 如果获取失败或解析异常，创建新的HLL对象
     * 
     * 异常处理：
     * - Base64解码失败：可能是数据损坏
     * - HLL反序列化失败：可能是版本不兼容
     * - 任何异常都会创建新的HLL，保证服务可用性
     * 
     * HLL参数说明：
     * - HLL_LOG2M=14：使用16384个桶，平衡精度和内存
     * - HLL_REGWIDTH=5：每桶5位，支持大数据集统计
     * 
     * @param key Redis中存储HLL数据的键名
     * @return HLL 可用的HyperLogLog对象
     */
    private HLL getOrCreateHLL(String key) {
        String hllValue = stringRedisTemplate.opsForValue().get(key);  // 从Redis获取HLL数据
        if (hllValue != null) {
            try {
                // 尝试解码并重建HLL对象
                byte[] hllBytes = Base64.getDecoder().decode(hllValue);
                return HLL.fromBytes(hllBytes);  // 从字节数组重建HLL
            } catch (Exception e) {
                log.warn("解析HyperLogLog失败，创建新的HLL: {}", e.getMessage());
                // 解析失败时不抛异常，而是创建新的HLL保证服务可用性
            }
        }
        // Redis中无数据或解析失败时，创建新的HLL对象
        return new HLL(HLL_LOG2M, HLL_REGWIDTH);
    }
    
    /**
     * 生成用户唯一标识
     * 
     * 功能说明：将用户IP和UserAgent组合生成唯一标识，用于UV统计的去重
     * 
     * 设计考虑：
     * - 使用IP+UserAgent组合提高去重准确性
     * - 同一用户使用相同浏览器访问时，标识保持一致
     * - 分隔符"|"避免不同组合产生相同标识
     * - 处理UserAgent为null的情况，保证系统稳定性
     * 
     * 示例：
     * - 输入：IP="192.168.1.1", UserAgent="Chrome/91.0"
     * - 输出："192.168.1.1|Chrome/91.0"
     * - 输入：IP="192.168.1.1", UserAgent=null
     * - 输出："192.168.1.1|"
     * 
     * 局限性：
     * - 同一IP不同用户使用相同浏览器时可能被误判为同一访客
     * - 但在大部分场景下能提供足够准确的UV统计
     * 
     * @param userIp 用户IP地址，不能为null
     * @param userAgent 用户代理字符串，可以为null
     * @return String 用户唯一标识字符串
     */
    private String generateUserIdentifier(String userIp, String userAgent) {
        return userIp + "|" + (userAgent != null ? userAgent : "");  // 使用"|"作为分隔符，处理null值
    }
    
    /**
     * 计算字符串的哈希值
     * 
     * 功能说明：将字符串转换为64位长整型哈希值，用于HyperLogLog算法
     * 
     * 算法选择：
     * - 优先使用MD5算法：分布均匀，冲突概率低
     * - 降级使用hashCode：MD5不可用时的备选方案
     * 
     * 处理流程：
     * 1. 使用MD5对输入字符串进行摘要计算
     * 2. 取摘要的前8个字节转换为long类型
     * 3. 如果MD5不可用，使用Java内置的hashCode方法
     * 
     * 字节转换逻辑：
     * - 将8个字节按大端序组合成64位long值
     * - 每个字节左移8位并与前面的结果合并
     * - 使用0xFF掩码确保字节值为正数
     * 
     * 异常处理：
     * - NoSuchAlgorithmException：MD5算法不可用时使用hashCode
     * - 保证在任何环境下都能返回有效的哈希值
     * 
     * @param input 需要计算哈希值的输入字符串
     * @return long 64位哈希值，用于HyperLogLog计算
     */
    private long hash(String input) {
        try {
            // 使用MD5算法计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 取前8个字节转换为long（大端序）
            long hash = 0;
            for (int i = 0; i < 8 && i < hashBytes.length; i++) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);  // 左移8位并合并当前字节
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // 如果MD5不可用，使用简单的哈希作为备选方案
            return input.hashCode();
        }
    }
    
    /**
     * 获取日期键
     * 
     * 功能说明：将Date对象转换为标准的日期字符串，用作Redis键的一部分
     * 
     * 格式说明：
     * - 使用"yyyy-MM-dd"格式，如：2024-01-01
     * - 保证日期格式的一致性，避免键名冲突
     * - 便于Redis键的管理和过期时间设置
     * 
     * 使用场景：
     * - 构造Redis中PV和UV的键名
     * - 确保同一天的数据使用相同的键
     * - 支持按日期进行数据查询和清理
     * 
     * 实现方式：
     * - 使用Java 8的LocalDate API，线程安全
     * - 转换为系统默认时区的本地日期
     * - toString()方法自动生成ISO-8601格式
     * 
     * @param date 需要转换的日期对象
     * @return String 格式化后的日期字符串，格式：yyyy-MM-dd
     */
    private String getDateKey(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.toString();  // 自动生成yyyy-MM-dd格式
    }
    
    /**
     * 判断两个日期是否为同一天
     * 
     * 功能说明：比较两个Date对象是否表示同一个自然日
     * 
     * 比较逻辑：
     * - 将Date转换为LocalDate进行比较
     * - 忽略具体的时分秒，只关注日期部分
     * - 使用系统默认时区进行转换
     * 
     * 优势：
     * - 使用Java 8 API，代码简洁且线程安全
     * - LocalDate.equals()方法已处理各种边界情况
     * - 自动处理闰年、月份天数等复杂逻辑
     * 
     * 使用场景：
     * - 判断查询日期是否为当天
     * - 决定是从Redis还是数据库获取数据
     * - 数据有效性验证
     * 
     * @param date1 第一个日期对象
     * @param date2 第二个日期对象
     * @return boolean 如果两个日期为同一天返回true，否则返回false
     */
    private boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.equals(localDate2);  // LocalDate的equals方法进行精确比较
    }
    
    /**
     * 获取指定天数之前的日期
     * 
     * 功能说明：计算指定日期之前N天的日期，返回当天的开始时间（00:00:00）
     * 
     * 计算逻辑：
     * - 将输入日期转换为LocalDate
     * - 使用minusDays()方法减去指定天数
     * - 转换回Date对象，时间设为当天开始（00:00:00）
     * 
     * 时间处理：
     * - 使用atStartOfDay()确保返回的是当天的开始时间
     * - 避免时分秒对日期范围查询的影响
     * - 使用系统默认时区进行转换
     * 
     * 使用场景：
     * - 计算昨天、前天等历史日期
     * - 生成时间范围查询的开始日期
     * - 统计最近N天的数据范围
     * 
     * @param date 基准日期
     * @param days 要减去的天数（正数）
     * @return Date 指定天数之前的日期，时间为当天开始
     */
    private Date getDateBefore(Date date, int days) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取本周开始日期
     * 
     * 功能说明：计算指定日期所在周的开始日期（周一），返回当天的开始时间
     * 
     * 计算逻辑：
     * - 获取当前日期是周几（1=周一，7=周日）
     * - 减去相应天数回到周一
     * - 例：周三(3) - (3-1) = 周一
     * 
     * 周的定义：
     * - 采用ISO-8601标准，周一为一周的开始
     * - getDayOfWeek().getValue()返回1-7（周一到周日）
     * - 计算公式：当前日期 - (星期几 - 1)
     * 
     * 时间处理：
     * - 返回周一的00:00:00时间
     * - 便于进行周统计的时间范围查询
     * 
     * 使用场景：
     * - 计算本周统计数据的开始日期
     * - 生成周报表的时间范围
     * - 周维度的数据分析
     * 
     * @param date 基准日期
     * @return Date 本周开始日期（周一的00:00:00）
     */
    private Date getWeekStart(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate weekStart = localDate.minusDays(localDate.getDayOfWeek().getValue() - 1);  // 回到周一
        return Date.from(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取本月开始日期
     * 
     * 功能说明：计算指定日期所在月的开始日期（1号），返回当天的开始时间
     * 
     * 计算逻辑：
     * - 使用withDayOfMonth(1)将日期设置为当月1号
     * - 保持年份和月份不变，只修改日期部分
     * - 自动处理不同月份的天数差异
     * 
     * 优势：
     * - withDayOfMonth()方法安全可靠，不会产生无效日期
     * - 自动处理2月、大小月等特殊情况
     * - 代码简洁，逻辑清晰
     * 
     * 时间处理：
     * - 返回当月1号的00:00:00时间
     * - 便于进行月统计的时间范围查询
     * 
     * 使用场景：
     * - 计算本月统计数据的开始日期
     * - 生成月报表的时间范围
     * - 月维度的数据分析和汇总
     * 
     * @param date 基准日期
     * @return Date 本月开始日期（1号的00:00:00）
     */
    private Date getMonthStart(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate monthStart = localDate.withDayOfMonth(1);  // 设置为当月1号
        return Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}