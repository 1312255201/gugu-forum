package cn.gugufish.service;

import cn.gugufish.entity.dto.AccountDetails;
import cn.gugufish.entity.vo.request.DetailsSaveVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AccountDetailsService extends IService<AccountDetails> {
    AccountDetails findAccountDetailsById(int id);
    boolean saveAccountDetails(int id, DetailsSaveVO vo);
}
