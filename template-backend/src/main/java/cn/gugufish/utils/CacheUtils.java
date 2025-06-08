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
 * 主要功能：
 * - 对象缓存：支持任意Java对象的序列化存储和反序列化读取
 * - 列表缓存：支持List集合的缓存操作，适用于分页数据等场景
 * - 过期时间控制：所有缓存操作均支持TTL（生存时间）设置
 * - 批量删除：支持按模式匹配批量删除缓存键
 * - 类型安全：使用泛型确保类型安全，避免类型转换错误
 * 
 * 序列化机制：
 * 使用FastJSON2进行对象序列化，具有以下优势：
 * - 性能优异：序列化和反序列化速度快
 * - 兼容性好：支持复杂对象结构和继承关系
 * - 内存占用少：序列化后的JSON字符串相对紧凑
 * - 易于调试：存储的是可读的JSON格式，便于问题排查
 * 
 * 适用场景：
 * - 数据库查询结果缓存，减少数据库访问压力
 * - 频繁计算结果缓存，提高响应速度
 * - 会话数据缓存，支持分布式会话共享
 * - 热点数据缓存，应对高并发访问
 * - 临时数据存储，如验证码、令牌等
 * 
 * 使用示例：
 * <pre>
 * // 缓存单个对象
 * User user = new User("张三", 25);
 * cacheUtils.saveToCache("user:123", user, 3600);
 * 
 * // 读取单个对象
 * User cachedUser = cacheUtils.takeFromCache("user:123", User.class);
 * 
 * // 缓存对象列表
 * List<Product> products = productService.getProducts();
 * cacheUtils.saveListToCache("products:category:1", products, 1800);
 * 
 * // 读取对象列表
 * List<Product> cachedProducts = cacheUtils.takeListFromCache("products:category:1", Product.class);
 * </pre>
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
     * 工作流程：
     * 1. 根据键名从Redis中获取JSON字符串
     * 2. 如果键不存在或值为null，直接返回null
     * 3. 使用FastJSON2将JSON字符串解析为JSONObject
     * 4. 将JSONObject转换为指定的目标类型对象
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
     * 工作流程：
     * 1. 根据键名从Redis中获取JSON数组字符串
     * 2. 如果键不存在或值为null，直接返回null
     * 3. 使用FastJSON2将JSON数组字符串解析为JSONArray
     * 4. 将JSONArray转换为指定类型的List对象
     * 
     * 注意事项：
     * - 返回的List中的元素类型由itemType参数指定
     * - 如果缓存不存在，返回null而不是空列表，便于业务层判断缓存命中情况
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
     * 
     * 工作流程：
     * 1. 使用FastJSON2将Java对象转换为JSONObject
     * 2. 将JSONObject转换为JSON字符串
     * 3. 将JSON字符串存储到Redis中，并设置TTL过期时间
     * 
     * 序列化特性：
     * - 支持复杂对象结构，包括嵌套对象和集合
     * - 自动处理null值和基本类型
     * - 保持对象的完整性，包括所有可序列化字段
     * 
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
     * 
     * 工作流程：
     * 1. 使用FastJSON2将List对象转换为JSONArray
     * 2. 将JSONArray转换为JSON数组字符串
     * 3. 将JSON数组字符串存储到Redis中，并设置TTL过期时间
     * 
     * 适用场景：
     * - 分页查询结果缓存：缓存某一页的数据列表
     * - 关联数据缓存：缓存某个对象关联的子对象列表
     * - 计算结果缓存：缓存复杂计算得出的对象列表
     * - 热点数据缓存：缓存经常访问的数据列表
     * 
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
     * 
     * 使用场景：
     * - 数据更新后清除相关缓存，保证数据一致性
     * - 用户注销后清除会话缓存
     * - 业务逻辑需要主动清除某个缓存项
     * 
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
     * 注意事项：
     * - KEYS命令在大数据量时性能较差，生产环境建议谨慎使用
     * - 建议在业务低峰期执行批量删除操作
     * - 可以考虑使用SCAN命令替代KEYS命令提高性能
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