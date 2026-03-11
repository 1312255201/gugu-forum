package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.service.AiService;
import com.alibaba.fastjson2.JSONArray;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Resource
    AiService service;

    @PostMapping("/chat")
    public RestBean<String> chatWithAI(@RequestBody JSONArray content) {
        return RestBean.success(service.chatWithAi(content));
    }
}
