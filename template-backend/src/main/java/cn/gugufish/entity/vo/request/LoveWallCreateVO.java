package cn.gugufish.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * 表白墙创建请求VO
 */
@Data
public class LoveWallCreateVO {
    
    @NotNull
    @Length(min = 1, max = 50)
    String nickname;
    
    @NotNull
    String avatar;
    
    List<String> photos;
    
    @NotNull
    @Length(min = 10, max = 1000)
    String introduction;
    
    @NotNull
    @Length(min = 1, max = 200)
    String contact;
    
    @Min(16)
    @Max(60)
    Integer age;
    
    @Min(0)
    @Max(2)
    Integer gender;
    
    List<String> tags;
} 