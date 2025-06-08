package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.dto.AccountDetails;
import cn.gugufish.entity.dto.AccountPrivacy;
import cn.gugufish.entity.vo.request.*;
import cn.gugufish.entity.vo.response.AccountDetailsVO;
import cn.gugufish.entity.vo.response.AccountPrivacyVO;
import cn.gugufish.entity.vo.response.AccountVO;
import cn.gugufish.service.AccountDetailsService;
import cn.gugufish.service.AccountPrivacyService;
import cn.gugufish.service.AccountService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 用户账户管理控制器
 * 所有操作都需要用户身份验证，确保只能修改自己的账户信息
 * 
 * @author GuguFish
 */
@RestController
@RequestMapping("/api/user")
public class AccountController {

    @Resource
    AccountService accountService;
    @Resource
    AccountDetailsService accountDetailsService;
    @Resource
    AccountPrivacyService accountPrivacyService;
    
    /**
     * 控制器工具类
     * 提供通用的请求处理和响应封装功能
     */
    @Resource
    ControllerUtils utils;

    /**
     * 获取当前用户的基本账户信息
     * 返回用户的基础信息，如用户名、邮箱、注册时间等
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @return 用户的基本账户信息
     */
    @GetMapping("/info")
    public RestBean<AccountVO> info(@RequestAttribute(Const.ATTR_USER_ID) int id){
        Account account = accountService.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVO.class));
    }
    
    /**
     * 获取当前用户的详细资料信息
     * 返回用户的详细个人信息，如性别、生日、个人简介等
     * 如果用户还未设置详细信息，则返回空的详细信息对象
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @return 用户的详细资料信息
     */
    @GetMapping("/details")
    public RestBean<AccountDetailsVO> details(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDetails details = Optional
                .ofNullable(accountDetailsService.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        return RestBean.success(details.asViewObject(AccountDetailsVO.class));
    }
    
    /**
     * 保存用户的详细资料信息
     * 用户可以设置或更新个人详细信息，如昵称、性别、生日等
     * 需要验证用户名的唯一性，确保不与其他用户冲突
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 详细资料保存请求对象，包含要更新的详细信息
     * @return 保存结果，如果用户名已被占用则返回错误信息
     */
    @PostMapping("/save-details")
    public RestBean<Void> saveDetails(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid DetailsSaveVO vo){
        boolean success = accountDetailsService.saveAccountDetails(id, vo);
        return success ? RestBean.success() : RestBean.failure(400, "用户名已经被占用了QAQ");
    }
    
    /**
     * 修改用户邮箱地址
     * 用户可以更换绑定的邮箱地址，需要验证新邮箱的有效性
     * 通常需要邮箱验证码确认操作
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 邮箱修改请求对象，包含新邮箱和验证码
     * @return 修改结果，成功返回成功响应，失败返回具体错误信息
     */
    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid ModifyEmailVO vo){
        return utils.messageHandle(() -> accountService.modifyEmail(id, vo));
    }
    
    /**
     * 修改用户密码
     * 用户可以更改账户登录密码，需要验证原密码的正确性
     * 新密码需要符合安全强度要求
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 密码修改请求对象，包含原密码和新密码
     * @return 修改结果，成功返回成功响应，失败返回具体错误信息
     */
    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                         @RequestBody @Valid ChangePasswordVO vo){
        return utils.messageHandle(() -> accountService.changePassword(id, vo));
    }
    
    /**
     * 保存用户的隐私设置
     * 用户可以配置个人隐私偏好，如是否公开个人信息、接收通知设置等
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 隐私设置保存请求对象，包含各项隐私配置
     * @return 保存结果
     */
    @PostMapping("/save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid PrivacySaveVO vo){
        accountPrivacyService.savePrivacy(id, vo);
        return RestBean.success();
    }
    
    /**
     * 获取用户的隐私设置
     * 返回当前用户的隐私配置信息，供前端显示和编辑
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @return 用户的隐私设置信息
     */
    @GetMapping("/privacy")
    public RestBean<AccountPrivacyVO> privacy(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(accountPrivacyService.accountPrivacy(id).asViewObject(AccountPrivacyVO.class));
    }
}
