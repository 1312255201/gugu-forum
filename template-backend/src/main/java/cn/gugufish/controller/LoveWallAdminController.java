package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.LoveWallVO;
import cn.gugufish.service.LoveWallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙管理员控制器
 */
@Tag(name = "表白墙管理", description = "表白墙管理相关接口（仅管理员）。")
@RestController
@RequestMapping("/api/admin/love-wall")
public class LoveWallAdminController {
    
    @Resource
    LoveWallService loveWallService;
    
    /**
     * 获取待审核的表白墙列表
     */
    @Operation(summary = "获取待审核的表白墙列表")
    @GetMapping("/pending")
    public RestBean<List<LoveWallVO>> getPendingLoveWallList() {
        List<LoveWallVO> list = loveWallService.getPendingLoveWallList();
        return RestBean.success(list);
    }
    
    /**
     * 审核表白墙
     * status: 1-通过 2-拒绝
     */
    @Operation(summary = "审核表白墙")
    @PostMapping("/approve/{id}")
    public RestBean<Void> approveLoveWall(@PathVariable int id, @RequestParam int status) {
        if (status != 1 && status != 2) {
            return RestBean.failure(400, "审核状态参数错误");
        }
        return loveWallService.approveLoveWall(id, status) ? 
            RestBean.success() : RestBean.failure(400, "审核失败");
    }
} 