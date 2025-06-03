package cn.gugufish.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 失物招领创建请求VO
 */
@Data
public class LostFoundCreateVO {
    @NotNull
    @Length(min = 1, max = 100)
    String title;
    
    @NotNull
    @Length(min = 1, max = 1000)
    String description;
    
    @NotNull
    @Length(min = 1, max = 100)
    String location;
    
    @NotNull
    Date lostTime;
    
    @NotNull
    @Length(min = 1, max = 200)
    String contactInfo;
    
    List<String> images;  // 图片URL列表
} 