package com.wsw.fitnesssystem.shared.exception;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import com.wsw.fitnesssystem.shared.response.ApiResult;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import org.springframework.beans.TypeMismatchException;
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
 * @author loriyuhv
 * @version 1.0 2026/1/14 18:23
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================================================================
    //  1. 业务异常（400 级别）
    //     触发条件：业务规则不满足、参数校验失败、资源不存在等
    //     日志级别：WARN（可预期，调用方可修正）
    // ================================================================

    /**
     * 业务异常处理器。
     * <p>
     * 处理所有 {@link BizException} 及其子类。这是系统中最常见的异常类型，
     * 覆盖参数校验、业务状态、权限检查等主动抛出的业务异常。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>参数校验失败：手机号格式错误、密码长度不足</li>
     *   <li>业务状态冲突：用户已存在</li>
     *   <li>资源不存在：用户不存在、文件未找到</li>
     * </ul>
     * <p>
     * <b>响应处理：</b>使用异常中携带的 {@link ResultCode} 构造响应，
     * 若调用方自定义了消息，则拼接为 "默认消息：自定义消息"。
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Object> handleBizException(BizException e) {
        ResultCode rc = e.getResultCode();
        String finalMsg = buildCombineMessage(rc.getMessage(), e.getMessage());
        log.warn("业务异常: {}", finalMsg, e);
        return ApiResult.error(rc, finalMsg);
    }

    // ================================================================
    //  2. 参数校验异常（400 级别）
    //     触发条件：Spring Validation 框架校验失败
    //     日志级别：WARN（客户端输入格式错误）
    // ================================================================

    /**
     * JSON 请求体验证失败。
     * <p>
     * <b>触发条件：</b>Controller 方法参数使用 {@code @Valid} + {@code @RequestBody}，
     * 且请求体 JSON 字段不满足校验注解约束。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>字段为空但标记了 {@code @NotBlank}</li>
     *   <li>数字超出 {@code @Min}/{@code @Max} 范围</li>
     *   <li>邮箱格式不正确（{@code @Email}）</li>
     * </ul>
     * <p>
     * <b>处理策略：</b>提取第一个校验失败的字段消息返回给前端，
     * 避免一次性返回过多错误信息导致前端处理复杂。
     *
     * @param e 校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
                ? fieldError.getDefaultMessage()
                : ResultCode.PARAM_INVALID.getMessage();
        log.warn("JSON 请求体验证失败：{}", msg);
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }

    /**
     * 表单/Query 参数绑定验证失败。
     * <p>
     * <b>触发条件：</b>
     * <ul>
     *   <li>使用 {@code @ModelAttribute} 绑定表单参数，且字段校验失败</li>
     *   <li>GET 请求的 Query 参数绑定到对象时校验失败</li>
     * </ul>
     * <p>
     * <b>与 {@link #handleMethodArgumentNotValidException} 的区别：</b>
     * 前者处理 JSON 请求体（Content-Type: application/json），
     * 后者处理表单/Query 参数（application/x-www-form-urlencoded）。
     *
     * @param e 绑定异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Object> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null
                ? fieldError.getDefaultMessage()
                : ResultCode.PARAM_INVALID.getMessage();
        log.warn("表单参数绑定失败 ：{}", msg);
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }


    /**
     * {@code @RequestParam}/{@code @PathVariable} 参数验证失败。
     * <p>
     * <b>触发条件：</b>
     * <ol>
     *   <li>Controller 类标记了 {@code @Validated}</li>
     *   <li>方法参数使用了校验注解（如 {@code @NotBlank}、{@code @Min}）</li>
     *   <li>请求传入的参数值不满足约束</li>
     * </ol>
     * <p>
     * <b>典型示例：</b>
     * <pre>
     * &#64;RestController
     * &#64;Validated
     * public class Controller {
     *     &#64;GetMapping("/user")
     *     public void getUser(&#64;RequestParam &#64;NotBlank String userId) {
     *         // userId 为空字符串时会触发此异常
     *     }
     * }
     * </pre>
     *
     * @param e 校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Object> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse(ResultCode.PARAM_INVALID.getMessage());
        log.warn("请求参数校验失败：{}", msg);
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }

    /**
     * 请求体 JSON 格式非法。
     * <p>
     * <b>触发条件：</b>请求体不是合法的 JSON 格式。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>缺少闭合引号或括号</li>
     *   <li>字段间缺少逗号</li>
     *   <li>布尔值用了整数（true 写成 1）</li>
     * </ul>
     *
     * @param e 格式异常
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体 JSON 格式非法：{}", e.getMessage());
        return ApiResult.error(ResultCode.REQUEST_FORMAT_ERROR);
    }

    /**
     * 参数类型转换失败。
     * <p>
     * <b>触发条件：</b>请求参数无法转换为目标类型。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>数字类型传入非数字字符串：{@code ?page=abc}</li>
     *   <li>日期格式不匹配：{@code ?birth=2026-13-01}</li>
     *   <li>枚举类型传入非法值：{@code ?type=UNKNOWN}</li>
     * </ul>
     *
     * @param e 类型转换异常
     * @return 统一错误响应
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ApiResult<Object> handleTypeMismatch(TypeMismatchException e) {
        String msg = String.format("%s：参数 '%s' 需要类型 '%s'",
            ResultCode.PARAM_TYPE_ERROR.getMessage(),
            e.getPropertyName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型转换失败：{}", msg);
        return ApiResult.error(ResultCode.PARAM_TYPE_ERROR, msg);
    }

    // ================================================================
    //  3. 文件上传异常（400 级别）
    //     触发条件：文件上传相关错误
    //     日志级别：WARN（客户端文件操作错误）
    // ================================================================

    /**
     * 文件大小超出限制。
     * <p>
     * <b>触发条件：</b>上传的文件大小超过 Spring 配置的 {@code spring.servlet.multipart.max-file-size}。
     * <p>
     * <b>默认限制：</b>50MB（在 {@code application.yml} 中配置）。
     *
     * @param e 文件大小超限异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制：{}", e.getMessage());
        return ApiResult.error(ResultCode.PARAM_INVALID, "文件大小超出限制，最大 100MB");
    }

    /**
     * 文件上传相关异常（兜底）。
     * <p>
     * <b>覆盖场景：</b>
     * <ul>
     *   <li>缺少文件（但 Controller 参数未加 {@code @NotNull}）</li>
     *   <li>文件解析失败</li>
     *   <li>文件存储失败</li>
     * </ul>
     *
     * @param e 文件上传异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MultipartException.class)
    public ApiResult<Object> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常：{}", e.getMessage());
        return ApiResult.error(ResultCode.FILE_UPLOAD_ERROR);
    }

    // ================================================================
    //  4. Spring Web 层客户端异常（400 级别）
    //     触发条件：HTTP 请求语义错误
    //     日志级别：WARN（客户端请求格式错误）
    // ================================================================

    /**
     * 缺少必填参数。
     * <p>
     * <b>触发条件：</b>Controller 方法声明了 {@code @RequestParam(required=true)}（默认），
     * 但请求中没有提供该参数。
     * <p>
     * <b>注意：</b>如果参数标记了 {@code required=false} 或有默认值，不会触发此异常。
     *
     * @param e 缺少参数异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Object> handleMissingParams(MissingServletRequestParameterException e) {
        String msg = "缺少必填参数：" + e.getParameterName();
        log.warn("缺少必填参数：{}", e.getParameterName());
        return ApiResult.error(ResultCode.PARAM_MISSING, msg);
    }

    /**
     * 请求方法不支持。
     * <p>
     * <b>触发条件：</b>请求的 HTTP 方法与 Controller 定义的映射不匹配。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>POST 接口被 GET 请求访问</li>
     *   <li>DELETE 接口被 PUT 请求访问</li>
     * </ul>
     *
     * @param e 方法不支持异常
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = "请求方法不支持：" + e.getMethod();
        log.warn("请求方法不支持：{}", e.getMethod());
        return ApiResult.error(ResultCode.PARAM_INVALID, msg);
    }

    /**
     * 媒体类型不支持。
     * <p>
     * <b>触发条件：</b>请求的 Content-Type 与 Controller 的 {@code @RequestMapping(consumes=...)} 不匹配。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>JSON 接口收到 application/xml</li>
     *   <li>文件上传接口收到 application/json</li>
     * </ul>
     *
     * @param e 媒体类型不支持异常
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<Object> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        String msg = "不支持的媒体类型：" + (e.getContentType() != null ? e.getContentType() : "未知");
        log.warn("媒体类型不支持：{}", msg);
        return ApiResult.error(ResultCode.REQUEST_FORMAT_ERROR, msg);
    }

    /**
     * 资源不存在（Spring MVC 处理静态资源/路径匹配时）。
     * <p>
     * <b>触发条件：</b>请求的路径没有匹配到任何 Controller 方法或静态资源。
     * <p>
     * <b>注意：</b>这个异常通常在 Spring MVC 默认的 404 处理中被拦截，
     * 但如果在 {@code @RestController} 中抛出了 {@code NoResourceFoundException}，
     * 也会被这里捕获。
     *
     * @param e 资源不存在异常
     * @return 统一错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<Object> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("请求资源不存在：{}", e.getMessage());
        return ApiResult.error(ResultCode.FILE_NOT_FOUND, "请求的资源不存在");
    }

    /**
     * Spring MVC Web 层异常统一处理器（兜底）。
     * <p>
     * <b>设计目的：</b>捕获 {@link ServletException} 及其所有子类中
     * 未被上述特定处理器覆盖的异常。
     * <p>
     * <b>覆盖场景：</b>
     * <ul>
     *   <li>路径变量缺失（{@code @PathVariable(required=true)}）</li>
     *   <li>请求参数绑定失败（框架层异常）</li>
     *   <li>其他 Spring Web 层未分类的异常</li>
     * </ul>
     *
     * @param e Servlet 异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ServletException.class)
    public ApiResult<Object> handleServletException(ServletException e) {
        String finalMsg = buildCombineMessage(ResultCode.PARAM_INVALID.getMessage(), e.getMessage());
        log.warn("Web 请求异常：{}", finalMsg);
        return ApiResult.error(ResultCode.PARAM_INVALID, finalMsg);
    }

    // ================================================================
    //  5. 认证与权限异常（401 / 403 级别）
    //     日志级别：WARN（用户身份/权限相关错误）
    // ================================================================

    /**
     * 权限不足异常。
     * <p>
     * <b>触发条件：</b>当前已认证的用户缺少访问目标资源所需的角色或权限。
     * <p>
     * <b>HTTP 映射：</b>403 Forbidden
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>普通用户访问管理员接口</li>
     *   <li>数据权限校验失败</li>
     *   <li>账号被禁用但仍在尝试访问</li>
     * </ul>
     *
     * @param e 权限异常
     * @return 统一错误响应
     */
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ApiResult<Object> handleAccessDeniedException(Exception e) {
        log.warn("权限异常: {}", e.getMessage());
        return ApiResult.error(ResultCode.PERMISSION_DENIED);
    }

    /**
     * 认证失败异常。
     * <p>
     * <b>触发条件：</b>请求未携带有效凭证，或凭证无效/过期。
     * <p>
     * <b>HTTP 映射：</b>401 Unauthorized
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>未登录访问需要认证的接口</li>
     *   <li>Token 过期或无效</li>
     *   <li>Refresh Token 刷新失败</li>
     * </ul>
     *
     * @param e 认证异常
     * @return 统一错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Object> handleAuthenticationException(AuthenticationException e) {
        String defaultMsg = ResultCode.AUTH_CREDENTIAL_INVALID.getMessage();
        String customMsg = e.getMessage();
        String finalMsg = buildCombineMessage(defaultMsg, customMsg);
        log.warn("认证失败: {}", finalMsg);
        return ApiResult.error(ResultCode.AUTH_CREDENTIAL_INVALID, finalMsg);
    }

    // ================================================================
    //  6. 系统异常（500 级别）
    //     触发条件：系统内部不可预期的技术故障
    //     日志级别：ERROR（需运维/开发介入）
    // ================================================================

    /**
     * 系统异常处理器。
     * <p>
     * <b>触发条件：</b>基础设施层（数据库、缓存、IO、第三方接口）抛出技术故障。
     * <p>
     * <b>典型场景：</b>
     * <ul>
     *   <li>数据库连接失败或 SQL 执行异常</li>
     *   <li>Redis 连接超时或操作失败</li>
     *   <li>文件读写 IO 异常</li>
     *   <li>线程池任务拒绝</li>
     * </ul>
     * <p>
     * <b>HTTP 映射：</b>500 Internal Server Error
     * <p>
     * <b>日志策略：</b>打印完整堆栈（ERROR 级别），便于运维排查。
     *
     * @param e 系统异常
     * @return 统一错误响应（不包含堆栈信息，前端仅看到友好提示）
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

    // ================================================================
    //  7. 兜底异常（500 级别）
    //     最后一道防线，捕获所有未被匹配的异常
    //     日志级别：ERROR（严重 Bug，需开发排查）
    // ================================================================

    /**
     * 未捕获异常处理器（系统最后一道防线）。
     * <p>
     * <b>设计原则：</b>这个处理器捕获的异常理论上不应该发生。
     * 如果发生了，说明系统存在未预期的 Bug，需要开发人员介入排查。
     * <p>
     * <b>处理策略：</b>
     * <ul>
     *   <li>记录完整堆栈（ERROR 级别）</li>
     *   <li>返回统一错误码 {@code 500000}</li>
     *   <li>不向客户端暴露任何内部信息（如 SQL 错误、类名等）</li>
     * </ul>
     *
     * @param e 未捕获的异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Object> handleUnknownException(Exception e) {
        log.error("系统未知异常（请开发排查）", e);
        return ApiResult.error(ResultCode.SYSTEM_ERROR);
    }

    // ================================================================
    //  8. 工具方法
    // ================================================================

    /**
     * 组装消息。
     * <p>
     * <b>拼接规则：</b>
     * <ol>
     *   <li>若 {@code customMsg} 为 {@code null}、空字符串，或与 {@code defaultMsg} 相同，
     *       直接返回 {@code defaultMsg}</li>
     *   <li>否则返回 {@code defaultMsg + "：" + customMsg}</li>
     * </ol>
     * <p>
     * <b>设计意图：</b>避免返回的消息中出现"参数错误：参数错误"这种冗余表述。
     * <p>
     * <b>示例：</b>
     * <ul>
     *   <li>defaultMsg="参数错误"，customMsg="手机号格式错误" → "参数错误：手机号格式错误"</li>
     *   <li>defaultMsg="参数错误"，customMsg="参数错误" → "参数错误"（去重）</li>
     *   <li>defaultMsg="参数错误"，customMsg="" → "参数错误"</li>
     *   <li>defaultMsg="参数错误"，customMsg=null → "参数错误"</li>
     * </ul>
     *
     * @param defaultMsg 默认消息（来自 {@link ResultCode}）
     * @param customMsg  自定义消息（来自异常构造参数）
     * @return 组装后的完整消息
     */
    private String buildCombineMessage(String defaultMsg, String customMsg) {
        if (customMsg == null || customMsg.isBlank() || customMsg.equals(defaultMsg)) {
            return defaultMsg;
        }
        return defaultMsg + "：" + customMsg;
    }

}
