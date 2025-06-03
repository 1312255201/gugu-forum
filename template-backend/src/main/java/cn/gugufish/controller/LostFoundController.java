package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.request.LostFoundCreateVO;
import cn.gugufish.entity.vo.response.LostFoundVO;
import cn.gugufish.service.LostFoundService;
import cn.gugufish.utils.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 失物招领相关操作接口
 */
@Tag(name = "失物招领相关", description = "包括失物招领的发布、查询、修改等操作。")
@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {
    
    @Resource
    LostFoundService lostFoundService;
    
    /**
     * 创建失物招领信息
     */
    @Operation(summary = "发布失物招领信息")
    @PostMapping("/create")
    public RestBean<Void> createLostFound(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                         @RequestBody @Valid LostFoundCreateVO vo) {
        boolean success = lostFoundService.createLostFound(uid, vo);
        return success ? RestBean.success() : RestBean.failure(400, "发布失败");
    }
    
    /**
     * 获取失物招领列表
     */
    @Operation(summary = "获取失物招领列表")
    @GetMapping("/list")
    public RestBean<List<LostFoundVO>> getLostFoundList(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false) Integer status) {
        List<LostFoundVO> list = lostFoundService.getLostFoundList(location, startTime, endTime, status);
        return RestBean.success(list);
    }
    
    /**
     * 获取失物招领详情
     */
    @Operation(summary = "获取失物招领详情")
    @GetMapping("/{id}")
    public RestBean<LostFoundVO> getLostFoundById(@PathVariable int id) {
        LostFoundVO lostFound = lostFoundService.getLostFoundById(id);
        return lostFound != null ? RestBean.success(lostFound) : RestBean.failure(404, "未找到该信息");
    }
    
    /**
     * 更新失物招领状态
     */
    @Operation(summary = "更新失物招领状态")
    @PostMapping("/{id}/status")
    public RestBean<Void> updateStatus(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                      @PathVariable int id,
                                      @RequestBody Map<String, Object> requestBody) {
        Integer status = (Integer) requestBody.get("status");
        if (status == null) {
            return RestBean.failure(400, "状态参数不能为空");
        }
        boolean success = lostFoundService.updateStatus(id, uid, status);
        return success ? RestBean.success() : RestBean.failure(400, "更新失败");
    }
    
    /**
     * 删除失物招领
     */
    @Operation(summary = "删除失物招领")
    @PostMapping("/{id}/delete")
    public RestBean<Void> deleteLostFound(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                         @PathVariable int id) {
        boolean success = lostFoundService.deleteLostFound(id, uid);
        return success ? RestBean.success() : RestBean.failure(400, "删除失败");
    }
} 