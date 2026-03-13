package cn.gugufish.entity.vo.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TopicSearchVO {
    int id;
    String title;
    String intro;
    int type;
    Map<String, List<String>> highlight;
}
