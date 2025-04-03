package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UntilController {
    @GetMapping("/api/util/ip")
    public RestBean<String> ip(HttpServletRequest request){
        return RestBean.success(request.getRemoteAddr());
    }

}
