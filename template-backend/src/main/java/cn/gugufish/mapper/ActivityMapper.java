package cn.gugufish.mapper;

import cn.gugufish.entity.dto.Activity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 活动数据访问层
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    /**
     * 查询所有活动信息（包含管理员用户名）
     */
    @Select("SELECT a.*, u.username as admin_username FROM db_activity a " +
            "LEFT JOIN db_account u ON a.admin_id = u.id " +
            "ORDER BY a.create_time DESC")
    List<Map<String, Object>> selectAllWithAdminUsername();
    
    /**
     * 根据ID查询活动详情（包含管理员用户名）
     */
    @Select("SELECT a.*, u.username as admin_username FROM db_activity a " +
            "LEFT JOIN db_account u ON a.admin_id = u.id " +
            "WHERE a.id = #{id}")
    Map<String, Object> selectByIdWithAdminUsername(@Param("id") Integer id);
    
    /**
     * 根据状态查询活动
     */
    @Select("SELECT a.*, u.username as admin_username FROM db_activity a " +
            "LEFT JOIN db_account u ON a.admin_id = u.id " +
            "WHERE a.status = #{status} " +
            "ORDER BY a.create_time DESC")
    List<Map<String, Object>> selectByStatusWithAdminUsername(@Param("status") Integer status);
} 