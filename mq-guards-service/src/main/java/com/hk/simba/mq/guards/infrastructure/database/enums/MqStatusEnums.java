package com.hk.simba.mq.guards.infrastructure.database.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Getter
@AllArgsConstructor
public enum MqStatusEnums {

    /**
     * 初始化
     */
    INIT(0,"初始化"),
    /**
     * 成功
     */
    SUCCESS(1,"成功"),
    /**
     * 失败
     */
    FAIL(2,"失败");

    private final int code;
    private final String desc;

}
