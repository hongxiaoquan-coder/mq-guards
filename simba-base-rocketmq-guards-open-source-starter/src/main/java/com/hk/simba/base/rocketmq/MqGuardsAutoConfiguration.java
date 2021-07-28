package com.hk.simba.base.rocketmq;

import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息卫士自动加载配置
 *
 * @author Allen
 * @since 2021-06-29
 */
@Configuration
@ConditionalOnProperty(prefix = "mq.guards", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MqGuardsProperties.class)
public class MqGuardsAutoConfiguration {
    private final MqGuardsProperties mqGuardsProperties;

    public MqGuardsAutoConfiguration(MqGuardsProperties mqGuardsProperties) {
        this.mqGuardsProperties = mqGuardsProperties;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public MqGuardsClient mqGuardsClient() {
        return new MqGuardsClient(mqGuardsProperties);
    }

}
