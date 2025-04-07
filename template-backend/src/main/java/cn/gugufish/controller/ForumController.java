package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.request.TopicCreateVO;
import cn.gugufish.entity.vo.response.TopicTypeVO;
import cn.gugufish.entity.vo.response.WeatherVO;
import cn.gugufish.service.TopicService;
import cn.gugufish.service.WeatherService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/forum")
public class ForumController {
    @Resource
    WeatherService weatherService;
    @Resource
    TopicService topicService;
    @Resource
    ControllerUtils controllerUtils;
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(double longitude,double latitude){
        WeatherVO vo = weatherService.fetchWeather( longitude, latitude);
        return vo == null?
                RestBean.failure(400,"获取天气失败，请联系管理员！") : RestBean.success(vo);
    }
    @GetMapping("/types")
    public RestBean<List<TopicTypeVO>> listTypes(){
        return RestBean.success(topicService
                .listTypes()
                .stream()
                .map(type -> type.asViewObject(TopicTypeVO.class))
                .toList());
    }
    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @Valid @RequestBody TopicCreateVO vo){
        return controllerUtils.messageHandle(()-> topicService.createTopic(id, vo));
    }
}
