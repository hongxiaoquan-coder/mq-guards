package com.hk.simba.mq.guards;
import com.google.common.collect.Lists;

import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.domain.param.SendMqByHandParams;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * RocketMQ示例
 *
 * @author lijianghui
 * @date 2020-05-12 18:03
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MqServiceTest {

    @Autowired
    MqSendLogsService mqSendLogsService;

    @Test
    public void testMqSender() {
        log.info("开始发送mq消息");
    }

    @Test
    public void testInsertMq(){
        InitMqSendLogsParams params = new InitMqSendLogsParams();
        params.setApplication("SAS");
        params.setTopic("test");
        params.setTag("test");
        params.setBody("test");
        params.setMqKey("test");
        params.setProducerId("test");
        params.setMaxRetryTimes(5);
        params.setMqMaxRetryTimes(5);
        params.setWaitTime(0L);
        params.setTargetTime(new Date());
        params.setCreateBy("test");
        params.setSendWay(0);
        mqSendLogsService.insert(params);
    }

    @Test
    public void testSendByHand(){
        SendMqByHandParams params = new SendMqByHandParams();
        params.setApplication("SAS");
//        params.setTopic("");
//        params.setProducerId("");
//        params.setIdList(Lists.newArrayList());
//        params.setBeginTime(new Date());
//        params.setEndTime(new Date());
        mqSendLogsService.sendByHand(params);
    }

}
