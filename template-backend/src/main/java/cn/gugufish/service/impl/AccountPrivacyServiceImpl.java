package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.AccountPrivacy;
import cn.gugufish.entity.vo.request.PrivacySaveVO;
import cn.gugufish.mapper.AccountPrivacyMapper;
import cn.gugufish.service.AccountPrivacyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacy> implements AccountPrivacyService {
    @Override
    @Transactional
    public void savePrivacy(int id, PrivacySaveVO vo){
        AccountPrivacy privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
        boolean status = vo.isStatus();
        switch (vo.getType()) {
            case "phone" -> privacy.setPhone(status);
            case "email" -> privacy.setEmail(status);
            case "qq" -> privacy.setQq(status);
            case "wechat" -> privacy.setWechat(status);
            case "gender" -> privacy.setGender(status);
        }
        this.saveOrUpdate(privacy);
    }
    public AccountPrivacy accountPrivacy(int id){
        AccountPrivacy privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
        this.saveOrUpdate(privacy);
        return privacy;
    }
}
