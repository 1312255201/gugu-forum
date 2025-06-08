package cn.gugufish.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 表白墙响应VO
 */
@Data
public class LoveWallVO {
    
    Integer id;
    
    Integer uid;
    
    String nickname;
    
    String avatar;
    
    List<String> photos;
    
    String introduction;
    
    String contact;
    
    Integer age;
    
    Integer gender;
    
    String genderText;
    
    List<String> tags;
    
    Integer status;
    
    String statusText;
    
    Integer likeCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date createTime;
    
    // 是否为当前用户发布的
    Boolean isMine;
} 