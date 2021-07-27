package com.hk.simba.base.rocketmq;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * mq发送记录表初始化参数
 *
 * @author Allen
 * @since 2021-07-02
 */
@Data
public class InitMqSendLogsParams implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 应用方项目名称
     */
    private String application;

    /**
     * mq topic
     */
    private String topic;

    /**
     * mq标签
     */
    private String tag;

    /**
     * 发送消息json串
     */
    private String body;

    /**
     * mq key
     */
    private String mqKey;

    /**
     * 生产者id
     */
    private String producerId;

    /**
     * mq最大重试次数
     */
    private Integer mqMaxRetryTimes;

    /**
     * 消息发送目标到达时间
     */
    private Date targetTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 发送方式 0：同步（sync） 1：异步（async） 2：oneway（不考虑发送结果）
     */
    private Integer sendWay;

}