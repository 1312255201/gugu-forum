package cn.gugufish.service;

import cn.gugufish.entity.dto.Topic;
import cn.gugufish.entity.dto.TopicType;
import cn.gugufish.entity.vo.request.TopicCreateVO;
import cn.gugufish.entity.vo.response.TopicPreviewVO;
import cn.gugufish.entity.vo.response.TopicTopVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> listTypes();
    String createTopic(int uid, TopicCreateVO vo);
    List<TopicPreviewVO> listTopicByPage(int page, int type);
    List<TopicTopVO> listTopTopics();
}
