package com.hk.simba.mq.guards.task;

import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import com.hk.simba.mq.guards.entity.MqMessage;
import com.hk.simba.mq.guards.util.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 失败队列处理线程
 *
 * @author Allen
 * @since 2021-07-07
 */
@Slf4j
@AllArgsConstructor
public class FailureQueueHandleThread extends Thread {

    private final LinkedBlockingQueue<MqMessage> failMessageQueue;
    private final MqGuardsProperties mqGuardsProperties;

    @Override
    public void run() {
        while (true) {
            try {
                // 取出队列中的消息
                MqMessage mqMessage = failMessageQueue.take();
                int retryTimes = mqMessage.getRetryTimes() == null ? 0 : mqMessage.getRetryTimes();
                log.info("【消息卫士】- 检测到失败队列中有失败消息存在，进行重试操作");
                int failQueueRetryTimes = mqGuardsProperties.getFailQueueRetryTimes() == null ? 5 :
                    mqGuardsProperties.getFailQueueRetryTimes();
                if (retryTimes >= failQueueRetryTimes) {
                    log.error("【消息卫士】- 消息={}已超过最大重试次数，不再进行重复处理", mqMessage);
                } else {
                    // 进行存储任务
                    boolean result = CommonUtils.saveMessageToServer(mqMessage, mqGuardsProperties);
                    if (!result) {
                        log.error("【消息卫士】- 消息={}重试失败，存储服务存在问题无法有效清除失败队列中的请求", mqMessage);
                        ++retryTimes;
                        mqMessage.setRetryTimes(retryTimes);
                        if (!failMessageQueue.offer(mqMessage)) {
                            log.error("【消息卫士】- 失败队列已满，请求={}无法重新放入失败队列中", mqMessage);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("【消息卫士】- 处理队列消息失败，异常原因=", e);
            }
        }
    }

}
