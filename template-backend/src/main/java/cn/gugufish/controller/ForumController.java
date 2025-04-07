package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.TopicTypeVO;
import cn.gugufish.entity.vo.response.WeatherVO;
import cn.gugufish.service.TopicService;
import cn.gugufish.service.WeatherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Resource
    WeatherService weatherService;
    @Resource
    TopicService topicService;
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
}
