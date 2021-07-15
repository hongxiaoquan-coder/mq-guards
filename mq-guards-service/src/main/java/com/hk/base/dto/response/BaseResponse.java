package com.hk.base.dto.response;

import com.alibaba.fastjson.JSON;
import com.hk.simba.mq.guards.infrastructure.constant.MqGuardsConstant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Allen
 * @since 2021-07-01
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseResponse {

    private Integer code;
    private String message;
    private Object data;

    private BaseResponse(int code, String message) {
        this.setCode(this.code);
        this.setMessage(message);
        this.setData((Object) null);
    }

    public static BaseResponse create(int code, String message) {
        return new BaseResponse(code, message, (Object) null);
    }

    public static BaseResponse create(int code, String message, Object data) {
        return new BaseResponse(code, message, data);
    }

    public static BaseResponse error(String message, Object data) {
        return new BaseResponse(MqGuardsConstant.CODE_ERROR, message, data);
    }

    public static BaseResponse error(String message) {
        return new BaseResponse(MqGuardsConstant.CODE_ERROR, message, "");
    }

    public static BaseResponse success(String message, Object data) {
        return new BaseResponse(MqGuardsConstant.CODE_SUCCESS, message, data);
    }

    public static BaseResponse success(String message) {
        return new BaseResponse(MqGuardsConstant.CODE_SUCCESS, message, "");
    }

    public static BaseResponse success(Object data) {
        return new BaseResponse(MqGuardsConstant.CODE_SUCCESS, "请求成功", data);
    }

    public String toJSONBackendResponse() {
        return JSON.toJSONString(this);
    }
}
