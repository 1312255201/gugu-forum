package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.Topic;
import cn.gugufish.entity.dto.TopicType;
import cn.gugufish.entity.vo.request.TopicCreateVO;
import cn.gugufish.mapper.TopicMapper;
import cn.gugufish.mapper.TopicTypeMapper;
import cn.gugufish.service.TopicService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.FlowUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    TopicTypeMapper mapper;
    @Resource
    FlowUtils flowUtils;
    @Override
    public List<TopicType> listTypes() {
        return mapper.selectList(null);
    }
    private Set<Integer> types = null;
    @PostConstruct
    private void initTypes() {
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public String createTopic(int uid, TopicCreateVO vo) {
        if(!textLimitCheck(vo.getContent()))
            return "字数超出限制，发送失败";
        if(!types.contains(vo.getType()))
            return "文章类型非法，发送失败";//封号qwq你在干什么
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if(!flowUtils.limitPeriodCounterCheck(key,4,3600))
            return "发文过快，请稍后再试";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo, topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save(topic)){
            return null;
        }
        else{
            return "发生内部错误，请联系管理员";
        }
    }
    private boolean textLimitCheck(JSONObject object){
        if(object == null){return false;}
        long length = 0;
        for (Object op : object.getJSONArray("ops")) {
            length += JSONObject.from(op).getString("insert").length();
            if(length > 10000)return false;
        }
        return true;
    }
}
