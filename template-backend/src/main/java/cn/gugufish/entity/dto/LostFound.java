package cn.gugufish.entity.dto;

import cn.gugufish.entity.BaseData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 失物招领实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_lost_found")
public class LostFound implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    
    Integer uid;  // 用户ID
    String title;  // 标题
    String description;  // 描述
    String location;  // 丢失位置
    Date lostTime;  // 丢失时间
    String contactInfo;  // 联系方式
    String images;  // 图片URLs（JSON格式存储多张图片）
    Integer status;  // 状态：0-寻找中，1-已找到，2-已过期
    Date createTime;  // 创建时间
    Date updateTime;  // 更新时间
} 