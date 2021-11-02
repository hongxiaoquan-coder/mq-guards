package com.hk.simba.mq.guards.access.api;

import com.alibaba.fastjson.JSON;
import com.hk.base.dto.response.BaseResponse;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.domain.param.SendMqByHandParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Allen
 * @since 2021-07-01
 */
@RestController
@RequestMapping("/mq")
@Slf4j
public class MqSendLogsController {

    @Autowired
    private MqSendLogsService mqSendLogsService;

    @PostMapping("/insert")
    public BaseResponse insert(@RequestBody InitMqSendLogsParams params){
        log.info("记录mq失败消息参数={}", JSON.toJSONString(params));
        mqSendLogsService.insert(params);
        return BaseResponse.success("初始化mq失败记录成功");
    }

    @PostMapping("/sendByHand")
    public BaseResponse sendByHand(@RequestBody SendMqByHandParams params){
        log.info("手动补发mq消息参数={}", JSON.toJSONString(params));
        mqSendLogsService.sendByHand(params);
        return BaseResponse.success("消息补发成功");
    }

}
