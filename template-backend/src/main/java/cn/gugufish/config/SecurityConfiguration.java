package cn.gugufish.config;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.vo.response.AuthorizeVO;
import cn.gugufish.filter.JwtAuthenticationFilter;
import cn.gugufish.filter.RequestLogFilter;
import cn.gugufish.service.AccountService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * SpringSecurity安全框架配置类
 *
 * @author GuguFish
 */
@Configuration
public class SecurityConfiguration {

    /**
     * JWT认证过滤器
     * 用于验证请求中的JWT令牌并设置用户认证信息
     */
    @Resource
    JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 请求日志过滤器
     * 用于记录HTTP请求的详细信息，便于调试和监控
     */
    @Resource
    RequestLogFilter requestLogFilter;

    /**
     * JWT工具类
     * 提供JWT令牌的生成、验证、解析等功能
     */
    @Resource
    JwtUtils utils;

    /**
     * 账户服务类
     * 提供用户账户相关的业务逻辑处理
     */
    @Resource
    AccountService service;

    /**
     * 配置SpringSecurity的安全过滤器链
     *
     * @param http HTTP安全配置器
     * @return 配置完成的安全过滤器链
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 配置HTTP请求授权规则
                .authorizeHttpRequests(conf -> conf
                        // 允许匿名访问的接口：认证相关、错误页面
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        // 允许匿名访问的工具接口
                        .requestMatchers("/api/util/**").permitAll()
                        // 允许匿名访问静态资源（图片等）
                        .requestMatchers("/images/**").permitAll()
                        // 允许匿名访问Swagger文档接口
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 管理员接口需要ADMIN角色
                        .requestMatchers("/api/admin/**").hasRole(Const.ROLE_ADMIN)
                        // 其他所有接口需要DEFAULT或ADMIN角色
                        .anyRequest().hasAnyRole(Const.ROLE_DEFAULT, Const.ROLE_ADMIN)
                )
                // 配置表单登录
                .formLogin(conf -> conf
                        // 设置登录处理URL
                        .loginProcessingUrl("/api/auth/login")
                        // 设置登录失败处理器
                        .failureHandler(this::handleProcess)
                        // 设置登录成功处理器
                        .successHandler(this::handleProcess)
                        // 允许所有用户访问登录接口
                        .permitAll()
                )
                // 配置登出
                .logout(conf -> conf
                        // 设置登出URL
                        .logoutUrl("/api/auth/logout")
                        // 设置登出成功处理器
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 配置异常处理
                .exceptionHandling(conf -> conf
                        // 设置访问拒绝处理器（权限不足时）
                        .accessDeniedHandler(this::handleProcess)
                        // 设置认证入口点（未认证时）
                        .authenticationEntryPoint(this::handleProcess)
                )
                // 禁用CSRF保护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加请求日志过滤器到认证过滤器之前
                .addFilterBefore(requestLogFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加JWT认证过滤器到请求日志过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, RequestLogFilter.class)
                .build();
    }

    /**
     * 统一的请求处理方法
     * 将多种类型的Handler整合到同一个方法中，包含：
     * - 登录成功处理：生成JWT令牌并返回用户信息
     * - 登录失败处理：返回错误信息
     * - 未登录拦截：返回未认证错误
     * - 无权限拦截：返回权限不足错误
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象  
     * @param exceptionOrAuthentication 异常对象或认证对象
     * @throws IOException 输出响应时可能出现的IO异常
     */
    private void handleProcess(HttpServletRequest request,
                               HttpServletResponse response,
                               Object exceptionOrAuthentication) throws IOException {
        // 设置响应内容类型为JSON格式
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        
        if(exceptionOrAuthentication instanceof AccessDeniedException exception) {
            // 处理访问拒绝异常（权限不足）
            writer.write(RestBean
                    .forbidden(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Exception exception) {
            // 处理其他认证异常（未登录等）
            writer.write(RestBean
                    .unauthorized(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Authentication authentication){
            // 处理登录成功的情况
            User user = (User) authentication.getPrincipal();
            Account account = service.findAccountByNameOrEmail(user.getUsername());
            
            // 检查账户是否被封禁
            if(account.isBanned()) {
                writer.write(RestBean.forbidden("登录失败，此账户已被封禁").asJsonString());
                return;
            }
            
            // 生成JWT令牌
            String jwt = utils.createJwt(user, account.getUsername(), account.getId());
            if(jwt == null) {
                // JWT生成失败（可能是请求过于频繁）
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                // 登录成功，返回用户信息和JWT令牌
                AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, o -> o.setToken(jwt));
                vo.setExpire(utils.expireTime());
                writer.write(RestBean.success(vo).asJsonString());
            }
        }
    }

    /**
     * 登出成功处理方法
     * 将对应的JWT令牌列入黑名单，使其失效
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param authentication 当前用户的认证信息
     * @throws IOException 输出响应时可能出现的IO异常
     */
    private void onLogoutSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication) throws IOException {
        // 设置响应内容类型为JSON格式
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        
        // 从请求头中获取JWT令牌
        String authorization = request.getHeader("Authorization");
        
        // 将JWT令牌加入黑名单使其失效
        if(utils.invalidateJwt(authorization)) {
            writer.write(RestBean.success("退出登录成功").asJsonString());
            return;
        }
        
        // 令牌失效操作失败
        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
    }
}
