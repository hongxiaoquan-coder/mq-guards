package com.hk.simba.mq.guards.access.api;

import com.hk.base.dto.response.BaseResponse;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        log.info("记录mq失败消息={}", params);
        mqSendLogsService.insert(params);
        return BaseResponse.success("初始化mq失败记录成功");
    }

}
