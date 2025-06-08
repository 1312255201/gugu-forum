package cn.gugufish.service;

import cn.gugufish.entity.vo.request.LoveWallCreateVO;
import cn.gugufish.entity.vo.response.LoveWallVO;

import java.util.List;

/**
 * 表白墙服务接口
 */
public interface LoveWallService {
    
    /**
     * 创建表白墙信息
     */
    boolean createLoveWall(int uid, LoveWallCreateVO vo);
    
    /**
     * 获取已审核通过的表白墙列表
     */
    List<LoveWallVO> getApprovedLoveWallList(Integer uid);
    
    /**
     * 根据性别筛选表白墙列表
     */
    List<LoveWallVO> getLoveWallListByGender(Integer gender, Integer uid);
    
    /**
     * 根据年龄范围筛选表白墙列表
     */
    List<LoveWallVO> getLoveWallListByAgeRange(Integer minAge, Integer maxAge, Integer uid);
    
    /**
     * 根据ID获取表白墙详情
     */
    LoveWallVO getLoveWallById(int id, Integer uid);
    
    /**
     * 获取用户自己发布的表白墙列表
     */
    List<LoveWallVO> getMyLoveWallList(int uid);
    
    /**
     * 点赞表白墙
     */
    boolean likeLoveWall(int id);
    
    /**
     * 删除表白墙（仅作者）
     */
    boolean deleteLoveWall(int id, int uid);
    
    /**
     * 更新表白墙信息（仅作者）
     */
    boolean updateLoveWall(int id, int uid, LoveWallCreateVO vo);
    
    /**
     * 获取待审核的表白墙列表（管理员）
     */
    List<LoveWallVO> getPendingLoveWallList();
    
    /**
     * 审核表白墙（管理员）
     */
    boolean approveLoveWall(int id, int status);
} 