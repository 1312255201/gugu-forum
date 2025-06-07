package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.Activity;
import cn.gugufish.entity.vo.request.ActivityCreateVO;
import cn.gugufish.entity.vo.response.ActivityVO;
import cn.gugufish.mapper.ActivityMapper;
import cn.gugufish.service.ActivityService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl implements ActivityService {
    
    @Resource
    ActivityMapper mapper;
    
    @Override
    public boolean createActivity(int adminId, ActivityCreateVO vo) {
        try {
            Activity activity = new Activity();
            BeanUtils.copyProperties(vo, activity);
            activity.setAdminId(adminId);
            activity.setCurrentParticipants(0);
            activity.setStatus(0); // 默认状态为未开始
            activity.setCreateTime(new Date());
            activity.setUpdateTime(new Date());
            
            // 如果最大参与人数为null，设置为0（不限制）
            if (activity.getMaxParticipants() == null) {
                activity.setMaxParticipants(0);
            }
            
            return mapper.insert(activity) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<ActivityVO> getAllActivities() {
        List<Map<String, Object>> list = mapper.selectAllWithAdminUsername();
        return list.stream().map(this::convertMapToVO).collect(Collectors.toList());
    }
    
    @Override
    public List<ActivityVO> getActivitiesByStatus(Integer status) {
        List<Map<String, Object>> list = mapper.selectByStatusWithAdminUsername(status);
        return list.stream().map(this::convertMapToVO).collect(Collectors.toList());
    }
    
    @Override
    public ActivityVO getActivityById(int id) {
        Map<String, Object> result = mapper.selectByIdWithAdminUsername(id);
        return result != null ? convertMapToVO(result) : null;
    }
    
    @Override
    public boolean updateActivityStatus(int id, int status) {
        UpdateWrapper<Activity> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        wrapper.set("status", status);
        wrapper.set("update_time", new Date());
        return mapper.update(null, wrapper) > 0;
    }
    
    @Override
    public boolean deleteActivity(int id) {
        return mapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean updateActivity(int id, ActivityCreateVO vo) {
        try {
            Activity activity = new Activity();
            BeanUtils.copyProperties(vo, activity);
            activity.setId(id);
            activity.setUpdateTime(new Date());
            
            // 如果最大参与人数为null，设置为0（不限制）
            if (activity.getMaxParticipants() == null) {
                activity.setMaxParticipants(0);
            }
            
            UpdateWrapper<Activity> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);
            return mapper.update(activity, wrapper) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 转换Map为VO
     */
    private ActivityVO convertMapToVO(Map<String, Object> map) {
        ActivityVO vo = new ActivityVO();
        
        // 基本字段映射
        vo.setId((Integer) map.get("id"));
        vo.setAdminId((Integer) map.get("admin_id"));
        vo.setAdminUsername((String) map.get("admin_username"));
        vo.setTitle((String) map.get("title"));
        vo.setContent((String) map.get("content"));
        vo.setLocation((String) map.get("location"));
        vo.setCoverImage((String) map.get("cover_image"));
        vo.setMaxParticipants((Integer) map.get("max_participants"));
        vo.setCurrentParticipants((Integer) map.get("current_participants"));
        vo.setStatus((Integer) map.get("status"));
        
        // 处理时间字段转换
        vo.setActivityTime(convertToDate(map.get("activity_time")));
        vo.setEndTime(convertToDate(map.get("end_time")));
        vo.setCreateTime(convertToDate(map.get("create_time")));
        vo.setUpdateTime(convertToDate(map.get("update_time")));
        
        // 设置状态文本
        Integer status = vo.getStatus();
        if (status != null) {
            switch (status) {
                case 0 -> vo.setStatusText("未开始");
                case 1 -> vo.setStatusText("进行中");
                case 2 -> vo.setStatusText("已结束");
                case 3 -> vo.setStatusText("已取消");
                default -> vo.setStatusText("未知状态");
            }
        }
        
        return vo;
    }
    
    /**
     * 将时间对象转换为Date类型
     */
    private Date convertToDate(Object timeObj) {
        if (timeObj == null) {
            return null;
        }
        
        if (timeObj instanceof Date) {
            return (Date) timeObj;
        } else if (timeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (timeObj instanceof java.sql.Timestamp) {
            return new Date(((java.sql.Timestamp) timeObj).getTime());
        }
        
        return null;
    }
} 