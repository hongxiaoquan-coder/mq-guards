## 概述

mq-guards主要用于保障mq的发送成功，当mq发送失败时会进行失败记录的存储并补发
## 使用

### 引入依赖

`pom.xml`引入依赖：

```xml
<!-- 消息补发中心 -->
<dependency>
    <groupId>com.hk.simba.base</groupId>
    <artifactId>simba-base-rocketmq-guards-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 增加配置

在properties文件中添加配置如下：

```properties
# 必须配置部分
# 是否启用mq-guards
mq.guards.use.enabled=true
# mq配置信息-秘钥
mq.guards.access-key=E69LABwjdhu1IwyZ
# mq配置信息-秘钥
mq.guards.secret-key=sngHLmd0YWFHQnUE3rhUkU5OFQE50u
# mq配置信息-生产者id
mq.guards.group-id=GID_RD_QUARK_GDS
# mq配置信息-名称服务器地址
mq.guards.name-server-address=http://onsaddr.mq-internet-access.mq-internet.aliyuncs.com:80
# 存储任务的后台接口地址
mq.guards.store-message-url=http://127.0.0.1:8080/mq/insert

# 非必须配置部分
# 内存队列容量（发送失败的队列会先存储在服务的内存队列中 不设置默认为1000）
mq.guards.fail-queue-size=1000
# mq配置信息-发送失败时mq重试次数（rocketmq内部设置，当生产者发送消息失败时会进行n+1次重试操作 不设置默认为5+1）
mq.guards.retry-times-when-failed=5
# mq配置信息-mq发送超时时间(不设置默认为3000ms)
mq.guards.send-msg-timeout-millis=3000
# 由于后台通过线程池异步发送至存储任务进行存储操作，所以需要进行线程池的配置
# 核心线程池数
mq.guards.core-pool-size=4
# 最大线程池数
mq.guards.maximum-pool-size=8
# 线程存活时间
mq.guards.keep-alive-time=10
# 时间单位
mq.guards.time-unit=seconds
# 队列数目
mq.guards.work-queue-num=100
# 失败队列重试次数（尝试将失败mq消息存储到数据库）默认为5次
mq.guards.fail-queue-retry-times=5
```
### 通过@Resource引入MqProducerClient
``` java
    @Resource
    private MqProducerClient mqProducerClient;
``` 
### 组装发送信息
``` java
    MqMessage mqMessage = new MqMessage();
    mqMessage.setTopic(gdsCommonTopic);
    mqMessage.setTag(SKU_ON_TAG);
    mqMessage.setBody(JSON.toJSONString(handler));
    mqMessage.setKey("SkuOn" + key);
    mqMessage.setApplication("gds");
    mqMessage.setOperator("test");
    mqMessage.setApplicaiton("xxx");
    mqMessage.setDelay(3000L);
    mqMessage.setTargetTime(new Date());
``` 
``` java
   public class MqGuardsProperties {
    /**
     * 生产者id
     */
    @NotBlank(message = "groupId is blank")
    private String groupId;
    /**
     * 访问密钥
     */
    @NotBlank(message = "accessKey is blank")
    private String accessKey;
    /**
     * 应用密钥
     */
    @NotBlank(message = "secretKey is blank")
    private String secretKey;
    /**
     * 服务注册中心地址
     */
    @NotBlank(message = "nameServerAddress is blank")
    private String nameServerAddress;
    /**
     * 发送失败时mq最大重试次数
     */
    private Integer retryTimesWhenFailed;
    /**
     * 判定消息发送失败的延迟时间
     */
    private Long sendMsgTimeoutMillis;

    /**
     * 失败队列最大数量
     */
    private Integer failQueueSize;

    /**
     * 存储失败消息核心线程池数量
     */
    private Integer corePoolSize;
    /**
     * 存储失败消息最大线程池数量
     */
    private Integer maximumPoolSize;
    /**
     * 存储失败消息线程存活时间
     */
    private Long keepAliveTime;
    /**
     * 存储失败消息线程存活时间单位
     */
    private TimeUnit timeUnit;
    /**
     * 存储失败消息线程池工作队列
     */
    private Integer workQueueNum;
    /**
     * 存储服务记录接口url
     */
    @NotBlank(message = "url is blank")
    private String storeMessageUrl;
     /**
     * 失败队列重试次数（尝试将失败mq消息存储到数据库）
     */
    private Integer failQueueRetryTimes;
}
```
``` java
public class MqMessage implements Serializable {
     private static final long serialVersionUID = -1L;
    /**
     * 业务主键
     */
    private String key;
    /**
     * 发送主题
     */
    @NotBlank(message = "topic is blank")
    private String topic;
    /**
     * 发送标签
     */
    private String tag;
    /**
     * 发送消息体
     */
    @NotBlank(message = "body is blank")
    private String body;
    /**
     * mq消息到达目标时间(无需设置)
     */
    private Date targetTime;
    /**
     * 发送人
     */
    @NotBlank(message = "operator is blank")
    private String operator;
    /**
     * 应用名称
     */
    @NotBlank(message = "application is blank")
    private String application;
    /**
     * 发送方式 0：同步（sync） 1：异步（async） 2：oneway（不考虑发送结果）
     */
    private Integer sendWay;

    /**
     * 延迟时间（mills为单位）
     */
    private Long delayTime;

    /**
     * 已重试次数
     */
    private Integer retryTimes;

}
```
### 进行发送
``` java
   SendResult sendResult = mqProducerClient.sendMessage(mqMessage);
```
### 需要关注的问题
1. 补发需要注意消息的幂等性，消费的时候要避免重复消费的可能性
2. 支持sync（同步）、async（异步）、oneway（单向）三种方式发送，异步方法不支持补发，只提供正常发送的能力，单向方法不支持延时发送
3. 因接口超时引起的dubbo重试可能导致发起多次消息，多次消息失败对应也会有多次补发发生，需要考虑这种情况的有效杜绝
4. 失败队列中的消息会进行自动重试操作（将信息存储到数据库），重试多次（次数可自定义，默认为5次）仍失败的消息会被丢弃