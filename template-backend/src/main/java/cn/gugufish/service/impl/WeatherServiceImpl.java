package cn.gugufish.service.impl;

import cn.gugufish.entity.vo.response.WeatherVO;
import cn.gugufish.service.WeatherService;
import cn.gugufish.utils.Const;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Resource
    RestTemplate rest;
    @Resource
    StringRedisTemplate template;
    @Value("${spring.weather.key}")
    String key;

    @Override
    public WeatherVO fetchWeather(double longitude, double latitude) {
        return fetchFromCache(longitude, latitude);
    }
    private WeatherVO fetchFromCache(double longitude, double latitude){
        JSONObject geo = this.decompressStringToJson(rest.getForObject(
                "https://geoapi.qweather.com/v2/city/lookup?location="+longitude+","+latitude+"&key="+key, byte[].class));
        if(geo == null)return null;
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);
        int id = location.getInteger("id");
        String key = Const.FORUM_WEATHER_CACHE + id;
        String cache =template.opsForValue().get(key);
        if(cache != null)
            return JSONObject.parseObject(cache).to(WeatherVO.class);
        WeatherVO vo = this.fetchFromAPI(id,location);
        template.opsForValue().set(key, JSONObject.from(vo).toJSONString(),1, TimeUnit.HOURS);
        if(vo == null)return null;
        return vo;
    }
    private WeatherVO fetchFromAPI(int id,JSONObject location){
        WeatherVO vo = new WeatherVO();
        vo.setLocation(location);
        JSONObject now = this.decompressStringToJson(rest.getForObject(
                "https://api.qweather.com/v7/weather/now?location="+id+"&key="+key, byte[].class));
        if(now == null) return null;
        vo.setNow(now.getJSONObject("now"));
        JSONObject hourly = this.decompressStringToJson(rest.getForObject(
                "https://api.qweather.com/v7/weather/24h?location="+id+"&key="+key, byte[].class));
        if(hourly == null) return null;
        vo.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return vo;
    }
    private JSONObject decompressStringToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while((read = gzip.read(buffer)) != -1){
                stream.write(buffer, 0, read);
            }
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        }catch (IOException e){
            return null;
        }
    }
}
