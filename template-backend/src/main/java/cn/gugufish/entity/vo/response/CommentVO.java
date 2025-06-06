package cn.gugufish.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {
    int id;
    String content;
    Date time;
    String quote;
    User user;

    @Data
    public static class User {
        Integer id;
        String username;
        String avatar;
        boolean gender;
        String qq;
        String wechat;
        String phone;
        String email;
    }
}