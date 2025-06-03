package cn.gugufish.mapper;

import cn.gugufish.entity.dto.LostFound;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 失物招领数据访问层
 */
@Mapper
public interface LostFoundMapper extends BaseMapper<LostFound> {
    
    /**
     * 根据条件查询失物招领信息
     */
    @Select("""
        <script>
        SELECT lf.*, u.username FROM db_lost_found lf 
        LEFT JOIN db_account u ON lf.uid = u.id
        WHERE 1=1
        <if test="location != null and location != ''">
            AND lf.location LIKE CONCAT('%', #{location}, '%')
        </if>
        <if test="startTime != null">
            AND lf.lost_time &gt;= #{startTime}
        </if>
        <if test="endTime != null">
            AND lf.lost_time &lt;= #{endTime}
        </if>
        <if test="status != null">
            AND lf.status = #{status}
        </if>
        ORDER BY lf.create_time DESC
        </script>
    """)
    List<Map<String, Object>> selectByConditionsWithUsername(@Param("location") String location,
                                                             @Param("startTime") Date startTime,
                                                             @Param("endTime") Date endTime,
                                                             @Param("status") Integer status);
    
    /**
     * 根据ID查询失物招领详情（包含用户名）
     */
    @Select("SELECT lf.*, u.username FROM db_lost_found lf LEFT JOIN db_account u ON lf.uid = u.id WHERE lf.id = #{id}")
    Map<String, Object> selectByIdWithUsername(@Param("id") Integer id);
} 