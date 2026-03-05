package cn.gugufish.config;

import cn.gugufish.utils.Const;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置类
 *
 * @author GuguFish
 */
@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean("errorQueue")
    public Queue dlQueue() {
        return QueueBuilder
                .durable(Const.MQ_ERROR)
                .ttl(24 * 60 * 60 * 1000)
                .build();
    }

    @Bean("errorExchange")
    public Exchange dlExchange() {
        return ExchangeBuilder
                .directExchange("dlx.direct")
                .build();
    }

    @Bean
    public Binding dlBinding(@Qualifier("errorExchange") Exchange exchange,
                             @Qualifier("errorQueue") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("error-message")
                .noargs();
    }

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
                .durable(Const.MQ_MAIL)
                .deadLetterExchange("dlx.direct")
                .deadLetterRoutingKey("error-message")
                .ttl(3 * 60 * 1000)
                .build();

    }
    
}
