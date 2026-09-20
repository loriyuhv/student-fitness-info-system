package com.wsw.fitnesssystem.shared.interfaces.web.exception;

import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.error.IamAuthZErrorCode;
import com.wsw.fitnesssystem.shared.kernel.exception.BizException;
import com.wsw.fitnesssystem.shared.kernel.exception.SystemException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainConflictException;
import com.wsw.fitnesssystem.shared.domain.exception.DomainException;
import com.wsw.fitnesssystem.shared.infrastructure.persistence.ConstraintResultCodeMapper;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ApiResponse;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * 全局异常处理器（异常处理链的最后一站）。
 *
 * <p><b>核心职责：</b>将 Controller 及其下游抛出的所有异常，转换为统一的
 * {@link ApiResponse} 响应体 + 真实 HTTP 状态码。所有未被局部捕获的异常最终
 * 都会汇聚于此，本类是「异常 → HTTP 响应」的唯一翻译层。</p>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *   <li><b>单一职责：</b>只做「异常 → 响应」的转换，不含任何业务逻辑</li>
 *   <li><b>统一结构：</b>所有响应遵循 {@link ApiResponse} 结构
 *       （{@code httpCode} / {@code bizCode} / {@code message} / {@code data} / {@code timestamp}）</li>
 *   <li><b>日志分层：</b>业务异常（WARN）与系统异常（ERROR）分级记录</li>
 *   <li><b>调用方友好：</b>4xx 透传具体原因；5xx 只返回友好文案，不暴露堆栈</li>
 *   <li><b>HTTP 语义真实：</b>返回真实 HTTP 状态（401/403/409/500…），
 *       而非恒 200，便于网关、APM、浏览器 DevTools 正确识别</li>
 * </ul>
 *
 * <p><b>handler 优先级（Spring MVC 匹配规则）：</b></p>
 * <p>Spring MVC 按「异常继承深度」匹配 handler，越具体的异常越优先。
 * 本类声明顺序不影响匹配结果，但按「具体 → 抽象」排列便于阅读。</p>
 *
 * <p><b>响应构造统一约定：</b>所有 handler 通过 {@link #build(ErrorCode, String)}
 * 构造响应，由 {@link HttpStatusResolver} 解析 HTTP 状态。禁止在 handler 内
 * 手动 {@code new ApiResponse(...)}。</p>
 *
 * <p><b>与 Spring Security 的关系：</b>Spring Security 的认证 / 授权异常不走
 * 本类（发生在 Filter Chain，早于 DispatcherServlet），由
 * {@code SecurityResponseWriter} 单独处理，但响应结构与本类保持一致。</p>
 *
 * <p><b>异常体系分工：</b></p>
 * <ul>
 *   <li>{@link BizException}：业务规则不满足（4xx，WARN）</li>
 *   <li>{@link SystemException}：技术系统故障（5xx，ERROR）</li>
 *   <li>{@link DomainException}：领域异常（通常由应用层翻译，本类兜底处理）</li>
 * </ul>
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
    //  1. 业务异常（4xx，WARN）
    //     触发条件：业务规则不满足、状态不合法、资源不存在等
    //     日志级别：WARN（可预期，调用方可修正）
    // ================================================================

    /**
     * 业务异常处理器。
     *
     * <p><b>触发条件：</b>应用层主动抛出 {@link BizException}（含子类）。</p>
     * <p><b>典型场景：</b>手机号重复、密码错误、权限不足、资源不存在。</p>
     * <p><b>日志级别：</b>WARN。属于可预期失败，不需要开发介入。</p>
     * <p><b>响应：</b>使用异常携带的 {@link ErrorCode}，由
     * {@link HttpStatusResolver} 解析为 4xx 状态码；消息取「错误码默认文案
     * + 异常自定义文案」的合并结果。</p>
     *
     * @param e 业务异常
     * @return 统一错误响应（4xx）
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Object>> handleBizException(BizException e) {
        ErrorCode ec = e.getErrorCode();
        String finalMsg = buildCombineMessage(ec.message(), e.getMessage());
        log.warn("Business exception: {}", finalMsg, e);
        return build(ec, finalMsg);
    }

    // ================================================================
    //  2. 参数校验异常（4xx，WARN）
    //     触发条件：Spring Validation / Web 层参数绑定失败
    //     日志级别：WARN（客户端输入错误）
    // ================================================================

    /**
     * JSON 请求体验证失败（{@code @RequestBody + @Valid}）。
     *
     * <p><b>典型场景：</b>字段为空但标注 {@code @NotBlank}、数值越界、
     * 邮箱格式错误。</p>
     * <p><b>处理策略：</b>取第一个校验失败的字段消息，避免一次性返回
     * 过多错误导致前端处理复杂。</p>
     *
     * @param e 校验异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
            ? fieldError.getDefaultMessage()
            : CommonErrorCode.PARAM_INVALID.message();
        log.warn("JSON request body validation failed: {}", msg);
        return build(CommonErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 表单 / Query 参数绑定验证失败（{@code @ModelAttribute}）。
     *
     * <p><b>与 {@link #handleMethodArgumentNotValidException} 的区别：</b>
     * 前者处理 JSON 请求体，本方法处理表单/Query 参数。</p>
     *
     * @param e 绑定异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
            ? fieldError.getDefaultMessage()
            : CommonErrorCode.PARAM_INVALID.message();
        log.warn("Form parameter binding failed: {}", msg);
        return build(CommonErrorCode.PARAM_INVALID, msg);
    }

    /**
     * {@code @RequestParam} / {@code @PathVariable} 参数校验失败。
     *
     * <p><b>触发条件：</b>Controller 类标注 {@code @Validated}，方法参数使用
     * 校验注解（如 {@code @NotBlank}、{@code @Min}）。</p>
     *
     * @param e 校验异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
        ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse(CommonErrorCode.PARAM_INVALID.message());
        log.warn("Request parameter validation failed: {}", msg);
        return build(CommonErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 请求体 JSON 格式非法。
     *
     * <p><b>典型场景：</b>缺少闭合括号、字段间缺逗号、布尔值用整数。</p>
     *
     * @param e 格式异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e) {
        log.warn("Malformed JSON request body: {}", e.getMessage());
        return build(CommonErrorCode.REQUEST_FORMAT_ERROR, null);
    }

    /**
     * 参数类型转换失败。
     *
     * <p><b>典型场景：</b>数字类型传入非数字字符串（{@code ?page=abc}）、
     * 日期格式不匹配、枚举值非法。</p>
     *
     * @param e 类型转换异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(TypeMismatchException e) {
        String msg = String.format("%s：参数 '%s' 需要类型 '%s'",
            CommonErrorCode.PARAM_TYPE_ERROR.message(),
            e.getPropertyName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("Parameter type conversion failed: {}", msg);
        return build(CommonErrorCode.PARAM_TYPE_ERROR, msg);
    }

    // ================================================================
    //  3. 文件上传异常（4xx / 5xx，WARN）
    //     触发条件：文件上传过程相关错误
    // ================================================================

    /**
     * 上传文件大小超出限制。
     *
     * <p><b>触发条件：</b>超过 {@code spring.servlet.multipart.max-file-size} 配置
     * （当前 200MB）。</p>
     *
     * @param e 文件大小超限异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceeded(
        MaxUploadSizeExceededException e) {
        log.warn("File size exceeds limit: {}", e.getMessage());
        return build(CommonErrorCode.PARAM_INVALID, "文件大小超出限制，最大 200MB");
    }

    /**
     * 文件上传异常（兜底）。
     *
     * <p><b>覆盖场景：</b>缺少文件、文件解析失败、文件存储失败。</p>
     *
     * @param e 文件上传异常
     * @return 统一错误响应（500）
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMultipartException(MultipartException e) {
        log.warn("File upload exception: {}", e.getMessage());
        return build(CommonErrorCode.FILE_UPLOAD_ERROR, null);
    }

    // ================================================================
    //  4. Spring Web 层客户端异常（4xx，WARN）
    //     触发条件：HTTP 请求语义错误
    // ================================================================

    /**
     * 缺少必填参数。
     *
     * <p><b>触发条件：</b>{@code @RequestParam(required=true)}（默认）未提供。</p>
     *
     * @param e 缺少参数异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(
        MissingServletRequestParameterException e) {
        String msg = "缺少必填参数：" + e.getParameterName();
        log.warn("Missing required parameter: {}", e.getParameterName());
        return build(CommonErrorCode.PARAM_MISSING, msg);
    }

    /**
     * 请求方法不支持。
     *
     * <p><b>典型场景：</b>POST 接口被 GET 访问、DELETE 接口被 PUT 访问。</p>
     *
     * @param e 方法不支持异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException e) {
        String msg = "请求方法不支持：" + e.getMethod();
        log.warn("Request method not supported: {}", e.getMethod());
        return build(CommonErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 媒体类型不支持。
     *
     * <p><b>典型场景：</b>JSON 接口收到 {@code application/xml}。</p>
     *
     * @param e 媒体类型不支持异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException e) {
        String msg = "不支持的媒体类型：" + (e.getContentType() != null ? e.getContentType() : "未知");
        log.warn("Media type not supported: {}", msg);
        return build(CommonErrorCode.REQUEST_FORMAT_ERROR, msg);
    }

    /**
     * 请求资源不存在（Spring MVC 静态资源 / 路径匹配失败）。
     *
     * <p><b>触发条件：</b>请求路径未匹配到任何 Controller 方法或静态资源。</p>
     *
     * @param e 资源不存在异常
     * @return 统一错误响应（404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("Requested resource not found: {}", e.getMessage());
        return build(CommonErrorCode.FILE_NOT_FOUND, "请求的资源不存在");
    }

    /**
     * Spring Web 层异常兜底（未被上述特定处理器覆盖的 ServletException）。
     *
     * <p><b>覆盖场景：</b>路径变量缺失、请求参数绑定失败等框架层异常。</p>
     *
     * @param e Servlet 异常
     * @return 统一错误响应（400）
     */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiResponse<Object>> handleServletException(ServletException e) {
        String finalMsg = buildCombineMessage(CommonErrorCode.PARAM_INVALID.message(), e.getMessage());
        log.warn("Web request exception: {}", finalMsg);
        return build(CommonErrorCode.PARAM_INVALID, finalMsg);
    }

    // ================================================================
    //  5. 认证与权限异常（401 / 403，WARN）
    //     注意：Spring Security 的异常通常不走本类（见类级 Javadoc），
    //     但当异常传播到 DispatcherServlet 时仍会被捕获
    // ================================================================

    /**
     * 权限不足异常。
     *
     * <p><b>触发条件：</b>当前已认证用户缺少访问目标资源所需的角色或权限。</p>
     * <p><b>HTTP 映射：</b>403 Forbidden。</p>
     *
     * @param e 权限异常
     * @return 统一错误响应（403）
     */
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(Exception e) {
        log.warn("Authorization exception: {}", e.getMessage());
        return build(IamAuthZErrorCode.PERMISSION_DENIED, null);
    }

    /**
     * 认证失败异常。
     *
     * <p><b>触发条件：</b>请求未携带有效凭证，或凭证无效 / 过期。</p>
     * <p><b>HTTP 映射：</b>401 Unauthorized。</p>
     * <p><b>典型场景：</b>未登录访问需认证接口、Token 过期、RefreshToken 刷新失败。</p>
     *
     * @param e 认证异常
     * @return 统一错误响应（401）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
        String defaultMsg = IamAuthNErrorCode.CREDENTIAL_INVALID.message();
        String finalMsg = buildCombineMessage(defaultMsg, e.getMessage());
        log.warn("Authentication failed: {}", finalMsg);
        return build(IamAuthNErrorCode.CREDENTIAL_INVALID, finalMsg);
    }

    // ================================================================
    //  6. 数据完整性 / 领域异常（4xx / 5xx）
    // ================================================================

    /**
     * 数据完整性约束违反（数据库层）。
     *
     * <p><b>触发条件：</b>MyBatis 未在 Repository 层转换的唯一约束冲突。</p>
     * <p><b>处理策略：</b>优先通过 {@link ConstraintResultCodeMapper} 解析
     * 约束名，映射为业务错误码；未识别则返回通用数据库错误。</p>
     *
     * @param e 数据完整性异常
     * @return 统一错误响应（409 或 500）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
        DataIntegrityViolationException e) {
        ErrorCode ec = ConstraintResultCodeMapper.resolve(e.getMessage());
        if (ec != null) {
            log.warn("Unique constraint conflict (fallback hit): {}", ec.message());
            return build(ec, null);
        }
        log.error("Data integrity exception (unrecognized constraint)", e);
        return build(CommonErrorCode.DATABASE_ERROR, null);
    }

    /**
     * 领域异常兜底处理。
     *
     * <p><b>正常流程：</b>领域异常应由应用层翻译为 {@link BizException}，
     * 走 {@link #handleBizException}。</p>
     * <p><b>本处理器作用：</b>兜底——若某处领域异常未被应用层翻译，
     * 本类做最后一道防线。属于开发遗漏，日志会记录以便定位。</p>
     *
     * @param e 领域异常
     * @return 统一错误响应（409 或 400）
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Object>> handleDomainException(DomainException e) {
        log.warn("Domain exception (not translated by application layer, handled as fallback): {}",
            e.getMessage()
        );
        ErrorCode ec = (e instanceof DomainConflictException)
            ? CommonErrorCode.DATA_ALREADY_EXISTS
            : CommonErrorCode.PARAM_INVALID;
        return build(ec, e.getMessage());
    }

    // ================================================================
    //  7. 系统异常 / 兜底（5xx，ERROR）
    //     触发条件：技术系统故障、未知异常
    // ================================================================

    /**
     * 系统异常处理器。
     *
     * <p><b>触发条件：</b>基础设施层抛出 {@link SystemException}（DB、Redis、
     * IO、第三方接口等技术故障）。</p>
     * <p><b>日志级别：</b>ERROR。需运维/开发介入，建议接入告警。</p>
     * <p><b>响应脱敏：</b>不向客户端暴露堆栈、SQL 等内部信息。</p>
     *
     * @param e 系统异常
     * @return 统一错误响应（5xx）
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Object>> handleSystemException(SystemException e) {
        ErrorCode ec = e.getErrorCode();
        String finalMsg = buildCombineMessage(ec.message(), e.getMessage());
        log.error("System exception: {}", finalMsg, e);
        return build(ec, finalMsg);
    }

    /**
     * 未捕获异常处理器（系统最后一道防线）。
     *
     * <p><b>设计原则：</b>本处理器捕获的异常理论上不应发生。一旦触发说明
     * 存在未被预见的 Bug，需要开发介入排查。</p>
     * <p><b>日志级别：</b>ERROR。打印完整堆栈便于定位。</p>
     * <p><b>响应脱敏：</b>只返回通用错误码，不暴露任何内部信息。</p>
     *
     * @param e 未捕获的异常
     * @return 统一错误响应（500）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnknownException(Exception e) {
        log.error("Unknown system exception (please investigate)", e);
        return build(CommonErrorCode.SYSTEM_ERROR, null);
    }

    // ================================================================
    //  8. 工具方法
    // ================================================================

    /**
     * 统一构造 {@link ResponseEntity}。
     *
     * <p><b>为什么统一走此方法：</b></p>
     * <ul>
     *   <li>保证所有 handler 的响应结构一致（{@code ApiResponse}）</li>
     *   <li>HTTP 状态由 {@link HttpStatusResolver} 集中解析，避免散落</li>
     *   <li>消息合并逻辑统一（{@link #buildCombineMessage}）</li>
     * </ul>
     *
     * @param ec            错误码契约（由各 handler 传入具体枚举常量）
     * @param customMessage 自定义消息（可为 null）
     * @return 统一响应实体（HTTP 状态 + 响应体）
     */
    private ResponseEntity<ApiResponse<Object>> build(ErrorCode ec, String customMessage) {
        HttpStatus status = httpStatusResolver.resolve(ec);
        String finalMsg = buildCombineMessage(ec.message(), customMessage);
        ApiResponse<Object> body = ApiResponse.error(status.value(), ec, finalMsg);
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    /**
     * 合并「默认消息」与「自定义消息」。
     *
     * <p><b>拼接规则：</b></p>
     * <ol>
     *   <li>customMsg 为 null / 空白 / 与 defaultMsg 相同 → 返回 defaultMsg</li>
     *   <li>否则 → 返回 {@code defaultMsg + "：" + customMsg}</li>
     * </ol>
     *
     * <p><b>示例：</b></p>
     * <ul>
     *   <li>defaultMsg="参数不合法"，customMsg="用户名不能为空" → "参数不合法：用户名不能为空"</li>
     *   <li>defaultMsg="参数不合法"，customMsg="参数不合法" → "参数不合法"（去重）</li>
     *   <li>defaultMsg="参数不合法"，customMsg=null → "参数不合法"</li>
     * </ul>
     *
     * @param defaultMsg 默认消息（来自 {@link ErrorCode#message()}）
     * @param customMsg  自定义消息（来自异常构造参数）
     * @return 合并后的最终消息
     */
    private String buildCombineMessage(String defaultMsg, String customMsg) {
        if (customMsg == null || customMsg.isBlank() || customMsg.equals(defaultMsg)) {
            return defaultMsg;
        }
        return defaultMsg + "：" + customMsg;
    }

}