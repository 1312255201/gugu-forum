package cn.gugufish.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 活动响应VO
 */
@Data
public class ActivityVO {
    Integer id;
    Integer adminId;
    String adminUsername;  // 管理员用户名
    String title;
    String content;
    String location;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date activityTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date endTime;
    String coverImage;
    Integer maxParticipants;
    Integer currentParticipants;
    Integer status;
    String statusText;  // 状态文本
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date updateTime;
} 