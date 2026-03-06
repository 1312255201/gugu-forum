package cn.gugufish.controller.admin;

import cn.gugufish.entity.PageRestBean;
import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.TopicPreviewVO;
import cn.gugufish.service.TopicService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/forum")
public class ForumAdminController {

    @Resource
    private TopicService service;

    @GetMapping("/list")
    public PageRestBean<TopicPreviewVO> list(@RequestParam int page,
                                             @RequestParam int size) {
        JSONObject result = service.listAllTopicByPage(page, size);
        return PageRestBean.success(
                result.getJSONArray("list").toList(TopicPreviewVO.class),
                result.getIntValue("total"),
                page
        );
    }
    @GetMapping("/delete")
    public RestBean<Void> delete(@RequestParam int tid){
        service.deleteTopic(tid);
        return RestBean.success();
    }

    @PostMapping("/top")
    public RestBean<Void> setTop(@RequestBody JSONObject object) {
        service.setTopicTop(
                object.getIntValue("tid"),
                object.getBooleanValue("status")
        );
        return RestBean.success();
    }
    @PostMapping("/locked")
    public RestBean<Void> setLocked(@RequestBody JSONObject object) {
        service.setTopicLocked(
                object.getIntValue("tid"),
                object.getBooleanValue("status")
        );
        return RestBean.success();
    }

}
