package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


//TODO : 在部署到服务器的时候通过 Nginx部署的时候，因为使用了Nginx反代，根据反代的原理，其实这样获取的IP是错误的,永远都是本地IP需要修复 (BugFish注释)
//TODO : 上一条TODO，已经写了新的IpUtils进行替换，在反代的时候，从请求头中寻找真实的IP，并且直连也支持，需要部署测试暂时没测试(BugFish注释)
//TODO : 补上一条TODO，内部的限流，也需要改用这种IP
/**
 * 系统工具控制器
 * 提供系统级别的工具接口，为前端和其他服务提供辅助功能
 * 
 * 主要功能：
 * - IP地址获取：获取客户端的真实IP地址（支持Nginx反向代理）
 * - 系统信息查询：提供系统状态和配置信息
 *
 * @author GuguFish
 */
@Slf4j
@RestController
public class UntilController {
    
    /**
     * 获取客户端真实IP地址
     * 支持从Nginx反向代理环境中获取真实客户端IP
     * 返回发起请求的客户端的IP地址，用于：
     * - 前端显示用户当前IP
     * - 系统日志记录
     * - 安全审计
     *
     * 此接口无需身份验证，任何人都可以调用
     * 
     * @param request HTTP请求对象，用于获取客户端IP地址
     * @return 包含客户端真实IP地址的响应结果
     */
    @GetMapping("/api/util/ip")
    public RestBean<String> ip(HttpServletRequest request){
        String realIp = IpUtils.getRealClientIp(request);
        return RestBean.success(realIp);
    }
}
