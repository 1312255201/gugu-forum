package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.NotificationVO;
import cn.gugufish.service.NotificationService;
import cn.gugufish.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    public RestBean<List<NotificationVO>> listNotification(@RequestAttribute(Const.ATTR_USER_ID) int id) {
        return RestBean.success(service.findUserNotification(id));
    }

    @GetMapping("/delete")
    public RestBean<List<NotificationVO>> deleteNotification(@RequestParam @Min(0) int id,
                                                             @RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserNotification(id, uid);
        return RestBean.success();
    }

    @GetMapping("/delete-all")
    public RestBean<List<NotificationVO>> deleteAllNotification(@RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserAllNotification(uid);
        return RestBean.success();
    }
}