package com.hk.simba.mq.guards.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Allen
 * @since 2021-07-02
 */
@Getter
@AllArgsConstructor
public enum SendWayEnum {
    /**
     * 同步发送
     */
    SYNC(0, "sync", "同步"),
    /**
     * 异步发送
     */
    ASYNC(1, "async", "异步"),
    /**
     * 单向发送
     */
    ONEWAY(2,"oneway","单向");

    /**
     * 发送方式编码
     */
    private final Integer code;
    /**
     * 发送值
     */
    private final String value;
    /**
     * 发送方式描述
     */
    private final String desc;

    public static SendWayEnum get(Integer code) {
        return Arrays.stream(SendWayEnum.values()).filter(item -> item.code.equals(code)).findAny()
            .orElseThrow(() -> new RuntimeException("不支持的发送方式"));
    }

}
