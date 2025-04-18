package cn.gugufish.entity.dto;

import cn.gugufish.entity.BaseData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_notification")
public class Notification implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer uid;
    String title;
    String content;
    String type;
    String url;
    String time;
}