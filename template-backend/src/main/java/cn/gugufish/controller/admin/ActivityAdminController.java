package cn.gugufish.controller.admin;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.request.ActivityCreateVO;
import cn.gugufish.entity.vo.response.ActivityVO;
import cn.gugufish.service.ActivityService;
import cn.gugufish.utils.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动管理相关接口（管理员端）
 */
@Tag(name = "活动管理相关", description = "管理员管理校园活动相关接口。")
@RestController
@RequestMapping("/api/admin/activity")
public class ActivityAdminController {
    
    @Resource
    ActivityService activityService;
    
    /**
     * 创建活动
     */
    @Operation(summary = "创建新活动")
    @PostMapping("/create")
    public RestBean<Void> createActivity(@RequestAttribute(Const.ATTR_USER_ID) int adminId,
                                        @RequestBody @Valid ActivityCreateVO vo) {
        boolean success = activityService.createActivity(adminId, vo);
        return success ? RestBean.success() : RestBean.failure(400, "创建活动失败");
    }
    
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
     * 获取活动详情
     */
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public RestBean<ActivityVO> getActivityById(@PathVariable int id) {
        ActivityVO activity = activityService.getActivityById(id);
        return activity != null ? RestBean.success(activity) : RestBean.failure(404, "未找到该活动");
    }
    
    /**
     * 更新活动信息
     */
    @Operation(summary = "更新活动信息")
    @PostMapping("/{id}/update")
    public RestBean<Void> updateActivity(@PathVariable int id,
                                        @RequestBody @Valid ActivityCreateVO vo) {
        boolean success = activityService.updateActivity(id, vo);
        return success ? RestBean.success() : RestBean.failure(400, "更新活动失败");
    }
    
    /**
     * 更新活动状态
     */
    @Operation(summary = "更新活动状态")
    @PostMapping("/{id}/status")
    public RestBean<Void> updateActivityStatus(@PathVariable int id,
                                              @RequestBody Map<String, Object> requestBody) {
        Integer status = (Integer) requestBody.get("status");
        if (status == null) {
            return RestBean.failure(400, "状态参数不能为空");
        }
        boolean success = activityService.updateActivityStatus(id, status);
        return success ? RestBean.success() : RestBean.failure(400, "更新状态失败");
    }
    
    /**
     * 删除活动
     */
    @Operation(summary = "删除活动")
    @PostMapping("/{id}/delete")
    public RestBean<Void> deleteActivity(@PathVariable int id) {
        boolean success = activityService.deleteActivity(id);
        return success ? RestBean.success() : RestBean.failure(400, "删除活动失败");
    }
} 