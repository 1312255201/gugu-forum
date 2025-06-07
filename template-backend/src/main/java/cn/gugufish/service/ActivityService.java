package cn.gugufish.service;

import cn.gugufish.entity.vo.request.ActivityCreateVO;
import cn.gugufish.entity.vo.response.ActivityVO;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
    
    /**
     * 创建活动（仅管理员）
     */
    boolean createActivity(int adminId, ActivityCreateVO vo);
    
    /**
     * 获取所有活动列表
     */
    List<ActivityVO> getAllActivities();
    
    /**
     * 根据状态获取活动列表
     */
    List<ActivityVO> getActivitiesByStatus(Integer status);
    
    /**
     * 根据ID获取活动详情
     */
    ActivityVO getActivityById(int id);
    
    /**
     * 更新活动状态（仅管理员）
     */
    boolean updateActivityStatus(int id, int status);
    
    /**
     * 删除活动（仅管理员）
     */
    boolean deleteActivity(int id);
    
    /**
     * 更新活动信息（仅管理员）
     */
    boolean updateActivity(int id, ActivityCreateVO vo);
} 