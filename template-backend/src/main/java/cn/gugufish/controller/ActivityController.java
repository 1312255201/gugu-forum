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
 * 校园活动控制器
 */
@Tag(name = "校园活动相关", description = "用户查看校园活动相关接口。")
@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    
    /**
     * 活动服务类
     * 提供活动相关的业务逻辑处理
     */
    @Resource
    ActivityService activityService;
    
    /**
     * 获取所有活动列表
     * 返回系统中的全部活动信息，包括已发布、进行中和已结束的活动
     * 
     * @return 包含所有活动信息的响应结果
     */
    @Operation(summary = "获取所有活动列表")
    @GetMapping("/list")
    public RestBean<List<ActivityVO>> getAllActivities() {
        List<ActivityVO> list = activityService.getAllActivities();
        return RestBean.success(list);
    }
    
    /**
     * 根据活动状态获取活动列表
     * 支持按状态筛选活动，如：
     * - 0: 未开始
     * - 1: 进行中  
     * - 2: 已结束
     * 
     * @param status 活动状态码
     * @return 包含指定状态活动列表的响应结果
     */
    @Operation(summary = "根据状态获取活动列表")
    @GetMapping("/list/{status}")
    public RestBean<List<ActivityVO>> getActivitiesByStatus(@PathVariable Integer status) {
        List<ActivityVO> list = activityService.getActivitiesByStatus(status);
        return RestBean.success(list);
    }
    
    /**
     * 获取指定活动的详细信息
     * 根据活动ID查询活动的完整信息，包括活动描述、时间、地点等详细内容
     * 
     * @param id 活动的唯一标识ID
     * @return 包含活动详细信息的响应结果，如果活动不存在则返回404错误
     */
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public RestBean<ActivityVO> getActivityById(@PathVariable int id) {
        ActivityVO activity = activityService.getActivityById(id);
        return activity != null ? RestBean.success(activity) : RestBean.failure(404, "未找到该活动");
    }
} 