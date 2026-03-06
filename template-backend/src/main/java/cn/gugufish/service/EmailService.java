package cn.gugufish.service;

import cn.gugufish.entity.dto.EmailRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface EmailService {
    void sendVerifyEmail(String type, String email, int code);
    Page<EmailRecord> listEmailRecord(int page, int size);
    boolean resendEmailRecord(int id);

}
