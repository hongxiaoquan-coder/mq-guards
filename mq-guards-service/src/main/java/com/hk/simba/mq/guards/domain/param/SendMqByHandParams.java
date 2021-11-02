package com.hk.simba.mq.guards.domain.param;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * mq手动补发参数
 *
 * @author Allen
 * @since 2021-11-01
 */
@Data
public class SendMqByHandParams implements Serializable {
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
     * 生产者id
     */
    private String producerId;

    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;

}