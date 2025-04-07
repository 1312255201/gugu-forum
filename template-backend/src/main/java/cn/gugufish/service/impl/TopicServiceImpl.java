package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.TopicType;
import cn.gugufish.mapper.TopicTypeMapper;
import cn.gugufish.service.TopicService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {
    @Resource
    TopicTypeMapper mapper;
    @Override
    public List<TopicType> listTypes() {
        return mapper.selectList(null);
    }
}
