package cn.gugufish.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web应用通用配置类
 * 提供Web应用运行所需的基础组件配置，包括：
 * - 密码加密器配置
 * - HTTP客户端配置  
 * - MyBatis-Plus分页插件配置
 * 
 * @author GuguFish
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * 配置密码加密器
     * 使用BCrypt算法对用户密码进行安全加密
     * BCrypt是一种自适应哈希函数，基于Blowfish密码算法，
     * 具有加盐、抗彩虹表攻击等安全特性
     * 
     * @return BCrypt密码加密器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 配置REST模板客户端
     * 提供HTTP客户端功能，用于与外部API进行通信
     * 如第三方服务调用、微服务间通信等
     * 
     * @return RestTemplate实例，用于发送HTTP请求
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    
    /**
     * 配置MyBatis-Plus拦截器
     * 主要配置分页插件，为数据库查询提供分页功能
     * 分页插件可以自动处理分页查询的SQL生成和结果处理
     * 
     * @return 配置了分页功能的MyBatis-Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 创建拦截器实例
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页内部拦截器，支持自动分页查询
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
