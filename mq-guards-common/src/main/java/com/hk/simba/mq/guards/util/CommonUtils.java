package com.hk.simba.mq.guards.util;

import com.alibaba.fastjson.JSON;
import com.hk.simba.mq.guards.entity.BaseResponse;
import com.hk.simba.mq.guards.entity.InitMqSendLogsParams;
import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import com.hk.simba.mq.guards.entity.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.nonNull;

/**
 * @author Allen
 * @since 2021-07-28
 */
@Slf4j
public class CommonUtils {

    private CommonUtils(){}

    /**
     * 将MQ消息保存到服务端
     *
     * @param mqMessage 消息实体
     * @return boolean 保存是否成功
     */
    public static boolean saveMessageToServer(MqMessage mqMessage, MqGuardsProperties mqGuardsProperties) {
        InitMqSendLogsParams params = new InitMqSendLogsParams();
        params.setApplication(mqMessage.getApplication());
        params.setTopic(mqMessage.getTopic());
        params.setTag(mqMessage.getTag());
        params.setBody(mqMessage.getBody());
        params.setMqKey(mqMessage.getKey());
        params.setProducerId(mqGuardsProperties.getGroupId());
        params.setMqMaxRetryTimes(mqGuardsProperties.getRetryTimesWhenFailed());
        params.setTargetTime(mqMessage.getTargetTime());
        params.setCreateBy(mqMessage.getOperator());
        params.setSendWay(mqMessage.getSendWay());
        try {
            log.debug("【消息卫士】- 将发送失败的消息保存到服务端，开始！mqMessage={}", mqMessage);
            String result = HttpUtils.httpMethodPost(mqGuardsProperties.getServerUrl(), toJSONString(params), null);
            log.debug("【消息卫士】- 将发送失败的消息保存到服务端，结束！mqMessage={}，result={}", mqMessage, result);
            if (StringUtils.isNotBlank(result)) {
                BaseResponse baseResponse = JSON.parseObject(result, BaseResponse.class);
                if (baseResponse.isSuccess()) {
                    return true;
                }
            }
            log.warn("【消息卫士】- 将发送失败的消息保存到服务端，失败！mqMessage={}，result={}", mqMessage, result);
            return false;
        } catch (IOException e) {
            log.warn("【消息卫士】- 将发送失败的消息保存到服务端，异常！mqMessage=", e);
            return false;
        }
    }

    /**
     * 保存失败消息到失败队列中(非阻塞方式)
     *
     * @param mqMessage 消息实体
     */
    public static void saveMessageToFailureQueue(MqMessage mqMessage, LinkedBlockingQueue<MqMessage> failureQueue) {
        boolean offer = failureQueue.offer(mqMessage);
        if (!offer) {
            log.error("【消息卫士】- 插入失败队列失败，抛弃该消息！mqMessage={}", mqMessage);
        }
    }

    /**
     * 清理失败队列中的元素，将每个元素都发到服务端，发放失败则直接抛弃
     * (服务下线时使用)
     */
    public static void releaseFailureQueue(LinkedBlockingQueue<MqMessage> failureQueue, MqGuardsProperties mqGuardsProperties) {
        log.info("【消息卫士】- 服务下线，清除失败队列中剩余存储消息");
        do {
            // 直接弹出头部元素
            MqMessage head = failureQueue.poll();
            if (nonNull(head)) {
                boolean result = saveMessageToServer(head, mqGuardsProperties);
                if (!result) {
                    log.error("【消息卫士】- 清除内存队列消息失败，丢失消息内容为={}", head);
                }
            }
        } while (!failureQueue.isEmpty());
    }

}
