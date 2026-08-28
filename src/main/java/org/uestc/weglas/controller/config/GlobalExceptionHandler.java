package org.uestc.weglas.controller.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.uestc.weglas.base.util.BaseResult;
import org.uestc.weglas.core.enums.ResultEnum;

import java.util.stream.Collectors;

/**
 * 全局异常处理：将 @Valid 校验失败、请求体解析失败等统一为 BaseResult 格式并返回 400，
 * 便于前端统一解析并展示「参数校验失败」或具体错误信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @RequestBody @Valid 校验失败时，Spring 在进入 Controller 前抛出此异常。
     * 返回 400 + BaseResult，与项目约定一致。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResult<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isEmpty()) {
            message = ResultEnum.PARAMETER_ILLEGAL.getMessage();
        }
        BaseResult<Object> body = new BaseResult<>(false, ResultEnum.PARAMETER_ILLEGAL.getCode(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 请求体 JSON 格式错误或无法解析时（如类型不匹配、缺少必填字段导致反序列化失败等）。
     * 返回 400 + BaseResult。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResult<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request body";
        if (message.length() > 200) {
            message = message.substring(0, 200) + "...";
        }
        BaseResult<Object> body = new BaseResult<>(false, ResultEnum.PARAMETER_ILLEGAL.getCode(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
