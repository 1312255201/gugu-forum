package cn.gugufish.filter;

import cn.gugufish.entity.RestBean;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.FlowUtils;
import cn.gugufish.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 流量限制过滤器
 * 基于Redis实现的分布式限流机制，防止恶意用户或程序对系统进行高频攻击
 * 
 * 限流策略：
 * - 基于IP地址进行限流控制
 * - 使用滑动窗口算法统计请求频率
 * - 超出限制后临时封禁IP地址
 * - 支持配置化的限流参数调整
 * 
 * 工作原理：
 * 1. 提取客户端真实IP地址（支持反向代理）
 * 2. 在Redis中维护每个IP的请求计数器
 * 3. 检查指定时间窗口内的请求次数
 * 4. 超出限制则将IP加入封禁列表
 * 5. 封禁期间拒绝该IP的所有请求
 * 
 * 配置参数：
 * - spring.web.flow.limit: 时间窗口内最大请求次数
 * - spring.web.flow.period: 统计时间窗口（秒）
 * - spring.web.flow.block: 超限后的封禁时间（秒）
 * 
 * @author GuguFish
 */
@Slf4j
@Component
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitingFilter extends HttpFilter {

    /**
     * Redis模板，用于存储限流计数器和封禁列表
     * 实现分布式环境下的统一流量控制
     */
    @Resource
    StringRedisTemplate template;
    
    /**
     * 指定时间窗口内允许的最大请求次数
     */
    @Value("${spring.web.flow.limit}")
    int limit;
    
    /**
     * 限流统计的时间窗口长度（秒）
     */
    @Value("${spring.web.flow.period}")
    int period;
    
    /**
     * 超出请求限制后的封禁时长（秒）
     * 从配置文件读取：spring.web.flow.block
     * 例如：3600表示封禁1小时
     */
    @Value("${spring.web.flow.block}")
    int block;

    /**
     * 流量控制工具类
     * 提供限流算法的具体实现
     */
    @Resource
    FlowUtils utils;

    /**
     * 过滤器核心方法
     * 对每个HTTP请求进行流量限制检查
     * 
     * 处理逻辑：
     * 1. 获取客户端真实IP地址
     * 2. 检查IP是否在封禁列表中
     * 3. 如果未封禁，检查请求频率是否超限
     * 4. 超限则返回429状态码和限流提示
     * 5. 正常则继续执行后续过滤器
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链
     * @throws IOException IO操作异常
     * @throws ServletException Servlet处理异常
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 获取客户端真实IP地址（支持Nginx反向代理）
        String address = IpUtils.getRealClientIp(request);
        
        // 检查IP的请求频率，如果超出限制则拒绝请求
        if (!tryCount(address)) {
            this.writeBlockMessage(response);
        } else {
            // 请求频率正常，继续执行后续过滤器
            chain.doFilter(request, response);
        }
    }

    /**
     * 尝试对指定IP地址进行请求计数和限流检查
     * 使用Redis分布式锁确保计数的原子性和准确性
     * 
     * 检查逻辑：
     * 1. 首先检查IP是否已被封禁
     * 2. 如果未封禁，则进行请求频率统计
     * 3. 使用滑动窗口算法检查时间窗口内的请求次数
     * 4. 超出限制则将IP加入封禁列表
     * 
     * @param address 客户端IP地址
     * @return true表示允许请求，false表示请求被限制
     */
    private boolean tryCount(String address) {
        // 使用IP地址的内部字符串作为锁对象，确保同一IP的并发安全
        synchronized (address.intern()) {
            // 检查IP是否已被封禁
            if(template.hasKey(Const.FLOW_LIMIT_BLOCK + address))
                return false;
                
            // 构建Redis键名
            String counterKey = Const.FLOW_LIMIT_COUNTER + address;  // 计数器键
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;      // 封禁键
            
            // 调用限流工具类进行周期性限流检查
            return utils.limitPeriodCheck(counterKey, blockKey, block, limit, period);
        }
    }

    /**
     * 向客户端返回限流拦截消息
     * 当请求被限流时，返回HTTP 429状态码和JSON格式的错误信息
     * 
     * 响应格式：
     * - 状态码：429 Too Many Requests
     * - 内容类型：application/json;charset=utf-8
     * - 响应体：{"success":false,"code":429,"message":"请求频率过快，请稍后再试"}
     * 
     * @param response HTTP响应对象
     * @throws IOException 写入响应时可能发生的IO异常
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        // 设置HTTP状态码为429（请求过于频繁）
        response.setStatus(429);
        // 设置响应内容类型为JSON格式
        response.setContentType("application/json;charset=utf-8");
        
        // 获取响应输出流
        PrintWriter writer = response.getWriter();
        // 写入标准化的错误响应
        writer.write(RestBean.failure(429, "请求频率过快，请稍后再试").asJsonString());
    }
}
