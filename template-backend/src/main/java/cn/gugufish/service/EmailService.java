package cn.gugufish.service;

public interface EmailService {
    void sendVerifyEmail(String type, String email, int code);
}
