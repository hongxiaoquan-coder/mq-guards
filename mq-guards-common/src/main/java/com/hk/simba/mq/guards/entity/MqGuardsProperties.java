package com.hk.simba.mq.guards.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author Allen
 * @since 2021-06-28
 */
@Data
@ConfigurationProperties(prefix = "mq.guards")
@Validated
public class MqGuardsProperties implements Serializable {

    /**
     * 生产者id
     */
    private String groupId;
    /**
     * 访问密钥
     */
    @NotBlank(message = "accessKey is blank")
    private String accessKey;
    /**
     * 应用密钥
     */
    @NotBlank(message = "secretKey is blank")
    private String secretKey;
    /**
     * 服务注册中心地址
     */
    @NotBlank(message = "nameServerAddress is blank")
    private String nameServerAddress;
    /**
     * 发送失败时mq最大重试次数
     */
    private Integer retryTimesWhenFailed;
    /**
     * 判定消息发送失败的延迟时间
     */
    private Long sendMsgTimeoutMillis;

    /**
     * 失败队列最大数量
     */
    private Integer failQueueSize;

    /**
     * 存储失败消息核心线程池数量
     */
    private Integer corePoolSize;
    /**
     * 存储失败消息最大线程池数量
     */
    private Integer maximumPoolSize;
    /**
     * 存储失败消息线程存活时间
     */
    private Long keepAliveTime;
    /**
     * 存储失败消息线程存活时间单位
     */
    private TimeUnit timeUnit;
    /**
     * 存储失败消息线程池工作队列
     */
    private Integer workQueueNum;
    /**
     * 消息卫士服务端地址
     * 用于存储消息记录的接口url
     */
    private String serverUrl;
    /**
     * 失败队列重试次数（尝试将失败mq消息存储到数据库）
     */
    private Integer failQueueRetryTimes;

}
