package com.hk.simba.mq.guards.infrastructure.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen
 * @since 2021-06-28
 */
@Data
@ConfigurationProperties(prefix = "mq.guards")
public class MqGuardsProperties {
    /**
     * 组件id
     */
    private String groupId;
    /**
     * 秘钥
     */
    private String accessKey;
    /**
     * 秘钥
     */
    private String secretKey;
    /**
     * 服务注册中心地址
     */
    private String nameServerAddress;
    /**
     * 最大重试次数
     */
    private Integer retryTimesWhenFailed;
    /**
     * 延迟时间
     */
    private String sendMsgTimeoutMillis;
    /**
     * 定时任务执行间隔 单位分钟
     */
    private Integer offset;
}
