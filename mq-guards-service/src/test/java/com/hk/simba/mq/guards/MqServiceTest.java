package com.hk.simba.mq.guards;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void testMqSender() {
        log.info("开始发送mq消息");
    }

}
