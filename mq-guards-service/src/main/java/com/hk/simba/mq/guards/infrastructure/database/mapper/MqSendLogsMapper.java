package com.hk.simba.mq.guards.infrastructure.database.mapper;

import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
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
     * @param currentTime   当前时间
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> selectAllByCreateTimeAndStatus(@Param("status") Integer status,
        @Param("maxRetryTimes") Integer maxRetryTimes, @Param("currentTime") Date currentTime);

    /**
     * 根据id修改重试次数和消息状态
     *
     * @param id            主键id
     * @param retriedTimes  重试次数
     * @param status        发送状态
     * @param executionTime 执行时间
     * @return int
     */
    int updateRetriedTimesAndStatusAndExecutionTimeById(@Param("id") Long id, @Param("retriedTimes") Integer retriedTimes,
        @Param("status") Integer status, @Param("executionTime") Date executionTime);

    /**
     * 根据搜索条件查询失败消息记录
     *
     * @param application   应用方
     * @param idCollection  id列表
     * @param topic         topic
     * @param minCreateTime 开始时间
     * @param maxCreateTime 结束时间
     * @param producerId    生产者ID
     * @return java.util.List<com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs>
     */
    List<MqSendLogs> selectAllByApplicationAndIdInAndTopicAndCreateTimeBetweenAndProducerId(
        @Param("application") String application, @Param("idCollection") Collection<Long> idCollection,
        @Param("topic") String topic, @Param("minCreateTime") Date minCreateTime,
        @Param("maxCreateTime") Date maxCreateTime, @Param("producerId") String producerId);

    int updateStatusById(@Param("updatedStatus")Integer updatedStatus,@Param("id")Long id);

}