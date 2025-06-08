package cn.gugufish.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_love_wall")
public class LoveWall {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    // 用户ID
    private Integer uid;
    
    // 昵称
    private String nickname;
    
    // 主要照片（头像）
    private String avatar;
    
    // 照片集合（JSON格式存储多张照片）
    private String photos;
    
    // 自我介绍
    private String introduction;
    
    // 联系方式
    private String contact;
    
    // 年龄
    private Integer age;
    
    // 性别 (0-女 1-男 2-其他)
    private Integer gender;
    
    // 爱好标签（JSON格式）
    private String tags;
    
    // 是否审核通过 (0-待审核 1-已通过 2-已拒绝)
    private Integer status;
    
    // 点赞数
    private Integer likeCount;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
} 