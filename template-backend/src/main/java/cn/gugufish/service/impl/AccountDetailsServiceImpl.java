package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.dto.AccountDetails;
import cn.gugufish.entity.vo.request.DetailsSaveVO;
import cn.gugufish.mapper.AccountDetailsMapper;
import cn.gugufish.service.AccountDetailsService;
import cn.gugufish.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsService {

    @Resource
    AccountService service;
    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }

    @Override
    public  synchronized boolean saveAccountDetails(int id, DetailsSaveVO vo) {
        Account account =service.findAccountByNameOrEmail(vo.getUsername());
        if (account==null || account.getId() == id){
            if(service.update()
                    .eq("id",id)
                    .set("username",vo.getUsername())
                    .update()){
                this.saveOrUpdate(new AccountDetails(
                        id,vo.getGender(),vo.getPhone(),vo.getQq(),vo.getWechat(),vo.getDesc()
                ));
                return true;
            }


        }
        return false;
    }
}
