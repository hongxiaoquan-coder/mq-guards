package com.hk.simba.mq.guards.infrastructure.database.mapper;

import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MqSendLogsMapper {
    /**
     * 初始化
     *
     * @param mqSendLogs 实体信息
     * @return int 影响条数
     */
    int insertSelective(MqSendLogs mqSendLogs);

    /**
     * 查询时间范围内 未进行补发且执行次数小于最大执行次数的消息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param status    状态值
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> selectAllByCreateTimeAndStatus(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("status") Integer status);

    /**
     * 根据id修改重试次数和消息状态
     *
     * @param retriedTimes 重试次数
     * @param status       发送状态
     * @return int
     */
    int updateRetriedTimesAndStatus(@Param("id") Long id, @Param("retriedTimes") Integer retriedTimes, @Param("status") Integer status);

}