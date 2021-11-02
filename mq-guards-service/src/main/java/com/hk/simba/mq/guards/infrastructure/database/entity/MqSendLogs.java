package com.hk.simba.mq.guards.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * mq发送记录表
 *
 * @author Allen
 * @since 2021-07-01
 */
@Data
@TableName("mq_send_logs")
public class MqSendLogs {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
    * 定时任务已重试次数
    */
    private Integer retriedTimes;

    /**
    * mq最大重试次数
    */
    private Integer mqMaxRetryTimes;

    /**
    * 消息发送目标到达时间
    */
    private Date targetTime;

    /**
    * 发送状态 0:初始状态 1:成功 2:失败
    */
    private Integer status;

    /**
     * 发送方式 0：同步（sync） 1：异步（async） 2：oneway（不考虑发送结果）
     */
    private Integer sendWay;

    /**
     * 执行时间点
     */
    private Date executionTime;

    /**
    * 创建时间
    */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
    * 创建者
    */
    private String createBy;

    /**
    * 修改时间
    */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
    * 修改者
    */
    private String modifyBy;

}