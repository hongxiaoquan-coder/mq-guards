package com.hk.simba.base.rocketmq;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Allen
 * @since 2021-07-02
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BaseResponse {

    private Integer code;
    private String message;
    private Object data;

    public boolean isSuccess(){
        return this.code == MqGuardsConstant.CODE_SUCCESS;
    }

}
