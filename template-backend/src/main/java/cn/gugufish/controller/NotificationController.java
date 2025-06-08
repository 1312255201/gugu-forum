package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.NotificationVO;
import cn.gugufish.service.NotificationService;
import cn.gugufish.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知消息控制器
 * 提供用户通知消息的管理功能，帮助用户及时了解系统消息和互动信息
 * 主要功能包括：
 * - 获取用户的所有通知消息
 * - 删除指定的通知消息
 * - 批量删除所有通知消息
 * 
 * 通知消息类型可能包括：
 * - 系统通知（如系统维护公告）
 * - 互动通知（如评论回复、点赞收藏）
 * - 私信通知（如用户私信）
 * 
 * @author GuguFish
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    /**
     * 通知服务类
     * 提供通知消息相关的业务逻辑处理
     */
    @Resource
    NotificationService service;

    /**
     * 获取用户的通知消息列表
     * 返回当前登录用户的所有未删除通知消息，按时间倒序排列
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @return 用户的通知消息列表
     */
    @GetMapping("/list")
    public RestBean<List<NotificationVO>> listNotification(@RequestAttribute(Const.ATTR_USER_ID) int id) {
        return RestBean.success(service.findUserNotification(id));
    }

    /**
     * 删除指定的通知消息
     * 用户可以删除不再需要的通知消息，只能删除属于自己的通知
     * 
     * @param id 要删除的通知消息ID
     * @param uid 当前登录用户的ID（从JWT令牌中获取）
     * @return 删除操作的结果
     */
    @GetMapping("/delete")
    public RestBean<List<NotificationVO>> deleteNotification(@RequestParam @Min(0) int id,
                                                             @RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserNotification(id, uid);
        return RestBean.success();
    }

    /**
     * 删除用户的所有通知消息
     * 批量清空当前用户的所有通知消息，通常用于"一键清空"功能
     * 
     * @param uid 当前登录用户的ID（从JWT令牌中获取）
     * @return 批量删除操作的结果
     */
    @GetMapping("/delete-all")
    public RestBean<List<NotificationVO>> deleteAllNotification(@RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserAllNotification(uid);
        return RestBean.success();
    }
}