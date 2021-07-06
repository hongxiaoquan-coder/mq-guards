package com.hk.simba.mq.guards.infrastructure.database.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MqStatusEnums {

    INIT(0,"初始化"),
    SUCCESS(1,"成功"),
    FAIL(2,"失败");

    private final int code;
    private final String desc;

}
