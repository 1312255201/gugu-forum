package cn.gugufish.filter;

import cn.gugufish.utils.Const;
import cn.gugufish.utils.JwtUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT身份认证过滤器
 * 负责处理HTTP请求头中的JWT令牌，实现无状态的用户身份认证
 * 
 * 主要功能：
 * - JWT令牌解析和验证：从请求头中提取并验证JWT令牌的有效性
 * - 用户身份设置：将有效的用户信息设置到Spring Security上下文中
 * - 封禁用户检查：检查用户是否被管理员封禁，封禁用户无法通过认证
 * - 令牌黑名单管理：失效的令牌将被加入黑名单，防止重复使用
 * - 请求属性注入：将用户ID注入到请求属性中，供后续业务使用
 * 
 * 工作流程：
 * 1. 从HTTP请求头的Authorization字段中提取JWT令牌
 * 2. 使用JwtUtils验证令牌的签名和有效期
 * 3. 检查用户是否被封禁（查询Redis黑名单）
 * 4. 如果令牌有效且用户未被封禁，设置Spring Security认证信息
 * 5. 将用户ID存储到请求属性中，供Controller层使用
 * 6. 如果用户被封禁，将令牌加入黑名单并拒绝认证
 * 
 * 安全特性：
 * - 令牌签名验证：确保令牌未被篡改
 * - 有效期检查：自动拒绝过期令牌
 * - 封禁用户拦截：实时检查用户封禁状态
 * - 令牌黑名单：防止已失效令牌的重复使用
 * 
 * @author GuguFish
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT工具类
     * 提供JWT令牌的生成、解析、验证等核心功能
     */
    @Resource
    JwtUtils utils;
    
    /**
     * Redis模板
     * 用于检查用户封禁状态和管理令牌黑名单
     */
    @Resource
    StringRedisTemplate template;

    /**
     * JWT令牌过期时间配置（小时）
     * 从配置文件读取：spring.security.jwt.expire
     * 用于令牌有效期的相关计算
     */
    @Value("${spring.security.jwt.expire}")
    private int expire;
    
    /**
     * 过滤器核心处理方法
     * 对每个HTTP请求进行JWT身份认证处理
     * 
     * 认证流程：
     * 1. 提取请求头中的Authorization字段
     * 2. 解析和验证JWT令牌
     * 3. 检查用户封禁状态
     * 4. 设置Spring Security认证上下文
     * 5. 注入用户ID到请求属性
     * 6. 继续执行后续过滤器
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     * @throws ServletException Servlet处理异常
     * @throws IOException IO操作异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取Authorization字段（格式：Bearer <token>）
        String authorization = request.getHeader("Authorization");
        
        // 解析JWT令牌，验证签名和有效期
        DecodedJWT jwt = utils.resolveJwt(authorization);
        
        if(jwt != null) {
            // JWT令牌有效，提取用户信息
            UserDetails user = utils.toUser(jwt);
            
            // 检查用户是否被封禁（查询Redis黑名单）
            if(!template.hasKey(Const.BANNED_BLOCK + utils.toId(jwt))) {
                // 用户未被封禁，设置Spring Security认证信息
                
                // 创建认证令牌，包含用户信息和权限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                
                // 设置认证详情（包含请求IP、会话ID等）
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 将认证信息设置到Spring Security上下文中
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 将用户ID注入到请求属性中，供Controller层使用
                request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
            } else {
                // 用户已被封禁，将令牌加入黑名单并拒绝认证
                utils.invalidateJwt(authorization);
            }
        }
        
        // 继续执行后续过滤器（无论认证是否成功）
        filterChain.doFilter(request, response);
    }
}
