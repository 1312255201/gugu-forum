package cn.gugufish.service;

import cn.gugufish.entity.vo.response.WeatherVO;

public interface WeatherService {
    WeatherVO fetchWeather(double longitude,double latitude);
}
