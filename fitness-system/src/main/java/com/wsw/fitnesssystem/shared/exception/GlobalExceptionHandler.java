package com.wsw.fitnesssystem.shared.exception;

import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * - 负责将异常 → HTTP 响应
 * - 是“异常的最后一站”
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:23
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 业务异常 → 400
     * 拼接规则：ResultCode默认消息 + "：" + 自定义消息
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        ResultCode rc = e.getResultCode();
        String defaultMsg = rc.getMessage();
        String customMsg = e.getMessage();
        String finalMsg = buildCombineMessage(defaultMsg, customMsg);

        log.warn("业务异常: {}", finalMsg);
        return ApiResult.error(rc, finalMsg);
    }

    /**
     * JSON请求体 @RequestBody + @Valid 校验失败
     * @param e 异常
     * @return 统一响应体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
                ? fieldError.getDefaultMessage()
                : ResultCode.PARAM_INVALID.getMessage();
        log.warn("参数校验失败：{}", msg);
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }

    /**
     * 表单、GET参数校验失败
     * @param e 异常
     * @return 响应体
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Object> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
                ? fieldError.getDefaultMessage()
                : ResultCode.PARAM_INVALID.getMessage();
        log.warn("参数校验失败 ：{}", msg);
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }

    /**
     * FIX: 请求体 JSON 格式非法（如缺少引号、逗号等）
     * @param e 异常
     * @return 通用响应体
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误：{}", e.getMessage());
        // return ApiResult.error(ResultCode.REQUEST_FORMAT_ERROR, "请求体 JSON 格式非法");
        return ApiResult.error(ResultCode.REQUEST_FORMAT_ERROR);
    }

    /**
     * 权限不足 → 403
     */
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ApiResult<Object> handleAccessDeniedException(Exception e) {
        log.warn("权限异常: {}", e.getMessage());
        return ApiResult.error(ResultCode.PERMISSION_DENIED);
    }


    /**
     * 认证异常 → 401
     * @param e 异常
     * @return 错误响应体
     */
    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Object> handleAuthenticationException(AuthenticationException e) {
        String defaultMsg = ResultCode.CREDENTIAL_INVALID.getMessage();
        String customMsg = e.getMessage();
        String finalMsg = buildCombineMessage(defaultMsg, customMsg);
        log.warn("认证失败: {}", finalMsg);
        return ApiResult.error(ResultCode.CREDENTIAL_INVALID, finalMsg);
    }

    /**
     * 统一捕获SpringMVC Web层所有客户端请求异常
     * <p>包含：缺少参数、缺少RequestPart、请求方法不对、媒体类型不对、路径变量缺失、参数类型转换失败等
     * 全部归类为客户端参数错误 400</p>
     *
     * @param e 异常对象
     * @return 通用响应体
     */
    @ExceptionHandler(ServletException.class)
    public ApiResult<Object> handleServletException(ServletException e) {
        String defaultMsg = ResultCode.PARAM_INVALID.getMessage();
        String customMsg = e.getMessage();
        String finalMsg = buildCombineMessage(defaultMsg, customMsg);
        log.warn("WEB请求客户端异常：{}", finalMsg);
        return ApiResult.error(ResultCode.PARAM_INVALID, finalMsg);
    }

    /**
     * 系统异常 → 500
     */
    @ExceptionHandler(SystemException.class)
    public ApiResult<Object> handleSystemException(SystemException e) {
        ResultCode rc = e.getResultCode();
        String defaultMsg = rc.getMessage();
        String customMsg = e.getMessage();
        String finalMsg = buildCombineMessage(defaultMsg, customMsg);
        log.error("系统异常: {}", finalMsg, e);
        return ApiResult.error(rc, finalMsg);
    }

    /**
     * 未捕获异常 → 系统兜底
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleUnknownException(Exception e) {
        log.error("系统未知异常", e);
        return ApiResult.error(ResultCode.SYSTEM_ERROR);
    }

    /**
     * 组装拼接消息
     * 规则：
     * 1. customMsg 为 null / 空串 / 和默认消息相同，直接返回 defaultMsg
     * 2. 否则：defaultMsg + "：" + customMsg
     *
     * @param defaultMsg 默认消息
     * @param customMsg 自定义消息
     * @return 完整消息
     */
    private String buildCombineMessage(String defaultMsg, String customMsg) {
        if (customMsg == null || customMsg.isBlank() || customMsg.equals(defaultMsg)) {
            return defaultMsg;
        }
        return defaultMsg + "：" + customMsg;
    }
}
