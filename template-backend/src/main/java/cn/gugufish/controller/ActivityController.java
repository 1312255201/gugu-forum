package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.ActivityVO;
import cn.gugufish.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校园活动相关操作接口（用户端）
 */
@Tag(name = "校园活动相关", description = "用户查看校园活动相关接口。")
@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    
    @Resource
    ActivityService activityService;
    
    /**
     * 获取所有活动列表
     */
    @Operation(summary = "获取所有活动列表")
    @GetMapping("/list")
    public RestBean<List<ActivityVO>> getAllActivities() {
        List<ActivityVO> list = activityService.getAllActivities();
        return RestBean.success(list);
    }
    
    /**
     * 根据状态获取活动列表
     */
    @Operation(summary = "根据状态获取活动列表")
    @GetMapping("/list/{status}")
    public RestBean<List<ActivityVO>> getActivitiesByStatus(@PathVariable Integer status) {
        List<ActivityVO> list = activityService.getActivitiesByStatus(status);
        return RestBean.success(list);
    }
    
    /**
     * 获取活动详情
     */
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public RestBean<ActivityVO> getActivityById(@PathVariable int id) {
        ActivityVO activity = activityService.getActivityById(id);
        return activity != null ? RestBean.success(activity) : RestBean.failure(404, "未找到该活动");
    }
} 