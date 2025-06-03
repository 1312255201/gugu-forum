package cn.gugufish.entity.vo.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 失物招领响应VO
 */
@Data
public class LostFoundVO {
    Integer id;
    Integer uid;
    String username;  // 用户名
    String title;
    String description;
    String location;
    Date lostTime;
    String contactInfo;
    List<String> images;  // 图片URL列表
    Integer status;
    String statusText;  // 状态文本
    Date createTime;
    Date updateTime;
} 