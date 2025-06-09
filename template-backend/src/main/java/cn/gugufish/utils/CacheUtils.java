package cn.gugufish.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存操作工具类
 * 提供统一的缓存数据存取接口，简化Redis操作，提高开发效率
 *
 * @author GuguFish
 */
@Component
public class CacheUtils {
    
    /**
     * Spring Redis模板
     * 用于执行Redis操作，包括字符串的存储、读取、删除等
     * StringRedisTemplate专门处理字符串类型的数据，性能更优
     */
    @Resource
    StringRedisTemplate template;

    /**
     * 从缓存中获取单个对象
     * 将Redis中存储的JSON字符串反序列化为指定类型的Java对象
     * 
     * @param <T> 目标对象类型的泛型参数
     * @param key Redis键名，用于标识缓存数据
     * @param dataType 目标对象的Class类型，用于反序列化
     * @return 反序列化后的对象实例，如果缓存不存在则返回null
     */
    public <T> T takeFromCache(String key, Class<T> dataType) {
        // 从Redis中获取JSON字符串
        String s = template.opsForValue().get(key);
        if(s == null) return null;
        
        // 将JSON字符串解析并转换为目标类型对象
        return JSONObject.parseObject(s).to(dataType);
    }

    /**
     * 从缓存中获取对象列表
     * 将Redis中存储的JSON数组字符串反序列化为指定类型的Java对象列表
     * 
     * @param <T> 列表元素类型的泛型参数
     * @param key Redis键名，用于标识缓存数据
     * @param itemType 列表元素的Class类型，用于反序列化
     * @return 反序列化后的对象列表，如果缓存不存在则返回null
     */
    public <T> List<T> takeListFromCache(String key, Class<T> itemType) {
        // 从Redis中获取JSON数组字符串
        String s = template.opsForValue().get(key);
        if(s == null) return null;
        
        // 将JSON数组字符串解析并转换为目标类型的List
        return JSONArray.parseArray(s).toList(itemType);
    }

    /**
     * 将单个对象保存到缓存
     * 将Java对象序列化为JSON字符串并存储到Redis中，同时设置过期时间
     * @param <T> 对象类型的泛型参数
     * @param key Redis键名，用于标识缓存数据
     * @param data 要缓存的Java对象
     * @param expire 过期时间（秒），超过此时间缓存将自动删除
     */
    public <T> void saveToCache(String key, T data, long expire) {
        // 将对象序列化为JSON字符串并存储到Redis
        template.opsForValue().set(key, JSONObject.from(data).toJSONString(), expire, TimeUnit.SECONDS);
    }

    /**
     * 将对象列表保存到缓存
     * 将Java对象列表序列化为JSON数组字符串并存储到Redis中，同时设置过期时间
     * @param <T> 列表元素类型的泛型参数
     * @param key Redis键名，用于标识缓存数据
     * @param list 要缓存的Java对象列表
     * @param expire 过期时间（秒），超过此时间缓存将自动删除
     */
    public <T> void saveListToCache(String key, List<T> list, long expire) {
        // 将对象列表序列化为JSON数组字符串并存储到Redis
        template.opsForValue().set(key, JSONArray.from(list).toJSONString(), expire, TimeUnit.SECONDS);
    }

    /**
     * 删除指定的缓存项
     * 根据精确的键名删除Redis中的缓存数据
     * @param key 要删除的Redis键名
     */
    public void deleteCache(String key){
        template.delete(key);
    }
    
    /**
     * 批量删除匹配模式的缓存项
     * 根据通配符模式查找并删除多个Redis键，适用于批量清理缓存
     * 
     * 工作流程：
     * 1. 使用Redis的KEYS命令查找匹配模式的所有键名
     * 2. 如果找到匹配的键，批量删除这些键
     * 3. 如果没有找到匹配的键，返回空集合，避免空指针异常
     * 
     * 通配符规则：
     * - * : 匹配任意字符序列（包括空字符）
     * - ? : 匹配任意单个字符
     * - [abc] : 匹配字符集中的任意一个字符
     * - [a-z] : 匹配字符范围中的任意一个字符
     * 
     * 使用示例：
     * - "user:*" : 删除所有以"user:"开头的缓存
     * - "session:user:*" : 删除特定用户的所有会话缓存
     * - "*:temp" : 删除所有以":temp"结尾的临时缓存
     *
     * @param key 键名的通配符模式，支持Redis的通配符语法
     */
    public void deleteCachePattern(String key){
        // 查找匹配模式的所有键名，使用Optional避免空指针异常
        Set<String> keys = Optional.ofNullable(template.keys(key)).orElse(Collections.emptySet());
        // 批量删除匹配的键
        template.delete(keys);
    }
}