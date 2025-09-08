package cn.gugufish.mapper;

import cn.gugufish.entity.dto.VisitStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 访问统计Mapper接口
 */
@Mapper
public interface VisitStatisticsMapper extends BaseMapper<VisitStatistics> {
    
    /**
     * 根据日期查询统计数据
     * @param date 统计日期
     * @return 统计数据
     */
    @Select("SELECT * FROM db_visit_statistics WHERE DATE(statistics_date) = DATE(#{date})")
    VisitStatistics selectByDate(@Param("date") Date date);
    
    /**
     * 查询指定日期范围内的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    @Select("SELECT * FROM db_visit_statistics WHERE DATE(statistics_date) BETWEEN DATE(#{startDate}) AND DATE(#{endDate}) ORDER BY statistics_date DESC")
    List<VisitStatistics> selectByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 插入或更新PV记录
     * @param date 统计日期
     */
    @Insert("INSERT INTO db_visit_statistics (statistics_date, page_views, unique_visitors, create_time, update_time) " +
            "VALUES (DATE(#{date}), 0, 0, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE update_time = NOW()")
    void insertOrUpdateRecord(@Param("date") Date date);
    
    /**
     * 更新PV计数
     * @param date 统计日期
     * @param increment 增量
     */
    @Update("UPDATE db_visit_statistics SET page_views = page_views + #{increment}, update_time = NOW() WHERE DATE(statistics_date) = DATE(#{date})")
    void incrementPageViews(@Param("date") Date date, @Param("increment") long increment);
    
    /**
     * 更新UV数据
     * @param date 统计日期
     * @param hllData HyperLogLog序列化数据
     * @param uvCount UV估算值
     */
    @Update("UPDATE db_visit_statistics SET unique_visitors_hll = #{hllData}, unique_visitors = #{uvCount}, update_time = NOW() WHERE DATE(statistics_date) = DATE(#{date})")
    void updateUniqueVisitors(@Param("date") Date date, @Param("hllData") byte[] hllData, @Param("uvCount") long uvCount);
    
    /**
     * 更新PV计数（设置绝对值）
     * @param date 统计日期
     * @param pageViews PV总数
     */
    @Update("UPDATE db_visit_statistics SET page_views = #{pageViews}, update_time = NOW() WHERE DATE(statistics_date) = DATE(#{date})")
    void updatePvCount(@Param("date") Date date, @Param("pageViews") long pageViews);
    
    /**
     * 更新UV数据（别名方法）
     * @param date 统计日期
     * @param hllData HyperLogLog序列化数据
     * @param uvCount UV估算值
     */
    @Update("UPDATE db_visit_statistics SET unique_visitors_hll = #{hllData}, unique_visitors = #{uvCount}, update_time = NOW() WHERE DATE(statistics_date) = DATE(#{date})")
    void updateUvData(@Param("date") Date date, @Param("hllData") byte[] hllData, @Param("uvCount") long uvCount);
}