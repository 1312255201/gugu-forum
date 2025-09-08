package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.VisitStatisticsVO;
import cn.gugufish.entity.vo.response.VisitStatisticsSummaryVO;
import cn.gugufish.service.VisitStatisticsService;
import cn.gugufish.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 访问统计控制器
 * 提供UV/PV统计相关的RESTful API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "访问统计管理", description = "UV/PV统计数据管理相关接口")
public class VisitStatisticsController {
    
    @Resource
    private VisitStatisticsService visitStatisticsService;
    
    /**
     * 记录页面访问
     * 每次页面访问时调用此接口记录PV和UV
     */
    @PostMapping("/visit")
    @Operation(summary = "记录页面访问", description = "记录用户的页面访问，用于统计PV和UV")
    public RestBean<Void> recordVisit(HttpServletRequest request) {
        try {
            String userIp = IpUtils.getRealClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 记录页面访问（同时会记录UV）
            visitStatisticsService.recordPageView(userIp, userAgent);
            
            return RestBean.success();
        } catch (Exception e) {
            log.error("记录页面访问失败", e);
            return RestBean.failure(500, "记录访问失败");
        }
    }
    
    /**
     * 获取统计汇总数据
     * 包含今日、昨日、本周、本月的PV/UV数据，以及最近7天和30天的趋势数据
     */
    @GetMapping("/summary")
    @Operation(summary = "获取统计汇总", description = "获取包含各时间段PV/UV统计的汇总数据")
    public RestBean<VisitStatisticsSummaryVO> getStatisticsSummary() {
        try {
            VisitStatisticsSummaryVO summary = visitStatisticsService.getStatisticsSummary();
            return RestBean.success(summary);
        } catch (Exception e) {
            log.error("获取统计汇总失败", e);
            return RestBean.failure(500, "获取统计数据失败");
        }
    }
    
    /**
     * 获取指定日期的统计数据
     */
    @GetMapping("/date")
    @Operation(summary = "获取指定日期统计", description = "获取指定日期的PV/UV统计数据")
    public RestBean<VisitStatisticsVO> getStatisticsByDate(
            @Parameter(description = "统计日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            VisitStatisticsVO statistics = visitStatisticsService.getStatisticsByDate(date);
            if (statistics == null) {
                return RestBean.failure(404, "未找到指定日期的统计数据");
            }
            return RestBean.success(statistics);
        } catch (Exception e) {
            log.error("获取指定日期统计失败", e);
            return RestBean.failure(500, "获取统计数据失败");
        }
    }
    
    /**
     * 获取日期范围内的统计数据
     */
    @GetMapping("/range")
    @Operation(summary = "获取日期范围统计", description = "获取指定日期范围内的PV/UV统计数据")
    public RestBean<List<VisitStatisticsVO>> getStatisticsByDateRange(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<VisitStatisticsVO> statistics = visitStatisticsService.getStatisticsByDateRange(startDate, endDate);
            return RestBean.success(statistics);
        } catch (Exception e) {
            log.error("获取日期范围统计失败", e);
            return RestBean.failure(500, "获取统计数据失败");
        }
    }
    
    /**
     * 获取最近N天的统计数据
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近N天统计", description = "获取最近N天的PV/UV统计数据")
    public RestBean<List<VisitStatisticsVO>> getRecentStatistics(
            @Parameter(description = "天数，默认7天")
            @RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 365) {
                return RestBean.failure(400, "天数参数无效，应在1-365之间");
            }
            
            List<VisitStatisticsVO> statistics = visitStatisticsService.getRecentStatistics(days);
            return RestBean.success(statistics);
        } catch (Exception e) {
            log.error("获取最近{}天统计失败", days, e);
            return RestBean.failure(500, "获取统计数据失败");
        }
    }
    

}