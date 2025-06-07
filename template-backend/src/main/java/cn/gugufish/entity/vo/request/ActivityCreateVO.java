package cn.gugufish.entity.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * 活动创建请求VO
 */
@Data
public class ActivityCreateVO {
    @NotNull
    @Length(min = 1, max = 100)
    String title;
    
    @NotNull
    @Length(min = 1, max = 10000)
    String content;
    
    @NotNull
    @Length(min = 1, max = 100)
    String location;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date activityTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
    Date endTime;
    
    String coverImage;
    
    Integer maxParticipants; // 最大参与人数，0表示不限制
} 