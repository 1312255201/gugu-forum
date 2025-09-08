package cn.gugufish.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 访问统计响应VO
 */
@Data
public class VisitStatisticsVO {
    
    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    Date statisticsDate;
    
    /**
     * 页面访问量(PV)
     */
    Long pageViews;
    
    /**
     * 独立访客数(UV)
     */
    Long uniqueVisitors;
}