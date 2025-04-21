package cn.gugufish.controller;


import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.AccountVO;
import cn.gugufish.service.AccountDetailsService;
import cn.gugufish.service.AccountPrivacyService;
import cn.gugufish.service.AccountService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
public class AccountAdminController {

    @Resource
    AccountService service;
    @Resource
    AccountDetailsService detailsService;

    @Resource
    AccountPrivacyService privacyService;
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
}