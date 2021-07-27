package com.hk.simba.base.rocketmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 存储发送失败的消息
 *
 * @author Allen
 * @since 2021-07-12
 */
@Data
@AllArgsConstructor
@Slf4j
class StoreFailedMessageToServerTask implements Runnable {

    private final MqMessage mqMessage;

    private MqGuardsClient mqGuardsClient;

    @Override
    public void run() {
        log.info("【消息卫士】- storeFailureMessageThread处理失败消息操作开始");
        // 异步方式不支持补发 故不进行存储记录
        if (!SendWayEnum.ASYNC.getCode().equals(mqMessage.getSendWay())) {
            // 处理队列中的请求
            boolean result = mqGuardsClient.saveMessageToServer(mqMessage);
            // 添加失败 则把请求存入内存队列中
            if (!result) {
                mqGuardsClient.saveMessageToFailureQueue(mqMessage);
            }
        }
    }
}
