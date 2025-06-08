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
 * 失物招领控制器
 * 
 * @author GuguFish
 */
@Tag(name = "失物招领相关", description = "包括失物招领的发布、查询、修改等操作。")
@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {
    
    /**
     * 失物招领服务类
     * 提供失物招领相关的业务逻辑处理
     */
    @Resource
    LostFoundService lostFoundService;
    
    /**
     * 创建失物招领信息
     * 用户可以发布两种类型的信息：
     * - 失物信息：我丢了什么东西
     * - 招领信息：我捡到了什么东西
     * 
     * @param uid 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 失物招领创建请求对象，包含物品描述、地点、时间等信息
     * @return 创建结果，成功返回成功响应，失败返回错误信息
     */
    @Operation(summary = "发布失物招领信息")
    @PostMapping("/create")
    public RestBean<Void> createLostFound(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                         @RequestBody @Valid LostFoundCreateVO vo) {
        boolean success = lostFoundService.createLostFound(uid, vo);
        return success ? RestBean.success() : RestBean.failure(400, "发布失败");
    }
    
    /**
     * 获取失物招领列表（支持多条件筛选）
     * 支持以下筛选条件的组合查询：
     * - 地点筛选：查找特定地点的失物招领信息
     * - 时间范围：查找指定时间段内的信息
     * - 状态筛选：未解决/已解决的物品信息
     * 
     * @param location 可选，物品丢失或捡到的地点
     * @param startTime 可选，查询开始时间（格式：yyyy-MM-dd）
     * @param endTime 可选，查询结束时间（格式：yyyy-MM-dd）
     * @param status 可选，状态筛选（0:未解决, 1:已解决）
     * @return 符合条件的失物招领列表
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
     * 获取失物招领详细信息
     * 根据失物招领ID查询完整的详细信息，包括物品描述、联系方式等
     * 
     * @param id 失物招领记录的唯一标识ID
     * @return 失物招领的详细信息，如果记录不存在则返回404错误
     */
    @Operation(summary = "获取失物招领详情")
    @GetMapping("/{id}")
    public RestBean<LostFoundVO> getLostFoundById(@PathVariable int id) {
        LostFoundVO lostFound = lostFoundService.getLostFoundById(id);
        return lostFound != null ? RestBean.success(lostFound) : RestBean.failure(404, "未找到该信息");
    }
    
    /**
     * 更新失物招领状态
     * 物品找到或归还后，可以更新状态为"已解决"
     * 只有发布者本人可以更新自己发布的信息状态
     * 
     * @param uid 当前登录用户的ID（从JWT令牌中获取）
     * @param id 失物招领记录的ID
     * @param requestBody 包含状态信息的请求体（status字段）
     * @return 更新结果，成功返回成功响应，失败返回错误信息
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
     * 删除失物招领信息
     * 用户可以删除自己发布的失物招领信息
     * 
     * @param uid 当前登录用户的ID（从JWT令牌中获取）
     * @param id 要删除的失物招领记录ID
     * @return 删除结果，成功返回成功响应，失败返回错误信息
     */
    @Operation(summary = "删除失物招领")
    @PostMapping("/{id}/delete")
    public RestBean<Void> deleteLostFound(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                         @PathVariable int id) {
        boolean success = lostFoundService.deleteLostFound(id, uid);
        return success ? RestBean.success() : RestBean.failure(400, "删除失败");
    }
} 