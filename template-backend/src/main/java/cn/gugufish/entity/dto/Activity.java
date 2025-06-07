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
 * 校园活动实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("db_activity")
public class Activity implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    
    Integer adminId;  // 管理员ID
    String title;     // 活动标题
    String content;   // 活动内容（富文本）
    String location;  // 活动地点
    Date activityTime; // 活动时间
    Date endTime;     // 活动结束时间
    String coverImage; // 封面图片
    Integer maxParticipants; // 最大参与人数（0表示不限制）
    Integer currentParticipants; // 当前参与人数
    Integer status;   // 状态：0-未开始，1-进行中，2-已结束，3-已取消
    Date createTime;  // 创建时间
    Date updateTime;  // 更新时间
} 