package cn.gugufish.service.impl;

import cn.gugufish.service.AiService;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {

    @Resource
    ChatModel chatModel;

    @Override
    public String chatWithAi(JSONArray content) {
        List<? extends AbstractMessage> list = content.stream().map(item -> {
            JSONObject obj = JSONObject.from(item);
            return switch (obj.getString("type")) {
                case "user" -> new UserMessage(obj.getString("text"));
                case "assistant" -> new AssistantMessage(obj.getString("text"));
                default -> throw new RuntimeException();
            };
        }).toList();
        return chatModel.call(list.toArray(new Message[0]));
    }
}
