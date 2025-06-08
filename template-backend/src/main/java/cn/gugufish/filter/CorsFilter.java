package cn.gugufish.filter;

import cn.gugufish.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 跨域资源共享(CORS)过滤器
 * 处理浏览器的跨域请求，解决前后端分离架构中的跨域访问问题
 * 
 * CORS简介：
 * 跨域资源共享(Cross-Origin Resource Sharing)是一种机制，它使用额外的HTTP头来告诉浏览器
 * 让运行在一个origin(域)上的Web应用被准许访问来自不同源服务器上的指定的资源。
 * 
 * 支持的HTTP方法：
 * GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH
 * 
 * 配置参数：
 * - spring.web.cors.origin: 允许跨域的源地址
 * - spring.web.cors.credentials: 是否允许发送凭据(Cookie等)
 * - spring.web.cors.methods: 允许的HTTP方法
 * 
 * @author GuguFish
 */
@Component
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {

    /**
     * 允许跨域访问的源地址配置
     */
    @Value("${spring.web.cors.origin}")
    String origin;

    /**
     * 是否允许发送身份凭据
     * 从配置文件读取：spring.web.cors.credentials
     * 
     * 凭据包括：
     * - HTTP Cookies
     * - HTTP认证头
     * - TLS客户端证书
     * 
     * 安全注意：
     * - 当设置为true时，origin不能设置为"*"
     * - 生产环境需要谨慎开启，避免CSRF攻击
     */
    @Value("${spring.web.cors.credentials}")
    boolean credentials;

    /**
     * 允许的HTTP请求方法
     * 从配置文件读取：spring.web.cors.methods
     * 
     * 配置示例：
     * - "*": 允许所有HTTP方法
     * - "GET,POST,PUT,DELETE": 只允许指定方法
     * 
     * 常用方法说明：
     * - GET: 获取资源
     * - POST: 创建资源
     * - PUT: 更新资源
     * - DELETE: 删除资源
     * - OPTIONS: 预检请求
     */
    @Value("${spring.web.cors.methods}")
    String methods;

    /**
     * 过滤器核心处理方法
     * 为每个HTTP请求添加CORS相关的响应头，使浏览器允许跨域访问
     * 
     * 处理流程：
     * 1. 向响应中添加所有必要的CORS头部
     * 2. 继续执行后续过滤器链
     * 
     * CORS头部说明：
     * - Access-Control-Allow-Origin: 允许访问的源地址
     * - Access-Control-Allow-Methods: 允许的HTTP方法
     * - Access-Control-Allow-Headers: 允许的请求头
     * - Access-Control-Allow-Credentials: 是否允许发送凭据
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链，用于继续执行后续过滤器
     * @throws IOException IO操作异常
     * @throws ServletException Servlet处理异常
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 添加所有CORS相关的响应头
        this.addCorsHeader(request, response);
        // 继续执行后续过滤器
        chain.doFilter(request, response);
    }

    /**
     * 添加所有跨域相关的HTTP响应头
     * 根据配置文件设置和请求信息，动态生成合适的CORS头部
     * 
     * 响应头详解：
     * 1. Access-Control-Allow-Origin: 指定允许访问的源
     * 2. Access-Control-Allow-Methods: 指定允许的HTTP方法
     * 3. Access-Control-Allow-Headers: 指定允许的请求头
     * 4. Access-Control-Allow-Credentials: 指定是否允许发送凭据
     * 
     * @param request HTTP请求对象，用于获取Origin头部信息
     * @param response HTTP响应对象，用于设置CORS头部
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        // 设置允许访问的源地址（支持动态配置）
        response.addHeader("Access-Control-Allow-Origin", this.resolveOrigin(request));
        
        // 设置允许的HTTP方法（支持通配符配置）
        response.addHeader("Access-Control-Allow-Methods", this.resolveMethod());
        
        // 设置允许的请求头（包含常用的认证和内容类型头）
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        
        // 根据配置决定是否允许发送凭据
        if(credentials) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * 解析和格式化允许的HTTP请求方法
     * 将配置文件中的方法配置转换为标准的CORS响应头格式
     * 
     * 转换规则：
     * - 如果配置为"*"：返回所有标准HTTP方法的完整列表
     * - 如果配置为具体方法：直接返回配置的方法列表
     * 
     * 标准HTTP方法列表：
     * GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH
     * 
     * @return 格式化后的HTTP方法字符串，用于Access-Control-Allow-Methods头部
     */
    private String resolveMethod(){
        return methods.equals("*") ? 
                "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH" : methods;
    }

    /**
     * 解析和确定允许的请求源地址
     * 根据配置和实际请求情况，动态确定Access-Control-Allow-Origin的值
     * 
     * 解析规则：
     * - 如果配置为"*"：返回请求头中的Origin值（实现动态跨域）
     * - 如果配置为具体域名：直接返回配置的域名（固定跨域）
     * 
     * 安全考虑：
     * - "*"配置适用于开发环境，提供最大的灵活性
     * - 具体域名配置适用于生产环境，提供更好的安全性
     * 
     * @param request HTTP请求对象，用于获取Origin请求头
     * @return 解析后的源地址字符串，用于Access-Control-Allow-Origin头部
     */
    private String resolveOrigin(HttpServletRequest request){
        return origin.equals("*") ? 
                request.getHeader("Origin") : origin;
    }
}
