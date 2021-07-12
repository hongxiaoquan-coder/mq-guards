package com.hk.simba.mq.guards.domain.impl;

import com.hk.simba.base.beanmapper.BeanMapper;
import com.hk.simba.mq.guards.domain.MqSendLogsService;
import com.hk.simba.mq.guards.domain.param.InitMqSendLogsParams;
import com.hk.simba.mq.guards.infrastructure.database.entity.MqSendLogs;
import com.hk.simba.mq.guards.infrastructure.database.mapper.MqSendLogsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Service
@Slf4j
public class MqSendLogsServiceImpl implements MqSendLogsService {

    @Autowired
    private MqSendLogsMapper mqSendLogsMapper;
    @Autowired
    private BeanMapper beanMapper;

    @Override
    public void insert(InitMqSendLogsParams params) {
        MqSendLogs mqSendLogs = beanMapper.map(params, MqSendLogs.class);
        mqSendLogs.setCreateTime(new Date());
        mqSendLogsMapper.insertSelective(mqSendLogs);
    }

    @Override
    public List<MqSendLogs> queryUnReissuedMessages(Date startTime, Date endTime, Integer status, Integer maxRetryTimes) {
        return mqSendLogsMapper.selectAllByCreateTimeAndStatus(startTime, endTime, status, maxRetryTimes);
    }

    @Override
    public int updateRetriedTimesAndStatus(Long id, Integer retriedTimes, Integer status) {
        return mqSendLogsMapper.updateRetriedTimesAndStatus(id, retriedTimes, status);
    }
}
