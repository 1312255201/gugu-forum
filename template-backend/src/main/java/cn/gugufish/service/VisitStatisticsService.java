package cn.gugufish.service;

import cn.gugufish.entity.dto.VisitStatistics;
import cn.gugufish.entity.vo.response.VisitStatisticsVO;
import cn.gugufish.entity.vo.response.VisitStatisticsSummaryVO;

import java.util.Date;
import java.util.List;

/**
 * 访问统计服务接口
 */
public interface VisitStatisticsService {
    
    /**
     * 记录页面访问(PV)
     * @param userIp 用户IP地址
     * @param userAgent 用户代理
     */
    void recordPageView(String userIp, String userAgent);
    
    /**
     * 记录独立访客(UV)
     * @param userIp 用户IP地址
     * @param userAgent 用户代理
     */
    void recordUniqueVisitor(String userIp, String userAgent);
    
    /**
     * 获取指定日期的统计数据
     * @param date 统计日期
     * @return 统计数据
     */
    VisitStatisticsVO getStatisticsByDate(Date date);
    
    /**
     * 获取指定日期范围的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<VisitStatisticsVO> getStatisticsByDateRange(Date startDate, Date endDate);
    
    /**
     * 获取统计汇总数据
     * @return 汇总统计数据
     */
    VisitStatisticsSummaryVO getStatisticsSummary();
    
    /**
     * 获取最近N天的统计数据
     * @param days 天数
     * @return 统计数据列表
     */
    List<VisitStatisticsVO> getRecentStatistics(int days);
}