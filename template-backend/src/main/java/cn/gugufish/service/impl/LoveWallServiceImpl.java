package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.LoveWall;
import cn.gugufish.entity.vo.request.LoveWallCreateVO;
import cn.gugufish.entity.vo.response.LoveWallVO;
import cn.gugufish.mapper.LoveWallMapper;
import cn.gugufish.service.LoveWallService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表白墙服务实现类
 */
@Service
public class LoveWallServiceImpl implements LoveWallService {
    
    @Resource
    LoveWallMapper mapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean createLoveWall(int uid, LoveWallCreateVO vo) {
        try {
            LoveWall loveWall = new LoveWall();
            BeanUtils.copyProperties(vo, loveWall);
            loveWall.setUid(uid);
            loveWall.setStatus(0); // 默认待审核
            loveWall.setLikeCount(0);
            loveWall.setCreateTime(LocalDateTime.now());
            loveWall.setUpdateTime(LocalDateTime.now());
            
            // 处理照片列表
            if (vo.getPhotos() != null && !vo.getPhotos().isEmpty()) {
                loveWall.setPhotos(objectMapper.writeValueAsString(vo.getPhotos()));
            }
            
            // 处理标签列表
            if (vo.getTags() != null && !vo.getTags().isEmpty()) {
                loveWall.setTags(objectMapper.writeValueAsString(vo.getTags()));
            }
            
            return mapper.insert(loveWall) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<LoveWallVO> getApprovedLoveWallList(Integer uid) {
        List<LoveWall> list = mapper.selectApprovedList();
        return list.stream().map(loveWall -> convertToVO(loveWall, uid)).collect(Collectors.toList());
    }
    
    @Override
    public List<LoveWallVO> getLoveWallListByGender(Integer gender, Integer uid) {
        List<LoveWall> list = mapper.selectApprovedListByGender(gender);
        return list.stream().map(loveWall -> convertToVO(loveWall, uid)).collect(Collectors.toList());
    }
    
    @Override
    public List<LoveWallVO> getLoveWallListByAgeRange(Integer minAge, Integer maxAge, Integer uid) {
        List<LoveWall> list = mapper.selectApprovedListByAgeRange(minAge, maxAge);
        return list.stream().map(loveWall -> convertToVO(loveWall, uid)).collect(Collectors.toList());
    }
    
    @Override
    public LoveWallVO getLoveWallById(int id, Integer uid) {
        LoveWall loveWall = mapper.selectById(id);
        return loveWall != null ? convertToVO(loveWall, uid) : null;
    }
    
    @Override
    public List<LoveWallVO> getMyLoveWallList(int uid) {
        List<LoveWall> list = mapper.selectByUid(uid);
        return list.stream().map(loveWall -> convertToVO(loveWall, uid)).collect(Collectors.toList());
    }
    
    @Override
    public boolean likeLoveWall(int id) {
        return mapper.increaseLikeCount(id) > 0;
    }
    
    @Override
    public boolean deleteLoveWall(int id, int uid) {
        LoveWall loveWall = mapper.selectById(id);
        if (loveWall != null && loveWall.getUid().equals(uid)) {
            return mapper.deleteById(id) > 0;
        }
        return false;
    }
    
    @Override
    public boolean updateLoveWall(int id, int uid, LoveWallCreateVO vo) {
        try {
            LoveWall existing = mapper.selectById(id);
            if (existing == null || !existing.getUid().equals(uid)) {
                return false;
            }
            
            LoveWall loveWall = new LoveWall();
            BeanUtils.copyProperties(vo, loveWall);
            loveWall.setId(id);
            loveWall.setStatus(0); // 修改后重新审核
            loveWall.setUpdateTime(LocalDateTime.now());
            
            // 处理照片列表
            if (vo.getPhotos() != null && !vo.getPhotos().isEmpty()) {
                loveWall.setPhotos(objectMapper.writeValueAsString(vo.getPhotos()));
            }
            
            // 处理标签列表
            if (vo.getTags() != null && !vo.getTags().isEmpty()) {
                loveWall.setTags(objectMapper.writeValueAsString(vo.getTags()));
            }
            
            UpdateWrapper<LoveWall> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);
            return mapper.update(loveWall, wrapper) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<LoveWallVO> getPendingLoveWallList() {
        List<LoveWall> list = mapper.selectPendingList();
        return list.stream().map(loveWall -> convertToVO(loveWall, null)).collect(Collectors.toList());
    }
    
    @Override
    public boolean approveLoveWall(int id, int status) {
        UpdateWrapper<LoveWall> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        wrapper.set("status", status);
        wrapper.set("update_time", LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }
    
    /**
     * 转换为VO
     */
    private LoveWallVO convertToVO(LoveWall loveWall, Integer currentUid) {
        LoveWallVO vo = new LoveWallVO();
        BeanUtils.copyProperties(loveWall, vo);
        
        // 处理时间转换
        if (loveWall.getCreateTime() != null) {
            vo.setCreateTime(Date.from(loveWall.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        // 处理照片列表
        if (loveWall.getPhotos() != null && !loveWall.getPhotos().isEmpty()) {
            try {
                List<String> photos = objectMapper.readValue(loveWall.getPhotos(), new TypeReference<List<String>>() {});
                vo.setPhotos(photos);
            } catch (JsonProcessingException e) {
                vo.setPhotos(new ArrayList<>());
            }
        } else {
            vo.setPhotos(new ArrayList<>());
        }
        
        // 处理标签列表
        if (loveWall.getTags() != null && !loveWall.getTags().isEmpty()) {
            try {
                List<String> tags = objectMapper.readValue(loveWall.getTags(), new TypeReference<List<String>>() {});
                vo.setTags(tags);
            } catch (JsonProcessingException e) {
                vo.setTags(new ArrayList<>());
            }
        } else {
            vo.setTags(new ArrayList<>());
        }
        
        // 设置性别文本
        Integer gender = loveWall.getGender();
        if (gender != null) {
            switch (gender) {
                case 0 -> vo.setGenderText("女");
                case 1 -> vo.setGenderText("男");
                case 2 -> vo.setGenderText("其他");
                default -> vo.setGenderText("未知");
            }
        }
        
        // 设置状态文本
        Integer status = loveWall.getStatus();
        if (status != null) {
            switch (status) {
                case 0 -> vo.setStatusText("待审核");
                case 1 -> vo.setStatusText("已通过");
                case 2 -> vo.setStatusText("已拒绝");
                default -> vo.setStatusText("未知状态");
            }
        }
        
        // 设置是否为当前用户发布的
        vo.setIsMine(currentUid != null && currentUid.equals(loveWall.getUid()));
        
        return vo;
    }
} 