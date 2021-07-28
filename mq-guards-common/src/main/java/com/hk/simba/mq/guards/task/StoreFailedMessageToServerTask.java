package com.hk.simba.mq.guards.task;

import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import com.hk.simba.mq.guards.entity.MqMessage;
import com.hk.simba.mq.guards.entity.SendWayEnum;
import com.hk.simba.mq.guards.util.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 存储发送失败的消息
 *
 * @author Allen
 * @since 2021-07-12
 */
@Data
@AllArgsConstructor
@Slf4j
public class StoreFailedMessageToServerTask implements Runnable {

    private final MqMessage mqMessage;
    private MqGuardsProperties mqGuardsProperties;
    private LinkedBlockingQueue<MqMessage> failureQueue;

    @Override
    public void run() {
        log.info("【消息卫士】- storeFailureMessageThread处理失败消息操作开始");
        // 仅同步方式支持补发
        if (SendWayEnum.SYNC.getCode().equals(mqMessage.getSendWay())) {
            // 处理队列中的请求
            boolean result = CommonUtils.saveMessageToServer(mqMessage, mqGuardsProperties);
            // 添加失败 则把请求存入内存队列中
            if (!result) {
                CommonUtils.saveMessageToFailureQueue(mqMessage, failureQueue);
            }
        }
    }

}
