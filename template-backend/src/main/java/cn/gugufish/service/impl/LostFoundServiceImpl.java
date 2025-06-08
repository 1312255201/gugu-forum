package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.LostFound;
import cn.gugufish.entity.vo.request.LostFoundCreateVO;
import cn.gugufish.entity.vo.response.LostFoundVO;
import cn.gugufish.mapper.LostFoundMapper;
import cn.gugufish.service.LostFoundService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 失物招领服务实现类
 */
@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {
    
    @Resource
    LostFoundMapper mapper;
    
    @Override
    public boolean createLostFound(int uid, LostFoundCreateVO vo) {
        try {
            LostFound lostFound = new LostFound();
            BeanUtils.copyProperties(vo, lostFound);
            lostFound.setUid(uid);
            lostFound.setImages(JSON.toJSONString(vo.getImages()));
            lostFound.setStatus(0);  // 默认状态为寻找中
            lostFound.setCreateTime(new Date());
            lostFound.setUpdateTime(new Date());
            return mapper.insert(lostFound) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<LostFoundVO> getLostFoundList(String location, Date startTime, Date endTime, Integer status) {
        List<Map<String, Object>> list = mapper.selectByConditionsWithUsername(location, startTime, endTime, status);
        return list.stream().map(this::convertMapToVO).collect(Collectors.toList());
    }
    
    @Override
    public LostFoundVO getLostFoundById(int id) {
        Map<String, Object> result = mapper.selectByIdWithUsername(id);
        return result != null ? convertMapToVO(result) : null;
    }
    
    @Override
    public boolean updateStatus(int id, int uid, int status) {
        UpdateWrapper<LostFound> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).eq("uid", uid);
        wrapper.set("status", status);
        wrapper.set("update_time", new Date());
        return mapper.update(null, wrapper) > 0;
    }
    
    @Override
    public boolean deleteLostFound(int id, int uid) {
        QueryWrapper<LostFound> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("uid", uid);
        return mapper.delete(wrapper) > 0;
    }
    
    /**
     * 转换Map为VO
     */
    private LostFoundVO convertMapToVO(Map<String, Object> map) {
        LostFoundVO vo = new LostFoundVO();
        
        // 基本字段映射
        vo.setId((Integer) map.get("id"));
        vo.setUid((Integer) map.get("uid"));
        vo.setUsername((String) map.get("username"));
        vo.setTitle((String) map.get("title"));
        vo.setDescription((String) map.get("description"));
        vo.setLocation((String) map.get("location"));
        vo.setContactInfo((String) map.get("contact_info"));
        vo.setStatus((Integer) map.get("status"));
        
        // 处理时间字段转换
        vo.setLostTime(convertToDate(map.get("lost_time")));
        vo.setCreateTime(convertToDate(map.get("create_time")));
        vo.setUpdateTime(convertToDate(map.get("update_time")));
        
        // 解析图片JSON字符串
        try {
            String imagesStr = (String) map.get("images");
            if (imagesStr != null && !imagesStr.isEmpty()) {
                List<String> imageList = JSON.parseObject(imagesStr, List.class);
                vo.setImages(imageList);
            } else {
                vo.setImages(List.of());
            }
        } catch (Exception e) {
            vo.setImages(List.of());
        }
        
        // 设置状态文本
        Integer status = vo.getStatus();
        if (status != null) {
            switch (status) {
                case 0 -> vo.setStatusText("寻找中");
                case 1 -> vo.setStatusText("已找到");
                case 2 -> vo.setStatusText("已过期");
                default -> vo.setStatusText("未知状态");
            }
        }
        
        return vo;
    }
    
    /**
     * 将时间对象转换为Date类型
     */
    private Date convertToDate(Object timeObj) {
        if (timeObj == null) {
            return null;
        }
        
        if (timeObj instanceof Date) {
            return (Date) timeObj;
        } else if (timeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (timeObj instanceof java.sql.Timestamp) {
            return new Date(((java.sql.Timestamp) timeObj).getTime());
        }
        
        // 如果都不是，尝试toString然后解析
        return null;
    }
}