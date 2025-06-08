package cn.gugufish.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置类
 *
 * @author GuguFish
 */
@Configuration
public class RabbitConfiguration {
    
    /**
     * 创建邮件发送队列
     * 配置一个持久化的邮件队列，用于异步处理邮件发送任务
     * 当系统需要发送邮件时（如注册验证、密码重置等），
     * 会将邮件任务放入此队列，由专门的消费者异步处理
     * 
     * @return 邮件队列实例，队列名称为"mail"
     */
    @Bean("mailQueue")
    public Queue queue(){
        return QueueBuilder
                // 创建持久化队列，服务器重启后队列仍然存在
                .durable("mail")
                .build();
    }
    
    /**
     * 配置消息转换器
     * 使用Jackson2JsonMessageConverter将Java对象转换为JSON格式的消息
     * 这样可以在消息队列中传递复杂的对象数据，而不仅仅是简单的字符串
     * 
     * @return JSON消息转换器，用于序列化和反序列化消息对象
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
