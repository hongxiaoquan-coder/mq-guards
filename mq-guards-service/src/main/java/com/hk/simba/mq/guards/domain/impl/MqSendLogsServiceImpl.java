package com.hk.simba.mq.guards.domain.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendCallback;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.hk.simba.base.beanmapper.BeanMapper;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.domain.param.SendMqByHandParams;
import com.hk.simba.mq.guards.entity.SendWayEnum;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import com.hk.simba.mq.guards.infrastructure.database.enums.MqStatusEnums;
import com.hk.simba.mq.guards.infrastructure.database.mapper.MqSendLogsMapper;
import com.xxl.job.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Service
@Slf4j
public class MqSendLogsServiceImpl implements MqSendLogsService {

    @Autowired
    private MqSendLogsMapper mqSendLogsMapper;
    @Autowired
    private BeanMapper beanMapper;
    @Autowired
    private DefaultMQProducer defaultMQProducer;

    @Override
    public void insert(InitMqSendLogsParams params) {
        MqSendLogs mqSendLogs = beanMapper.map(params, MqSendLogs.class);
        mqSendLogs.setCreateTime(new Date());
        mqSendLogs.setExecutionTime(new Date());
        mqSendLogsMapper.insertSelective(mqSendLogs);
    }

    @Override
    public List<MqSendLogs> queryUnReissuedMessages(Integer status, Integer maxRetryTimes) {
        return mqSendLogsMapper.selectAllByCreateTimeAndStatus(status, maxRetryTimes, new Date());
    }

    @Override
    public int updateRetriedTimesAndStatusAndExecutionTimeById(Long id, Integer retriedTimes, Integer status,
        Date executionTime) {
        return mqSendLogsMapper
            .updateRetriedTimesAndStatusAndExecutionTimeById(id, retriedTimes, status, executionTime);
    }

    @Override
    public void sendByHand(SendMqByHandParams params) {
        List<MqSendLogs> mqSendLogs = mqSendLogsMapper
            .selectAllByApplicationAndIdInAndTopicAndCreateTimeBetweenAndProducerId(params.getApplication(),
                params.getIdList(), params.getTopic(), params.getBeginTime(), params.getEndTime(),
                params.getProducerId());
        if (CollectionUtils.isNotEmpty(mqSendLogs)) {
            mqSendLogs.forEach(mq -> {
                log.info("【消息卫士】 - 手动补发开始，补发记录为：{}", mq);
                Integer mqMaxRetryTimes = mq.getMqMaxRetryTimes();
                defaultMQProducer.setRetryTimesWhenSendAsyncFailed(mqMaxRetryTimes);
                String producerGroup = mq.getProducerId();
                defaultMQProducer.setProducerGroup(
                    StringUtils.isEmpty(producerGroup) ? "__ONS_PRODUCER_DEFAULT_GROUP" : producerGroup);
                Message message = new Message(mq.getTopic(), mq.getTag(), mq.getMqKey(), mq.getBody().getBytes());
                Date targetTime = mq.getTargetTime();
                if (targetTime != null) {
                    long endTime = targetTime.getTime();
                    long startTime = System.currentTimeMillis();
                    long startDeliverTime = endTime - startTime > 0 ? endTime - startTime : 0L;
                    if (startDeliverTime > 0) {
                        message.putUserProperty(com.aliyun.openservices.ons.api.Message.SystemPropKey.STARTDELIVERTIME,
                            String.valueOf(startDeliverTime));
                    }
                }
                SendWayEnum sendWayEnum = SendWayEnum.get(mq.getSendWay());
                try {
                    switch (sendWayEnum) {
                        case SYNC:
                            defaultMQProducer.send(message);
                            break;
                        case ASYNC:
                            defaultMQProducer.send(message, (SendCallback)null);
                            break;
                        case ONEWAY:
                            defaultMQProducer.sendOneway(message);
                            break;
                        default:
                            log.error("【消息卫士】- unsupported send way={}", mq.getSendWay());
                            break;
                    }
                    // 修改定时任务任务状态
                    mqSendLogsMapper.updateStatusById(MqStatusEnums.SUCCESS.getCode(), mq.getId());
                } catch (Exception e) {
                    // 修改定时任务任务状态
                    mqSendLogsMapper.updateStatusById(MqStatusEnums.FAIL.getCode(), mq.getId());
                    log.error("【消息卫士】- {},消息={}补发失败，异常原因=", DateUtil.formatDateTime(new Date()), message, e);
                }
            });
        }
    }
}
