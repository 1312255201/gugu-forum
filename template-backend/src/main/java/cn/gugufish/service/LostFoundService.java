package cn.gugufish.service;

import cn.gugufish.entity.dto.LostFound;
import cn.gugufish.entity.vo.request.LostFoundCreateVO;
import cn.gugufish.entity.vo.response.LostFoundVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * 失物招领服务接口
 */
public interface LostFoundService extends IService<LostFound> {
    
    /**
     * 创建失物招领信息
     */
    boolean createLostFound(int uid, LostFoundCreateVO vo);
    
    /**
     * 获取失物招领列表
     */
    List<LostFoundVO> getLostFoundList(String location, Date startTime, Date endTime, Integer status);
    
    /**
     * 根据ID获取失物招领详情
     */
    LostFoundVO getLostFoundById(int id);
    
    /**
     * 更新失物招领状态
     */
    boolean updateStatus(int id, int uid, int status);
    
    /**
     * 删除失物招领
     */
    boolean deleteLostFound(int id, int uid);
} 