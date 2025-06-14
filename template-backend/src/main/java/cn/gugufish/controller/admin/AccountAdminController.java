package cn.gugufish.controller.admin;


import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.dto.AccountDetails;
import cn.gugufish.entity.dto.AccountPrivacy;
import cn.gugufish.entity.vo.response.AccountVO;
import cn.gugufish.service.AccountDetailsService;
import cn.gugufish.service.AccountPrivacyService;
import cn.gugufish.service.AccountService;
import cn.gugufish.utils.Const;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/user")
public class AccountAdminController {

    @Resource
    AccountService service;
    @Resource
    AccountDetailsService detailsService;
    @Resource
    AccountPrivacyService privacyService;
    @Resource
    StringRedisTemplate template;

    @Value("${spring.security.jwt.expire}")
    private int expire;

    @GetMapping("/list")
    public RestBean<JSONObject> accountList(int page, int size) {
        JSONObject object = new JSONObject();
        List<AccountVO> list = service.page(Page.of(page, size))
                .getRecords()
                .stream()
                .map(a -> a.asViewObject(AccountVO.class))
                .toList();
        object.put("total", service.count());
        object.put("list", list);
        return RestBean.success(object);
    }
    @GetMapping("/detail")
    public RestBean<JSONObject> accountDetail(int id) {
        JSONObject object = new JSONObject();
        object.put("detail", detailsService.findAccountDetailsById(id));
        object.put("privacy", privacyService.accountPrivacy(id));
        return RestBean.success(object);
    }

    @PostMapping("/save")
    public RestBean<Void> saveAccount(@RequestBody JSONObject object) {
        //TODO 这里有个错误，如果详细信息内容是空的会导致保存失败，导致程序异常退出报错，需要修复(BugFish留)
        int id = object.getInteger("id");
        Account account = service.findAccountById(id);
        Account save = object.toJavaObject(Account.class);
        handleBanned(account, save);
        BeanUtils.copyProperties(save, account, "password", "registerTime");
        service.saveOrUpdate(account);
        AccountDetails details = detailsService.findAccountDetailsById(id);
        AccountDetails saveDetails = object.getJSONObject("detail").toJavaObject(AccountDetails.class);
        BeanUtils.copyProperties(saveDetails, details);
        detailsService.saveOrUpdate(details);
        AccountPrivacy privacy = privacyService.accountPrivacy(id);
        AccountPrivacy savePrivacy = object.getJSONObject("privacy").toJavaObject(AccountPrivacy.class);
        BeanUtils.copyProperties(savePrivacy, privacy);
        privacyService.saveOrUpdate(savePrivacy);
        return RestBean.success();
    }
    private void handleBanned(Account old, Account current) {
        String key = Const.BANNED_BLOCK + old.getId();
        if(!old.isBanned() && current.isBanned()) {
            template.opsForValue().set(key, "true", expire, TimeUnit.HOURS);
        } else if(old.isBanned() && !current.isBanned()) {
            template.delete(key);
        }
    }
}