package cn.gugufish.entity.dto;

import cn.gugufish.entity.BaseData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 访问统计实体类
 * 用于存储UV/PV统计数据
 */
@Data
@TableName("db_visit_statistics")
@AllArgsConstructor
@NoArgsConstructor
public class VisitStatistics implements BaseData {
    
    @TableId(type = IdType.AUTO)
    Integer id;
    
    /**
     * 统计日期
     */
    Date statisticsDate;
    
    /**
     * 页面访问量(PV)
     */
    Long pageViews;
    
    /**
     * 独立访客数(UV) - HyperLogLog序列化数据
     */
    byte[] uniqueVisitorsHll;
    
    /**
     * 独立访客数估算值
     */
    Long uniqueVisitors;
    
    /**
     * 创建时间
     */
    Date createTime;
    
    /**
     * 更新时间
     */
    Date updateTime;
}