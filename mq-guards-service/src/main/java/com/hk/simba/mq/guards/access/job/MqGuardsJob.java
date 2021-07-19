package com.hk.simba.mq.guards.access.job;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendCallback;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.google.common.base.Stopwatch;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import com.hk.simba.mq.guards.infrastructure.database.enums.MqStatusEnums;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.aliyun.openservices.shade.com.alibaba.fastjson.JSON.toJSONString;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Component
@Slf4j
@AllArgsConstructor
public class MqGuardsJob {
    private final MqSendLogsService mqSendLogsService;
    private final DefaultMQProducer defaultMQProducer;

    @Value(value = "${mq.guards.maxRetryTimes}")
    private final Integer maxRetryTimes;

    @XxlJob("reissueMessageJob")
    public ReturnT<String> reissueMessage(String param) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("【mq-guards】--------消息补发开始--------");
        stopwatch.start();
        // 查询状态为初始化 重试次数在最大重试次数下的消息进行补发操作
        List<MqSendLogs> mqSendLogs =
            mqSendLogsService.queryUnReissuedMessages(MqStatusEnums.SUCCESS.getCode(), maxRetryTimes);
        log.info("【mq-guards】--------共有{}条消息需要补发--------", mqSendLogs.size());
        if (CollectionUtils.isNotEmpty(mqSendLogs)) {
            mqSendLogs.forEach(mq -> {
                Integer mqMaxRetryTimes = mq.getMqMaxRetryTimes();
                defaultMQProducer.setRetryTimesWhenSendAsyncFailed(mqMaxRetryTimes);
                String producerGroup = mq.getProducerId();
                if (StringUtils.isEmpty(producerGroup)) {
                    producerGroup = "__ONS_PRODUCER_DEFAULT_GROUP";
                }
                defaultMQProducer.setProducerGroup(producerGroup);
                Message message = new Message(mq.getTopic(), mq.getTag(), mq.getMqKey(), mq.getBody().getBytes());
                try {
                    Integer sendWay = mq.getSendWay();
                    Date targetTime = mq.getTargetTime();
                    if (targetTime != null) {
                        long endTime = targetTime.getTime();
                        long startTime = System.currentTimeMillis();
                        long startDeliverTime = endTime - startTime > 0 ? endTime - startTime : 0L;
                        if (startDeliverTime > 0) {
                            message
                                .putUserProperty(com.aliyun.openservices.ons.api.Message.SystemPropKey.STARTDELIVERTIME,
                                    String.valueOf(startDeliverTime));
                        }
                    }
                    switch (sendWay) {
                        case 0:
                            defaultMQProducer.send(message);
                            break;
                        case 1:
                            defaultMQProducer.send(message, (SendCallback)null);
                            break;
                        case 2:
                            defaultMQProducer.sendOneway(message);
                            break;
                        default:
                            log.error("【mq-guards】- 暂不支持该发送方式");
                            break;
                    }
                    log.info("【mq-guards】- {},消息补偿服务补发mq消息={}", DateUtil.formatDateTime(new Date()), message);
                    // 修改定时任务执行次数、任务状态
                    mqSendLogsService.updateRetriedTimesAndStatus(mq.getId(), mq.getRetriedTimes() + 1,
                        MqStatusEnums.SUCCESS.getCode());
                    log.info("【mq-guards】- {}耗时{},消息={},补发成功", DateUtil.formatDateTime(new Date()), stopwatch.stop(),
                        toJSONString(mq));
                } catch (Exception e) {
                    log.error("【mq-guards】- {}耗时{},消息={}补发失败，异常原因={}", DateUtil.formatDateTime(new Date()),
                        stopwatch.stop(), message, e);
                    // 修改定时任务执行次数、任务状态
                    mqSendLogsService.updateRetriedTimesAndStatus(mq.getId(), mq.getRetriedTimes() + 1,
                        MqStatusEnums.FAIL.getCode());
                }
            });
        }
        return ReturnT.SUCCESS;
    }

}
