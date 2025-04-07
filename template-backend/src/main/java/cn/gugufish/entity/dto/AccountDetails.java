package cn.gugufish.entity.dto;

import cn.gugufish.entity.BaseData;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("db_account_details")
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetails implements BaseData {
    @TableId
    Integer id;
    Integer gender;
    String phone;
    String qq;
    String wechat;
    @TableField("`desc`")
    String desc;
}
