package cn.gugufish.mapper;

import cn.gugufish.entity.dto.LoveWall;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 表白墙数据访问层
 */
@Mapper
public interface LoveWallMapper extends BaseMapper<LoveWall> {
    
    /**
     * 查询已审核通过的表白墙信息
     */
    @Select("SELECT * FROM db_love_wall WHERE status = 1 ORDER BY create_time DESC")
    List<LoveWall> selectApprovedList();
    
    /**
     * 根据性别筛选已审核通过的表白墙信息
     */
    @Select("SELECT * FROM db_love_wall WHERE status = 1 AND gender = #{gender} ORDER BY create_time DESC")
    List<LoveWall> selectApprovedListByGender(@Param("gender") Integer gender);
    
    /**
     * 根据年龄范围筛选已审核通过的表白墙信息
     */
    @Select("SELECT * FROM db_love_wall WHERE status = 1 AND age BETWEEN #{minAge} AND #{maxAge} ORDER BY create_time DESC")
    List<LoveWall> selectApprovedListByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    /**
     * 查询用户自己发布的表白墙信息
     */
    @Select("SELECT * FROM db_love_wall WHERE uid = #{uid} ORDER BY create_time DESC")
    List<LoveWall> selectByUid(@Param("uid") Integer uid);
    
    /**
     * 点赞数+1
     */
    @Update("UPDATE db_love_wall SET like_count = like_count + 1 WHERE id = #{id}")
    int increaseLikeCount(@Param("id") Integer id);
    
    /**
     * 查询所有待审核的表白墙（管理员用）
     */
    @Select("SELECT * FROM db_love_wall WHERE status = 0 ORDER BY create_time ASC")
    List<LoveWall> selectPendingList();
} 