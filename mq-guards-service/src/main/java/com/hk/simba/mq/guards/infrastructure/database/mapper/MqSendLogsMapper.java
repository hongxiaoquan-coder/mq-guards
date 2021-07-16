package com.hk.simba.mq.guards.infrastructure.database.mapper;

import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
/**
 * @author Allen
 * @since 2021-07-01
 */
public interface MqSendLogsMapper {
    /**
     * 初始化
     *
     * @param mqSendLogs 实体信息
     * @return int 影响条数
     */
    int insertSelective(MqSendLogs mqSendLogs);

    /**
     * 查询补发未成功且执行次数小于最大执行次数的消息
     *
     * @param status        状态值
     * @param maxRetryTimes 定时任务最大重试次数
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> selectAllByCreateTimeAndStatus(@Param("status") Integer status, @Param("maxRetryTimes") Integer maxRetryTimes);

    /**
     * 根据id修改重试次数和消息状态
     *
     * @param id 主键id
     * @param retriedTimes 重试次数
     * @param status       发送状态
     * @return int
     */
    int updateRetriedTimesAndStatus(@Param("id") Long id, @Param("retriedTimes") Integer retriedTimes, @Param("status") Integer status);

}