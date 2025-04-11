package cn.gugufish.service;

import cn.gugufish.entity.dto.Topic;
import cn.gugufish.entity.dto.TopicType;
import cn.gugufish.entity.dto.Interact;
import cn.gugufish.entity.vo.request.TopicCreateVO;
import cn.gugufish.entity.vo.request.TopicUpdateVO;
import cn.gugufish.entity.vo.response.TopicDetailVO;
import cn.gugufish.entity.vo.response.TopicPreviewVO;
import cn.gugufish.entity.vo.response.TopicTopVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> listTypes();
    String createTopic(int uid, TopicCreateVO vo);
    List<TopicPreviewVO> listTopicByPage(int page, int type);
    List<TopicTopVO> listTopTopics();
    TopicDetailVO getTopic(int tid, int uid);
    void interact(Interact interact, boolean state);
    List<TopicPreviewVO> listTopicCollects(int uid);
    String updateTopic(int uid, TopicUpdateVO vo);
}
