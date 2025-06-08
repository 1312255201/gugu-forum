package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.request.ConfirmResetVO;
import cn.gugufish.entity.vo.request.EmailRegisterVO;
import cn.gugufish.entity.vo.request.EmailResetVO;
import cn.gugufish.service.AccountService;
import cn.gugufish.utils.ControllerUtils;
import cn.gugufish.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * 用户认证控制器
 * 处理用户身份认证相关的所有操作，包括注册、登录验证、密码重置等核心功能
 * 这是用户进入系统的第一道门槛，确保系统安全性
 * 
 * 主要功能模块：
 * - 邮箱验证码服务：为注册、重置密码等操作提供邮箱验证
 * - 用户注册：基于邮箱的用户注册流程
 * - 密码重置：忘记密码时的密码重置流程
 * 
 * 注意：实际的登录操作由Spring Security自动处理，不在此控制器中
 * 
 * @author GuguFish
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "包括用户登录、注册、验证码请求等操作。")
public class AuthorizeController {

    /**
     * 账户服务类
     * 提供用户账户相关的核心业务逻辑
     */
    @Resource
    AccountService accountService;
    
    /**
     * 控制器工具类
     * 提供通用的请求处理和响应封装功能
     */
    @Resource
    ControllerUtils utils;
    
    /**
     * 请求邮箱验证码
     * 根据不同的操作类型发送相应的验证码邮件
     * 支持的操作类型：
     * - register: 用户注册验证
     * - reset: 密码重置验证  
     * - modify: 邮箱修改验证
     * 
     * 为防止恶意请求，会记录请求IP并进行频率限制
     * 
     * @param email 接收验证码的邮箱地址
     * @param type 验证码类型（register/reset/modify）
     * @param request HTTP请求对象，用于获取客户端IP地址
     * @return 发送结果，成功返回成功响应，失败返回具体错误信息
     */
    @GetMapping("/ask-code")
    @Operation(summary = "请求邮件验证码")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request){
        return utils.messageHandle(() ->
                accountService.registerEmailVerifyCode(type, String.valueOf(email), IpUtils.getRealClientIp(request)));
        //TODO : 整体代码修复添加新IP工具类， 用来适配部署服务器后Nginx反代导致IP错误的问题，本工具类没有测试临时添加TODO
    }

    /**
     * 用户注册操作
     * 基于邮箱的用户注册流程，需要先通过邮箱验证码验证
     * 注册流程：
     * 1. 用户提供邮箱并请求验证码
     * 2. 用户输入验证码、用户名、密码等信息
     * 3. 系统验证信息有效性并创建账户
     * 
     * @param vo 注册信息对象，包含邮箱、验证码、用户名、密码等
     * @return 注册结果，成功返回成功响应，失败返回具体错误信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册操作")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return utils.messageHandle(() ->
                accountService.registerEmailAccount(vo));
    }

    /**
     * 密码重置确认操作
     * 密码重置流程的第一步：验证邮箱和验证码的有效性
     * 只有验证通过后，用户才能进行下一步的密码设置
     * 
     * 此步骤不会实际重置密码，仅确认用户身份的合法性
     * 
     * @param vo 重置确认信息对象，包含邮箱和验证码
     * @return 确认结果，验证通过返回成功响应，否则返回错误信息
     */
    @PostMapping("/reset-confirm")
    @Operation(summary = "密码重置确认")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return utils.messageHandle(() -> accountService.resetConfirm(vo));
    }

    /**
     * 执行密码重置操作
     * 密码重置流程的第二步：实际设置新密码
     * 需要先通过resetConfirm验证，然后才能调用此接口
     * 
     * 成功重置后，用户可以使用新密码登录系统
     * 
     * @param vo 密码重置信息对象，包含邮箱、验证码和新密码
     * @return 重置结果，成功返回成功响应，失败返回具体错误信息
     */
    @PostMapping("/reset-password")
    @Operation(summary = "密码重置操作")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo){
        return utils.messageHandle(() ->
                accountService.resetEmailAccountPassword(vo));
    }
}
