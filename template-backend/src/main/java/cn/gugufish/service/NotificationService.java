package cn.gugufish.service;

import cn.gugufish.entity.dto.Notification;
import cn.gugufish.entity.vo.response.NotificationVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    List<NotificationVO> findUserNotification(int uid);
    void deleteUserNotification(int id, int uid);
    void deleteUserAllNotification(int uid);
    void addNotification(int uid, String title, String content, String type, String url);
}