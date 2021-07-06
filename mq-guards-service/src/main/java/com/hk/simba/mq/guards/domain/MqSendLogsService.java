package com.hk.simba.mq.guards.domain;

import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;

import java.util.Date;
import java.util.List;

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
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param status    状态值
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> queryUnReissuedMessages(Date startTime, Date endTime, Integer status);

    /**
     * 根据id修改重试次数和消息状态
     *
     * @param retriedTimes 重试次数
     * @param status       发送状态
     * @return int
     */
    int updateRetriedTimesAndStatus(Long id, Integer retriedTimes, Integer status);

}
