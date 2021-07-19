package com.hk.simba.mq.guards.domain;

import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;

import java.util.List;

/**
 * @author Allen
 * @since 2021-07-01
 */
public interface MqSendLogsService {

    /**
     * 初始化
     *
     * @param params 初始化参数
     * @return void
     */
    void insert(InitMqSendLogsParams params);

    /**
     * 查询时间范围内 未进行补发且执行次数小于最大执行次数的消息
     *
     * @param status        状态值
     * @param maxRetryTimes 定时任务最大重试次数
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> queryUnReissuedMessages(Integer status, Integer maxRetryTimes);

    /**
     * 根据id修改重试次数和消息状态
     *
     * @param id 主键id
     * @param retriedTimes 重试次数
     * @param status       发送状态
     * @return int
     */
    int updateRetriedTimesAndStatus(Long id, Integer retriedTimes, Integer status);

}
