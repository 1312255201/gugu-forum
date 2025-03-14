package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.vo.response.AccountVO;
import cn.gugufish.service.AccountService;
import cn.gugufish.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountService accountService;
    @GetMapping("/info")
    public RestBean<AccountVO> info(@RequestAttribute(Const.ATTR_USER_ID) int id){
        Account account = accountService.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVO.class));
    }
}
