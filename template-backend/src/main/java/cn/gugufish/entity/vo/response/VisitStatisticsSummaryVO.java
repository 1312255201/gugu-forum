package cn.gugufish.entity.vo.response;

import lombok.Data;

import java.util.List;

/**
 * 访问统计汇总响应VO
 */
@Data
public class VisitStatisticsSummaryVO {

    /**
     * 今日PV
     */
    Long todayPv;

    /**
     * 今日UV
     */
    Long todayUv;

    /**
     * 昨日PV
     */
    Long yesterdayPv;

    /**
     * 昨日UV
     */
    Long yesterdayUv;

    /**
     * 本周PV
     */
    Long weekPv;

    /**
     * 本周UV
     */
    Long weekUv;

    /**
     * 本月PV
     */
    Long monthPv;

    /**
     * 本月UV
     */
    Long monthUv;

    /**
     * 最近7天统计数据
     */
    List<VisitStatisticsVO> recentDays;

    /**
     * 最近30天统计数据
     */
    List<VisitStatisticsVO> recentMonth;
}