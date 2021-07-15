package com.hk.simba.mq.guards.access.job;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendCallback;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import com.hk.simba.mq.guards.infrastructure.database.enums.MqStatusEnums;
import com.hk.simba.mq.guards.infrastructure.mq.MqGuardsProperties;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MqGuardsJob {

    @Autowired
    private MqSendLogsService mqSendLogsService;
    @Autowired
    private DefaultMQProducer defaultMQProducer;
    @Autowired
    private MqGuardsProperties mqGuardsProperties;

    @Value(value = "${mq.guards.maxRetryTimes}")
    private Integer maxRetryTimes;

    @XxlJob("reissueMessageJob")
    public ReturnT<String> reissueMessage(String param) {
        log.info("--------消息补发开始--------, param:" + param);
        // 确定执行时间范围
        Integer offset = mqGuardsProperties.getOffset();
        Date endDate = new Date();
        Date startDate = DateUtils.addMinutes(endDate, -offset);
        // 查询执行时间范围内 状态为初始化的消息进行补发操作
        List<MqSendLogs> mqSendLogs = mqSendLogsService.queryUnReissuedMessages(startDate, endDate, MqStatusEnums.INIT.getCode(), maxRetryTimes);
        log.info("--------共有{}条消息需要补发--------", mqSendLogs.size());
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
                        message.putUserProperty(com.aliyun.openservices.ons.api.Message.SystemPropKey.STARTDELIVERTIME, String.valueOf(startDeliverTime));
                    }
                }
                switch (sendWay){
                    case 0:
                        defaultMQProducer.send(message);
                        break;
                    case 1:
                        defaultMQProducer.send(message, (SendCallback) null);
                        break;
                    case 2:
                        defaultMQProducer.sendOneway(message);
                        break;
                    default:
                        log.error("unsupported send way");
                        break;
                }
                log.info("{},消息补偿服务补发mq消息={}", DateUtil.formatDateTime(new Date()), message);
                // 修改定时任务执行次数、任务状态
                mqSendLogsService.updateRetriedTimesAndStatus(mq.getId(), mq.getRetriedTimes() + 1, MqStatusEnums.SUCCESS.getCode());
                log.info("{},消息={},补发成功", DateUtil.formatDateTime(new Date()), toJSONString(mq));
            } catch (Exception e) {
                log.error("{},消息={},发送错误，异常原因={}",DateUtil.formatDateTime(new Date()), message, e);
                // 修改定时任务执行次数、任务状态
                mqSendLogsService.updateRetriedTimesAndStatus(mq.getId(), mq.getRetriedTimes() + 1, MqStatusEnums.FAIL.getCode());
            }
        });
        return ReturnT.SUCCESS;
    }

}
