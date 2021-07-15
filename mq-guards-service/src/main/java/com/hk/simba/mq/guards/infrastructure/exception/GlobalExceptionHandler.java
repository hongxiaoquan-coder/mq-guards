package com.hk.simba.mq.guards.infrastructure.exception;

import com.hk.base.dto.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 * @author Allen
 * @since 2021-07-01
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BaseResponse handleBizException(Exception ex) {
        if (ex instanceof BindException) {
            return BaseResponse.error("参数异常", ((BindException) ex).getFieldError());
        }
        // 未捕获的exception
        log.error("系统错误:{}", ex);
        return BaseResponse.error("系统错误,原因为={}", ex);
    }

}
