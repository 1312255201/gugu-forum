package cn.gugufish.controller.admin;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.EmailRecord;
import cn.gugufish.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/email")
public class EmailAdminController {

    @Resource
    EmailService service;


    @GetMapping("/list")
    public RestBean<List<EmailRecord>> listEmailRecord(){
        return RestBean.success(service.listEmailRecord());
    }
}
