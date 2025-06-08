package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.dto.Interact;
import cn.gugufish.entity.vo.request.AddCommentVO;
import cn.gugufish.entity.vo.request.TopicCreateVO;
import cn.gugufish.entity.vo.request.TopicUpdateVO;
import cn.gugufish.entity.vo.response.*;
import cn.gugufish.service.AccountService;
import cn.gugufish.service.TopicService;
import cn.gugufish.service.WeatherService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 论坛核心控制器
 * 提供论坛系统的核心功能，是整个论坛系统的主要业务入口
 * 主要功能模块包括：
 * - 天气信息服务：为用户提供实时天气数据
 * - 主题管理：创建、查看、编辑、删除论坛主题
 * - 互动功能：点赞、收藏主题内容
 * - 评论系统：发表、查看、删除主题评论
 * - 分类浏览：按主题类型分类浏览内容
 * - 用户权限控制：禁言用户无法发布内容
 * 
 * @author GuguFish
 */
@RestController
@Slf4j
@RequestMapping("/api/forum")
public class ForumController {
    
    /**
     * 天气服务类
     * 提供基于地理位置的天气信息查询功能
     */
    @Resource
    WeatherService weatherService;
    
    /**
     * 主题服务类
     * 提供论坛主题相关的核心业务逻辑
     */
    @Resource
    TopicService topicService;
    
    /**
     * 控制器工具类
     * 提供通用的请求处理和响应封装功能
     */
    @Resource
    ControllerUtils controllerUtils;
    
    /**
     * 账户服务类
     * 提供用户账户相关的业务逻辑，用于权限验证
     */
    @Resource
    AccountService accountService;
    
    /**
     * 获取指定地理位置的天气信息
     * 根据用户提供的经纬度坐标，调用天气服务获取当前天气状况
     * 
     * @param longitude 经度坐标
     * @param latitude 纬度坐标
     * @return 天气信息响应，包含温度、天气状况、风速等数据
     */
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(double longitude, double latitude){
        WeatherVO vo = weatherService.fetchWeather(longitude, latitude);
        return vo == null ? 
                RestBean.failure(400, "获取天气失败，请联系管理员！") : RestBean.success(vo);
    }
    
    /**
     * 获取所有主题分类列表
     * 返回论坛支持的所有主题分类，供用户在发布主题时选择
     * 
     * @return 主题分类列表，包含分类ID和分类名称
     */
    @GetMapping("/types")
    public RestBean<List<TopicTypeVO>> listTypes(){
        return RestBean.success(topicService
                .listTypes()
                .stream()
                .map(type -> type.asViewObject(TopicTypeVO.class))
                .toList());
    }
    
