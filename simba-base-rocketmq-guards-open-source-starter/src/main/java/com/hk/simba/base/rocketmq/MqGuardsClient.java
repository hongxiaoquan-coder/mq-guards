package com.hk.simba.base.rocketmq;

import com.alibaba.fastjson.JSON;
import com.hk.simba.mq.guards.entity.BaseResponse;
import com.hk.simba.mq.guards.task.FailureQueueHandleThread;
import com.hk.simba.mq.guards.util.HttpUtils;
import com.hk.simba.mq.guards.entity.InitMqSendLogsParams;
import com.hk.simba.mq.guards.entity.MqGuardsProperties;
import com.hk.simba.mq.guards.entity.MqMessage;
import com.hk.simba.mq.guards.entity.SendWayEnum;
import com.hk.simba.mq.guards.task.StoreFailedMessageToServerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

/**
 * 消息卫士客户端
 * 用于发送MQ消息，以及将发送失败的MQ消息存储到服务端
 *
 * @author Allen
 * @since 2021-06-28
 */
@Slf4j
@Data
public class MqGuardsClient {
    /**
     * 消息生产者
     */
    private DefaultMQProducer producer;
    /**
     * 失败队列
     */
    private LinkedBlockingQueue<MqMessage> failureQueue;
    /**
     * 消息存储线程池
     */
    private ThreadPoolExecutor poolExecutor;
    /**
     * 消息卫士属性
     */
    private MqGuardsProperties mqGuardsProperties;

    public MqGuardsClient(MqGuardsProperties mqGuardsProperties) {
        this.mqGuardsProperties = mqGuardsProperties;
    }

    /**
     * 初始化方法
     */
    public void start() {
        producer = RocketMQUtil
            .createDefaultMQProducer(mqGuardsProperties.getGroupId(), mqGuardsProperties.getAccessKey(),
                mqGuardsProperties.getSecretKey(), false, null);
        // 同步发送重试次数，默认5次
        producer.setRetryTimesWhenSendFailed(
            mqGuardsProperties.getRetryTimesWhenFailed() == null ? 5 : mqGuardsProperties.getRetryTimesWhenFailed());
        // 异步发送重试次数，默认5次
        producer.setRetryTimesWhenSendAsyncFailed(
            mqGuardsProperties.getRetryTimesWhenFailed() == null ? 5 : mqGuardsProperties.getRetryTimesWhenFailed());
        producer.setSendMsgTimeout(mqGuardsProperties.getSendMsgTimeoutMillis() == null ? 3000 :
            mqGuardsProperties.getSendMsgTimeoutMillis().intValue());
        producer.setNamesrvAddr(mqGuardsProperties.getNameServerAddress());
        try {
            log.debug("【消息卫士】- 初始化MQ生产者，开始。");
            producer.start();
            log.debug("【消息卫士】- 初始化MQ生产者，成功。");
        } catch (MQClientException e) {
            log.error("【消息卫士】- 初始化MQ生产者，异常。异常原因=" + e.getMessage(), e);
        }

        // 初始化失败队列，默认值1000
        int failQueueSize =
            mqGuardsProperties.getFailQueueSize() == null ? 1000 : mqGuardsProperties.getFailQueueSize();
        log.debug("【消息卫士】- 初始化失败队列，开始。failQueueSize={}", failQueueSize);
        failureQueue = new LinkedBlockingQueue<>(failQueueSize);
        log.debug("【消息卫士】- 初始化失败队列，成功。");

        // 初始化消息池
        // 核心线程数，默认值为2
        int corePoolSize = mqGuardsProperties.getCorePoolSize() == null ? 2 : mqGuardsProperties.getCorePoolSize();
        // 最大线程数，默认值为10
        int maximumPoolSize =
            mqGuardsProperties.getMaximumPoolSize() == null ? 10 : mqGuardsProperties.getMaximumPoolSize();
        // 非核心线程的最大空闲时间，默认值为5分钟
        long keepAliveTime =
            mqGuardsProperties.getKeepAliveTime() == null ? 5 * 60 * 1000 : mqGuardsProperties.getKeepAliveTime();
        // 时间单位
        TimeUnit timeUnit =
            mqGuardsProperties.getTimeUnit() == null ? TimeUnit.MILLISECONDS : mqGuardsProperties.getTimeUnit();
        // 任务队列容量，默认值为100
        int workQueueNum = mqGuardsProperties.getWorkQueueNum() == null ? 100 : mqGuardsProperties.getWorkQueueNum();

        ThreadFactory storeMessageThreadFactory = new CustomizableThreadFactory("store-message-pool-");
        // 线程池满时的拒绝处理
        RejectedExecutionHandler rejectedExecutionHandler = (r, executor) -> {
            // 线程池storeMessageThreadPool已满，将消息直接插入到失败队列中，如果仍插入失败，将直接放弃该消息
            if (r instanceof StoreFailedMessageToServerTask) {
                StoreFailedMessageToServerTask task = (StoreFailedMessageToServerTask)r;
                MqMessage mqMessage = task.getMqMessage();
                log.info("【消息卫士】- 线程池已满，插入消息到失败队列中。mqMessage={}", mqMessage);
                saveMessageToFailureQueue(mqMessage);
            }
        };

        log.debug(
            "【消息卫士】- 初始化消息存储线程池，开始。corePoolSize={}，maximumPoolSize={}，keepAliveTime={}，timeUnit={}，workQueueNum={}",
            corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, workQueueNum);
        poolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
            new LinkedBlockingQueue<>(workQueueNum), storeMessageThreadFactory, rejectedExecutionHandler);
        log.debug("【消息卫士】- 初始化消息存储线程池，成功");

