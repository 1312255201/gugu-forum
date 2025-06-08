package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.request.LoveWallCreateVO;
import cn.gugufish.entity.vo.response.LoveWallVO;
import cn.gugufish.service.LoveWallService;
import cn.gugufish.utils.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙控制器
 */
@Tag(name = "表白墙相关", description = "表白墙相关接口。")
@RestController
@RequestMapping("/api/love-wall")
public class LoveWallController {
    
    @Resource
    LoveWallService loveWallService;
    
    /**
     * 创建表白墙信息
     */
    @Operation(summary = "创建表白墙信息")
    @PostMapping("/create")
    public RestBean<Void> createLoveWall(@Valid @RequestBody LoveWallCreateVO vo, HttpServletRequest request) {
        int uid = (int) request.getAttribute(Const.ATTR_USER_ID);
        return loveWallService.createLoveWall(uid, vo) ? 
            RestBean.success() : RestBean.failure(400, "发布失败");
    }
    
    /**
     * 获取已审核通过的表白墙列表
     */
    @Operation(summary = "获取表白墙列表")
    @GetMapping("/list")
    public RestBean<List<LoveWallVO>> getLoveWallList(HttpServletRequest request) {
        Integer uid = (Integer) request.getAttribute(Const.ATTR_USER_ID);
        List<LoveWallVO> list = loveWallService.getApprovedLoveWallList(uid);
        return RestBean.success(list);
    }
    
    /**
     * 根据性别筛选表白墙列表
     */
    @Operation(summary = "根据性别筛选表白墙列表")
    @GetMapping("/list/gender/{gender}")
    public RestBean<List<LoveWallVO>> getLoveWallListByGender(@PathVariable Integer gender, HttpServletRequest request) {
        Integer uid = (Integer) request.getAttribute(Const.ATTR_USER_ID);
        List<LoveWallVO> list = loveWallService.getLoveWallListByGender(gender, uid);
        return RestBean.success(list);
    }
    
    /**
     * 根据年龄范围筛选表白墙列表
     */
    @Operation(summary = "根据年龄范围筛选表白墙列表")
    @GetMapping("/list/age")
    public RestBean<List<LoveWallVO>> getLoveWallListByAgeRange(
            @RequestParam Integer minAge, 
            @RequestParam Integer maxAge, 
            HttpServletRequest request) {
        Integer uid = (Integer) request.getAttribute(Const.ATTR_USER_ID);
        List<LoveWallVO> list = loveWallService.getLoveWallListByAgeRange(minAge, maxAge, uid);
        return RestBean.success(list);
    }
    
    /**
     * 获取表白墙详情
     */
    @Operation(summary = "获取表白墙详情")
    @GetMapping("/{id}")
    public RestBean<LoveWallVO> getLoveWallById(@PathVariable int id, HttpServletRequest request) {
        Integer uid = (Integer) request.getAttribute(Const.ATTR_USER_ID);
        LoveWallVO loveWall = loveWallService.getLoveWallById(id, uid);
        return loveWall != null ? RestBean.success(loveWall) : RestBean.failure(404, "未找到该表白墙信息");
    }
    
    /**
     * 获取我发布的表白墙列表
     */
    @Operation(summary = "获取我发布的表白墙列表")
    @GetMapping("/my")
    public RestBean<List<LoveWallVO>> getMyLoveWallList(HttpServletRequest request) {
        int uid = (int) request.getAttribute(Const.ATTR_USER_ID);
        List<LoveWallVO> list = loveWallService.getMyLoveWallList(uid);
        return RestBean.success(list);
    }
    
    /**
     * 点赞表白墙
     */
    @Operation(summary = "点赞表白墙")
    @PostMapping("/like/{id}")
    public RestBean<Void> likeLoveWall(@PathVariable int id) {
        return loveWallService.likeLoveWall(id) ? 
            RestBean.success() : RestBean.failure(400, "点赞失败");
    }
    
    /**
     * 删除表白墙（仅作者）
     */
    @Operation(summary = "删除表白墙")
    @PostMapping("/delete/{id}")
    public RestBean<Void> deleteLoveWall(@PathVariable int id, HttpServletRequest request) {
        int uid = (int) request.getAttribute(Const.ATTR_USER_ID);
        return loveWallService.deleteLoveWall(id, uid) ? 
            RestBean.success() : RestBean.failure(403, "删除失败，权限不足");
    }
    
    /**
     * 更新表白墙信息（仅作者）
     */
    @Operation(summary = "更新表白墙信息")
    @PostMapping("/update/{id}")
    public RestBean<Void> updateLoveWall(@PathVariable int id, @Valid @RequestBody LoveWallCreateVO vo, HttpServletRequest request) {
        int uid = (int) request.getAttribute(Const.ATTR_USER_ID);
        return loveWallService.updateLoveWall(id, uid, vo) ? 
            RestBean.success() : RestBean.failure(403, "更新失败，权限不足");
    }
} 