    /**
     * 创建新的论坛主题
     * 用户发布新主题到论坛，需要验证用户是否被禁言
     * 被禁言的用户无法创建新主题
     * 
     * @param id 当前登录用户的ID（从JWT令牌中获取）
     * @param vo 主题创建请求对象，包含标题、内容、分类等信息
     * @return 创建结果，成功返回成功响应，失败返回错误信息
     */
    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @Valid @RequestBody TopicCreateVO vo){
        Account account = accountService.findAccountById(id);
        if(account.isMute()) {
            return RestBean.forbidden("您已被禁言，无法创建新的主题");
        }
        return controllerUtils.messageHandle(() -> topicService.createTopic(id, vo));
    }
    
    /**
     * 分页获取主题列表
     * 支持按分类筛选和分页浏览论坛主题
     * 
     * @param page 页码（从0开始）
     * @param type 主题分类ID，0表示查看所有分类
     * @return 指定页面的主题预览列表
     */
    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewVO>> listTopic(@RequestParam @Min(0) int page,
                                                    @RequestParam @Min(0) int type){
        return RestBean.success(topicService.listTopicByPage(page + 1, type));
    }
    
    /**
     * 获取置顶主题列表
     * 返回管理员设置的置顶主题，通常显示在论坛首页顶部
     * 
     * @return 置顶主题列表
     */
    @GetMapping("/top-topic")
    public RestBean<List<TopicTopVO>> topTopic(){
        return RestBean.success(topicService.listTopTopics());
    }
    
    /**
     * 获取主题详细内容
     * 根据主题ID获取完整的主题信息，包括内容、作者、发布时间等
     * 
     * @param tid 主题ID
     * @param id 当前登录用户的ID（用于判断用户是否已点赞、收藏等）
     * @return 主题的详细信息
     */
    @GetMapping("/topic")
    public RestBean<TopicDetailVO> topic(@RequestParam @Min(0) int tid,
                                         @RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(topicService.getTopic(tid, id));
    }
    
    /**
     * 处理用户与主题的互动操作
     * 支持点赞和收藏两种互动类型，用户可以取消之前的操作
     * 
     * @param tid 主题ID
     * @param type 互动类型（"like"表示点赞，"collect"表示收藏）
     * @param state 操作状态（true表示添加互动，false表示取消互动）
     * @param id 当前登录用户的ID
     * @return 操作结果
     */
    @GetMapping("/interact")
    public RestBean<Void> interact(@RequestParam @Min(0) int tid,
                                   @RequestParam @Pattern(regexp = "(like|collect)") String type,
                                   @RequestParam boolean state,
                                   @RequestAttribute(Const.ATTR_USER_ID) int id) {
        topicService.interact(new Interact(tid, id, new Date(), type), state);
        return RestBean.success();
    }
    
    /**
     * 获取用户收藏的主题列表
     * 返回当前用户收藏的所有主题
     * 
     * @param id 当前登录用户的ID
     * @return 用户收藏的主题列表
     */
    @GetMapping("/collects")
    public RestBean<List<TopicPreviewVO>> collects(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(topicService.listTopicCollects(id));
    }
    
    /**
     * 更新主题内容
     * 用户可以编辑自己发布的主题内容
     * 
     * @param vo 主题更新请求对象，包含修改后的内容
     * @param id 当前登录用户的ID
     * @return 更新结果
     */
    @PostMapping("/update-topic")
    public RestBean<Void> updateTopic(@Valid @RequestBody TopicUpdateVO vo,
                                      @RequestAttribute(Const.ATTR_USER_ID) int id){
        return controllerUtils.messageHandle(() -> topicService.updateTopic(id, vo));
    }
    
    /**
     * 添加主题评论
     * 用户对主题发表评论，需要验证用户是否被禁言
     * 被禁言的用户无法发表评论
     * 
     * @param vo 评论添加请求对象，包含评论内容
     * @param id 当前登录用户的ID
     * @return 添加结果
     */
    @PostMapping("/add-comment")
    public RestBean<Void> addComment(@Valid @RequestBody AddCommentVO vo,
                                     @RequestAttribute(Const.ATTR_USER_ID) int id){
        Account account = accountService.findAccountById(id);
        if(account.isMute()) {
            return RestBean.forbidden("您已被禁言，无法创建新的回复");
        }
        return controllerUtils.messageHandle(() -> topicService.createComment(id, vo));
    }
    
    /**
     * 分页获取主题评论列表
     * 获取指定主题的评论内容，支持分页浏览
     * 
     * @param tid 主题ID
     * @param page 页码（从0开始）
     * @return 指定页面的评论列表
     */
    @GetMapping("/comments")
    public RestBean<List<CommentVO>> comments(@RequestParam @Min(0) int tid,
                                              @RequestParam @Min(0) int page){
        return RestBean.success(topicService.comments(tid, page + 1));
    }
    
    /**
     * 删除评论
     * 用户可以删除自己发表的评论
     * 
     * @param id 评论ID
     * @param uid 当前登录用户的ID
     * @return 删除结果
     */
    @GetMapping("/delete-comment")
    public RestBean<Void> deleteComment(@RequestParam @Min(0) int id,
                                        @RequestAttribute(Const.ATTR_USER_ID) int uid){
        topicService.deleteComment(id, uid);
        return RestBean.success();
    }
}
