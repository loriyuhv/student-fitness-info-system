package com.wsw.fitnesssystem.shared.interfaces.web.exception;

import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.error.IamAuthZErrorCode;
import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.application.exception.SystemException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainConflictException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainException;
import com.wsw.fitnesssystem.shared.infrastructure.persistence.ConstraintResultCodeMapper;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResult;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。
 * <p>
 * <b>核心职责：</b>将系统内部各种异常转换为统一的 HTTP 响应格式 {@link ApiResult}。
 * 作为"异常处理链的最后一站"，所有未被局部捕获的异常最终汇聚于此。
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *   <li>单一职责：只负责异常 → HTTP 响应的转换，不包含业务逻辑</li>
 *   <li>统一格式：所有响应遵循 {@link ApiResult} 规范，前端无需特殊处理</li>
 *   <li>日志分层：客户端错误（WARN）与服务器错误（ERROR）区分记录</li>
 *   <li>调用方友好：400 级错误透传具体原因，500 级错误脱敏处理</li>
 * </ul>
 * <p>
 * <b>处理器优先级：</b>按照"具体 → 抽象"排列，避免被更宽泛的处理器提前拦截。
 *
 * 全局异常处理器。
 *
 * <p>P3 阶段改动：注入 {@link HttpStatusResolver}，所有错误码切换到各模块自治枚举。
 * 对外协议不变（httpCode 仍在 body 里）。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:23
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {


    private final HttpStatusResolver httpStatusResolver;

    // ================================================================
    //  1. 业务异常
    // ================================================================

    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        ErrorCode ec = e.getErrorCode();
        String finalMsg = buildCombineMessage(ec.message(), e.getMessage());
        log.warn("业务异常: {}", finalMsg, e);
        return ApiResult.error(httpStatusResolver.resolveValue(ec), ec, finalMsg);
    }

    // ================================================================
    //  2. 参数校验异常
    // ================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
            ? fieldError.getDefaultMessage()
            : CommonErrorCode.PARAM_INVALID.message();
        log.warn("JSON 请求体验证失败：{}", msg);
        return error(CommonErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResult<Object> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
            ? fieldError.getDefaultMessage()
            : CommonErrorCode.PARAM_INVALID.message();
        log.warn("表单参数绑定失败：{}", msg);
        return error(CommonErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Object> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse(CommonErrorCode.PARAM_INVALID.message());
        log.warn("请求参数校验失败：{}", msg);
        return error(CommonErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体 JSON 格式非法：{}", e.getMessage());
        return error(CommonErrorCode.REQUEST_FORMAT_ERROR, null);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ApiResult<Object> handleTypeMismatch(TypeMismatchException e) {
        String msg = String.format("%s：参数 '%s' 需要类型 '%s'",
            CommonErrorCode.PARAM_TYPE_ERROR.message(),
            e.getPropertyName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型转换失败：{}", msg);
        return error(CommonErrorCode.PARAM_TYPE_ERROR, msg);
    }

    // ================================================================
    //  3. 文件上传异常
    // ================================================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制：{}", e.getMessage());
        return error(CommonErrorCode.PARAM_INVALID, "文件大小超出限制，最大 200MB");
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResult<Object> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常：{}", e.getMessage());
        return error(CommonErrorCode.FILE_UPLOAD_ERROR, null);
    }

    // ================================================================
    //  4. Spring Web 层客户端异常
    // ================================================================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Object> handleMissingParams(MissingServletRequestParameterException e) {
        String msg = "缺少必填参数：" + e.getParameterName();
        log.warn("缺少必填参数：{}", e.getParameterName());
        return error(CommonErrorCode.PARAM_MISSING, msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = "请求方法不支持：" + e.getMethod();
        log.warn("请求方法不支持：{}", e.getMethod());
        return error(CommonErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<Object> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        String msg = "不支持的媒体类型：" + (e.getContentType() != null ? e.getContentType() : "未知");
        log.warn("媒体类型不支持：{}", msg);
        return error(CommonErrorCode.REQUEST_FORMAT_ERROR, msg);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<Object> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("请求资源不存在：{}", e.getMessage());
        return error(CommonErrorCode.FILE_NOT_FOUND, "请求的资源不存在");
    }

    @ExceptionHandler(ServletException.class)
    public ApiResult<Object> handleServletException(ServletException e) {
        String finalMsg = buildCombineMessage(CommonErrorCode.PARAM_INVALID.message(), e.getMessage());
        log.warn("Web 请求异常：{}", finalMsg);
        return error(CommonErrorCode.PARAM_INVALID, finalMsg);
    }

    // ================================================================
    //  5. 认证与权限异常
    // ================================================================

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ApiResult<Object> handleAccessDeniedException(Exception e) {
        log.warn("权限异常: {}", e.getMessage());
        return error(IamAuthZErrorCode.PERMISSION_DENIED, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Object> handleAuthenticationException(AuthenticationException e) {
        String defaultMsg = IamAuthNErrorCode.CREDENTIAL_INVALID.message();
        String finalMsg = buildCombineMessage(defaultMsg, e.getMessage());
        log.warn("认证失败: {}", finalMsg);
        return error(IamAuthNErrorCode.CREDENTIAL_INVALID, finalMsg);
    }

    // ================================================================
    //  6. 数据完整性 / 领域异常
    // ================================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Object> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode ec = ConstraintResultCodeMapper.resolve(e.getMessage());
        if (ec != null) {
            log.warn("唯一约束冲突（兜底命中）: {}", ec.message());
            return error(ec, null);
        }
        log.error("数据完整性异常（未识别的约束）", e);
        return error(CommonErrorCode.DATABASE_ERROR, null);
    }

    @ExceptionHandler(DomainException.class)
    public ApiResult<Object> handleDomainException(DomainException e) {
        log.warn("领域异常（未被应用层翻译，兜底处理）: {}", e.getMessage());
        ErrorCode ec = (e instanceof DomainConflictException)
            ? CommonErrorCode.DATA_ALREADY_EXISTS
            : CommonErrorCode.PARAM_INVALID;
        return error(ec, e.getMessage());
    }

    // ================================================================
    //  7. 系统异常 / 兜底
    // ================================================================

    @ExceptionHandler(SystemException.class)
    public ApiResult<Object> handleSystemException(SystemException e) {
        ErrorCode ec = e.getErrorCode();
        String finalMsg = buildCombineMessage(ec.message(), e.getMessage());
        log.error("系统异常: {}", finalMsg, e);
        return ApiResult.error(httpStatusResolver.resolveValue(ec), ec, finalMsg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleUnknownException(Exception e) {
        log.error("系统未知异常（请开发排查）", e);
        return error(CommonErrorCode.SYSTEM_ERROR, null);
    }

    // ================================================================
    //  8. 工具方法
    // ================================================================

    /** 便捷构造：自动解析 httpCode */
    private ApiResult<Object> error(ErrorCode ec, String customMessage) {
        String finalMsg = buildCombineMessage(ec.message(), customMessage);
        return ApiResult.error(httpStatusResolver.resolveValue(ec), ec, finalMsg);
    }

    /**
     * 组装消息。
     *
     * <p>若 customMsg 为 null / 空 / 与 defaultMsg 相同，直接返回 defaultMsg；否则拼接。</p>
     */
    private String buildCombineMessage(String defaultMsg, String customMsg) {
        if (customMsg == null || customMsg.isBlank() || customMsg.equals(defaultMsg)) {
            return defaultMsg;
        }
        return defaultMsg + "：" + customMsg;
    }

}