        // 开启线程专门处理失败队列中的请求
        log.debug("【消息卫士】- 开启失败队列处理线程，开始。");
        new FailureQueueHandleThread(failureQueue, mqGuardsProperties).start();
        log.debug("【消息卫士】- 开启失败队列处理线程，成功。");

        log.info("【消息卫士】- 启动成功！");
    }

    /**
     * 销毁方法
     */
    public void shutdown() {
        log.info("【消息卫士】- mqGuardsClient下线，开始。");
        // 销毁producer
        log.debug("【消息卫士】- 销毁MQ生产者，开始。");
        producer.shutdown();
        log.debug("【消息卫士】- 销毁MQ生产者，结束。");
        // 销毁消息存储线程池
        poolExecutor.shutdown();
        log.debug("【消息卫士】- 销毁线程池，结束。");
        // 释放失败队列中的请求
        log.debug("【消息卫士】- 清理失败队列，开始。");
        releaseFailureQueue();
        log.debug("【消息卫士】- 清理失败队列，结束。");
        log.info("【消息卫士】- mqGuardsClient下线，完成。");
    }

    /**
     * 同步发送消息
     *
     * @param mqMessage 消息实体
     * @return com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult 发送结果
     */
    public SendResult sendMessage(MqMessage mqMessage) {
        mqMessage.setSendWay(SendWayEnum.SYNC.getCode());
        return sendMsg(mqMessage, null);
    }

    /**
     * 发送异步消息
     *
     * @param mqMessage    消息实体
     * @param sendCallback 回调方法
     */
    public void sendMessageAsync(MqMessage mqMessage, SendCallback sendCallback) {
        mqMessage.setSendWay(SendWayEnum.ASYNC.getCode());
        sendMsg(mqMessage, sendCallback);
    }

    /**
     * 发送oneway消息
     *
     * @param mqMessage 消息实体
     */
    public void sendMessageOneWay(MqMessage mqMessage) {
        mqMessage.setSendWay(SendWayEnum.ONEWAY.getCode());
        sendMsg(mqMessage, null);
    }

    /**
     * 将MQ消息保存到服务端
     *
     * @param mqMessage 消息实体
     * @return boolean 保存是否成功
     */
    public boolean saveMessageToServer(MqMessage mqMessage) {
        InitMqSendLogsParams params = new InitMqSendLogsParams();
        params.setApplication(mqMessage.getApplication());
        params.setTopic(mqMessage.getTopic());
        params.setTag(mqMessage.getTag());
        params.setBody(mqMessage.getBody());
        params.setMqKey(mqMessage.getKey());
        params.setProducerId(mqGuardsProperties.getGroupId());
        params.setMqMaxRetryTimes(mqGuardsProperties.getRetryTimesWhenFailed());
        params.setTargetTime(null);
        params.setCreateBy(mqMessage.getOperator());
        params.setSendWay(mqMessage.getSendWay());
        try {
            log.debug("【消息卫士】- 将发送失败的消息保存到服务端，开始！mqMessage={}", mqMessage);
            String result = HttpUtils.httpMethodPost(mqGuardsProperties.getServerUrl(), JSON.toJSONString(params), null);
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
    public void saveMessageToFailureQueue(MqMessage mqMessage) {
        boolean offer = failureQueue.offer(mqMessage);
        if (!offer) {
            log.error("【消息卫士】- 插入失败队列失败，抛弃该消息！mqMessage={}", mqMessage);
        }
    }

    /**
     * 发送消息
     *
     * @param mqMessage    传入mq消息
     * @param sendCallback 回调方法
     * @return com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult 发送结果
     */
    private SendResult sendMsg(MqMessage mqMessage, SendCallback sendCallback) {
        Message message =
            new Message(mqMessage.getTopic(), mqMessage.getTag(), mqMessage.getKey(), mqMessage.getBody().getBytes());
        // 设置延迟时间
        message.setDelayTimeLevel(mqMessage.getDelayTimeLevel());
        log.debug("【消息卫士】- 发送MQ消息，开始。mqMessage={}，message={}", mqMessage, message);
        // 发送内容
        SendResult sendResult = new SendResult();
        SendWayEnum sendWayEnum = SendWayEnum.get(mqMessage.getSendWay());
        try {
            switch (sendWayEnum) {
                case SYNC:
                    sendResult = producer.send(message);
                    break;
                case ASYNC:
                    producer.send(message, sendCallback);
                    break;
                case ONEWAY:
                    producer.sendOneway(message);
                    break;
                default:
                    log.warn("【消息卫士】- 其他发送方式尚无法支持，sendWay={}", mqMessage.getSendWay());
                    break;
            }
            log.info("【消息卫士】- 发送MQ消息，成功。mqMessage={}, message={}, msgId={}", mqMessage, message, sendResult.getMsgId());
        } catch (Exception e) {
            log.error("【消息卫士】- 发送MQ消息={}失败，失败原因=", mqMessage, e);
            // 发送失败，则将消息存到服务端 防止发送延迟 异常进程异步处理
            StoreFailedMessageToServerTask storeFailedMessageToServerTask =
                new StoreFailedMessageToServerTask(mqMessage, mqGuardsProperties, failureQueue);
            poolExecutor.execute(storeFailedMessageToServerTask);
        }
        return sendResult;
    }

    /**
     * 清理失败队列中的元素，将每个元素都发到服务端，发放失败则直接抛弃
     * (服务下线时使用)
     */
    private void releaseFailureQueue() {
        log.info("【消息卫士】- 服务下线，清除失败队列中剩余存储消息");
        do {
            // 直接弹出头部元素
            MqMessage head = failureQueue.poll();
            if (nonNull(head)) {
                boolean result = saveMessageToServer(head);
                if (!result) {
                    log.error("【消息卫士】- 清除内存队列消息失败，丢失消息内容为={}", head);
                }
            }
        } while (!failureQueue.isEmpty());
    }

}
