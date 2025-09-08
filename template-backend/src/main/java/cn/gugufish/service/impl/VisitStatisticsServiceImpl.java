package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.VisitStatistics;
import cn.gugufish.entity.vo.response.VisitStatisticsVO;
import cn.gugufish.entity.vo.response.VisitStatisticsSummaryVO;
import cn.gugufish.mapper.VisitStatisticsMapper;
import cn.gugufish.service.VisitStatisticsService;
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
 */
@Slf4j
@Service
public class VisitStatisticsServiceImpl implements VisitStatisticsService {
    
    @Resource
    private VisitStatisticsMapper visitStatisticsMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    // HyperLogLog参数配置
    private static final int HLL_LOG2M = 14; // 2^14 = 16384 buckets
    private static final int HLL_REGWIDTH = 5; // 5 bits per bucket
    
    // Redis缓存键前缀
    private static final String REDIS_PV_KEY = "visit:pv:";
    private static final String REDIS_UV_HLL_KEY = "visit:uv:hll:";
    
    @Override
    @Transactional
    public void recordPageView(String userIp, String userAgent) {
        Date today = new Date();
        String dateKey = getDateKey(today);
        
        // 增加Redis中的PV计数
        String pvKey = REDIS_PV_KEY + dateKey;
        stringRedisTemplate.opsForValue().increment(pvKey);
        stringRedisTemplate.expire(pvKey, 2, TimeUnit.DAYS);
        
        // 同时记录UV
        recordUniqueVisitor(userIp, userAgent);
        
        // 确保数据库记录存在，然后更新PV计数
        visitStatisticsMapper.insertOrUpdateRecord(today);
        visitStatisticsMapper.incrementPageViews(today, 1L);
        
        log.debug("记录页面访问: IP={}, UserAgent={}", userIp, userAgent);
    }
    
    @Override
    @Transactional
    public void recordUniqueVisitor(String userIp, String userAgent) {
        Date today = new Date();
        String dateKey = getDateKey(today);
        String uvKey = REDIS_UV_HLL_KEY + dateKey;
        
        try {
            // 生成用户唯一标识
            String userIdentifier = generateUserIdentifier(userIp, userAgent);
            
            // 从Redis获取或创建HyperLogLog
            HLL hll = getOrCreateHLL(uvKey);
            
            // 添加用户标识到HyperLogLog
            hll.addRaw(hash(userIdentifier));
            
            // 保存更新后的HyperLogLog到Redis
            byte[] hllBytes = hll.toBytes();
            stringRedisTemplate.opsForValue().set(uvKey, Base64.getEncoder().encodeToString(hllBytes));
            stringRedisTemplate.expire(uvKey, 2, TimeUnit.DAYS);
            
            // 同步UV数据到数据库
            visitStatisticsMapper.insertOrUpdateRecord(today);
            visitStatisticsMapper.updateUniqueVisitors(today, hllBytes, hll.cardinality());
            
            log.debug("记录独立访客: IP={}, UserAgent={}, Identifier={}", userIp, userAgent, userIdentifier);
            
        } catch (Exception e) {
            log.error("记录独立访客失败", e);
        }
    }
    
    @Override
    public VisitStatisticsVO getStatisticsByDate(Date date) {
        // 先尝试从数据库获取
        VisitStatistics statistics = visitStatisticsMapper.selectByDate(date);
        
        if (statistics == null) {
            // 如果数据库中没有，尝试从Redis获取当天数据
            if (isSameDay(date, new Date())) {
                return getCurrentDayStatistics();
            }
            return null;
        }
        
        VisitStatisticsVO vo = new VisitStatisticsVO();
        BeanUtils.copyProperties(statistics, vo);
        return vo;
    }
    
    @Override
    public List<VisitStatisticsVO> getStatisticsByDateRange(Date startDate, Date endDate) {
        List<VisitStatistics> statisticsList = visitStatisticsMapper.selectByDateRange(startDate, endDate);
        
        return statisticsList.stream().map(statistics -> {
            VisitStatisticsVO vo = new VisitStatisticsVO();
            BeanUtils.copyProperties(statistics, vo);
            return vo;
        }).collect(Collectors.toList());
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
     */
    private VisitStatisticsVO getCurrentDayStatistics() {
        Date today = new Date();
        String dateKey = getDateKey(today);
        
        VisitStatisticsVO vo = new VisitStatisticsVO();
        vo.setStatisticsDate(today);
        
        // 获取PV
        String pvKey = REDIS_PV_KEY + dateKey;
        String pvValue = stringRedisTemplate.opsForValue().get(pvKey);
        vo.setPageViews(pvValue != null ? Long.parseLong(pvValue) : 0L);
        
        // 获取UV
        String uvKey = REDIS_UV_HLL_KEY + dateKey;
        String hllValue = stringRedisTemplate.opsForValue().get(uvKey);
        if (hllValue != null) {
            try {
                byte[] hllBytes = Base64.getDecoder().decode(hllValue);
                HLL hll = HLL.fromBytes(hllBytes);
                vo.setUniqueVisitors(hll.cardinality());
            } catch (Exception e) {
                log.error("解析HyperLogLog数据失败", e);
                vo.setUniqueVisitors(0L);
            }
        } else {
            vo.setUniqueVisitors(0L);
        }
        
        return vo;
    }
    
    /**
     * 获取或创建HyperLogLog
     */
    private HLL getOrCreateHLL(String key) {
        String hllValue = stringRedisTemplate.opsForValue().get(key);
        if (hllValue != null) {
            try {
                byte[] hllBytes = Base64.getDecoder().decode(hllValue);
                return HLL.fromBytes(hllBytes);
            } catch (Exception e) {
                log.warn("解析HyperLogLog失败，创建新的HLL: {}", e.getMessage());
            }
        }
        return new HLL(HLL_LOG2M, HLL_REGWIDTH);
    }
    
    /**
     * 生成用户唯一标识
     */
    private String generateUserIdentifier(String userIp, String userAgent) {
        return userIp + "|" + (userAgent != null ? userAgent : "");
    }
    
    /**
     * 计算字符串的哈希值
     */
    private long hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 取前8个字节转换为long
            long hash = 0;
            for (int i = 0; i < 8 && i < hashBytes.length; i++) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // 如果MD5不可用，使用简单的哈希
            return input.hashCode();
        }
    }
    
    /**
     * 获取日期键
     */
    private String getDateKey(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.toString();
    }
    
    /**
     * 判断两个日期是否为同一天
     */
    private boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.equals(localDate2);
    }
    
    /**
     * 获取指定天数之前的日期
     */
    private Date getDateBefore(Date date, int days) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取本周开始日期
     */
    private Date getWeekStart(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate weekStart = localDate.minusDays(localDate.getDayOfWeek().getValue() - 1);
        return Date.from(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取本月开始日期
     */
    private Date getMonthStart(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate monthStart = localDate.withDayOfMonth(1);
        return Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}