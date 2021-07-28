package com.hk.simba.mq.guards.infrastructure.config;

import com.aliyun.openservices.ons.api.impl.rocketmq.ProducerImpl;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


/**
 * @author Allen
 * @since 2021-06-29
 */
@Configuration
@ConditionalOnProperty(prefix = "mq.guards", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MqGuardsProperties.class)
public class MqConfig {
    private final MqGuardsProperties mqGuardsProperties;

    public MqConfig(MqGuardsProperties mqGuardsProperties) {
        this.mqGuardsProperties = mqGuardsProperties;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer createProducer() {
        Properties properties = new Properties();
        properties.put("AccessKey", mqGuardsProperties.getAccessKey());
        properties.put("SecretKey", mqGuardsProperties.getSecretKey());
        properties.put("NAMESRV_ADDR", mqGuardsProperties.getNameServerAddress());
        properties.put("SendMsgTimeoutMillis", mqGuardsProperties.getSendMsgTimeoutMillis() == null ? 6000 : mqGuardsProperties.getSendMsgTimeoutMillis());
        ProducerImpl producerImpl = new ProducerImpl(properties);
        DefaultMQProducer producer = producerImpl.getDefaultMQProducer();
        producer.setRetryTimesWhenSendFailed(mqGuardsProperties.getRetryTimesWhenFailed() == null ? 5 : mqGuardsProperties.getRetryTimesWhenFailed());
        producer.setRetryTimesWhenSendAsyncFailed(mqGuardsProperties.getRetryTimesWhenFailed() == null ? 5 : mqGuardsProperties.getRetryTimesWhenFailed());
        return producer;
    }

}
