package cn.gugufish.service;

import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.vo.request.ConfirmResetVO;
import cn.gugufish.entity.vo.request.EmailRegisterVO;
import cn.gugufish.entity.vo.request.EmailResetVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String address);
    String registerEmailAccount(EmailRegisterVO info);
    String resetEmailAccountPassword(EmailResetVO info);
    String resetConfirm(ConfirmResetVO info);
}
