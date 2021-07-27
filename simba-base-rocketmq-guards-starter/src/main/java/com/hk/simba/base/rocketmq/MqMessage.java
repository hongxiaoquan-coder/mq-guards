package com.hk.simba.base.rocketmq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Allen
 * @since 2021-06-29
 */
@Data
@ToString
@NoArgsConstructor
public class MqMessage implements Serializable {
    private static final long serialVersionUID = -1L;
    /**
     * 业务主键
     */
    private String key;
    /**
     * 发送主题
     */
    @NotBlank(message = "topic is blank")
    private String topic;
    /**
     * 发送标签
     */
    private String tag;
    /**
     * 发送消息体
     */
    @NotBlank(message = "body is blank")
    private String body;
    /**
     * mq消息到达目标时间(无需设置)
     */
    private Date targetTime;
    /**
     * 发送人
     */
    @NotBlank(message = "operator is blank")
    private String operator;
    /**
     * 应用名称
     */
    @NotBlank(message = "application is blank")
    private String application;
    /**
     * 发送方式 0：同步（sync） 1：异步（async） 2：oneway（不考虑发送结果）
     */
    private Integer sendWay;
    /**
     * 延迟时间（mills为单位）
     */
    private Long delayTime;
    /**
     * 已重试次数
     */
    private Integer retryTimes;
    /**
     * 日志追踪id（唯一标识）
     */
    private String traceId;

}
