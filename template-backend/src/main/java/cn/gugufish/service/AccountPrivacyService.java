package cn.gugufish.service;

import cn.gugufish.entity.dto.AccountPrivacy;
import cn.gugufish.entity.vo.request.PrivacySaveVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountPrivacyService extends IService<AccountPrivacy> {
    void savePrivacy(int id, PrivacySaveVO vo);
    AccountPrivacy accountPrivacy(int id);
}
