package com.wsw.fitnesssystem.shared.interfaces.web.exception;

import com.wsw.fitnesssystem.data_exchange.error.DataExchangeErrorCode;
import com.wsw.fitnesssystem.fitness.error.FitnessErrorCode;
import com.wsw.fitnesssystem.iam.error.IamAuthNErrorCode;
import com.wsw.fitnesssystem.iam.error.IamAuthZErrorCode;
import com.wsw.fitnesssystem.iam.error.IamRiskErrorCode;
import com.wsw.fitnesssystem.iam.error.IamSessionErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.CommonErrorCode;
import com.wsw.fitnesssystem.shared.kernel.error.ErrorCode;
import com.wsw.fitnesssystem.user.error.UserErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 错误码 → HTTP 状态 映射器（接口层）。
 *
 * <p><b>核心职责：</b>把业务错误码翻译为 HTTP 状态码，供接口层构造响应时使用。
 * 是本系统中 <b>HTTP 语义的唯一入口</b>——业务代码、错误码枚举、领域层、
 * 应用层、基础设施层均不感知 HTTP，只有本类知道「哪个错误码对应哪个 HTTP 状态」。</p>
 *
 * <p><b>为什么放在接口层？</b></p>
 * <ul>
 *   <li>HTTP 是<b>传输协议</b>，属于接口层关注点；业务层不应感知</li>
 *   <li>未来若接入 gRPC / MQ / WebSocket，只需替换本类为对应协议的 Resolver，
 *       业务代码和错误码枚举零改动</li>
 *   <li>依赖方向正确：接口层 → 共享内核契约（{@link ErrorCode}），
 *       而非业务层反向依赖接口层</li>
 * </ul>
 *
 * <p><b>为什么显式注册而不是按 code 前缀推断？</b></p>
 * <ul>
 *   <li><b>前缀是隐式耦合：</b>「4 开头 → 4xx」这类约定一旦被打破（如
 *       {@code common.interface.outer_error} 映射到 502），推断逻辑就会失效</li>
 *   <li><b>同一前缀多种语义：</b>接口类错误包含 502 / 403 / 400 / 504 多个状态，
 *       无法用单一前缀规则覆盖</li>
 *   <li><b>启动期校验：</b>显式注册配合 {@link #validateAllRegistered()}，
 *       新增错误码忘记注册会导致<b>应用启动失败</b>，把运行时隐患前移到启动期</li>
 * </ul>
 *
 * <p><b>启动期校验机制（本类的关键设计）：</b></p>
 * <ol>
 *   <li>所有错误码通过 {@link #reg(ErrorCode, HttpStatus)} 显式注册</li>
 *   <li>{@link #validateAllRegistered()} 在 {@code @PostConstruct} 阶段
 *       遍历 8 个错误码枚举的所有常量</li>
 *   <li>发现任一未注册的错误码 → 抛 {@link IllegalStateException} → Spring 上下文启动失败</li>
 * </ol>
 *
 * <p><b>为什么这个机制重要：</b>错误码遗漏注册是「运行时静默失败」的典型场景
 * ——如果没有启动期校验，新增错误码忘记注册会导致该错误码永远返回 500，
 * 只有在生产环境触发时才被发现。启动期校验把这类问题转化为「无法发布」，
 * 是本系统少数几个能<b>在启动期拦截配置错误</b>的机制之一。</p>
 *
 * <p><b>被谁使用：</b></p>
 * <ul>
 *   <li>{@link GlobalExceptionHandler}：通过 {@link #resolve(ErrorCode)} 或
 *       {@link #resolveValue(ErrorCode)} 构造 {@code ResponseEntity}</li>
 *   <li>{@code SecurityResponseWriter}：Spring Security 异常响应写出时使用</li>
 * </ul>
 *
 * <p><b>维护约定：</b>新增错误码时必须在本类 {@link #init()} 中补一行
 * {@code reg(...)}，否则应用启动失败。这一约束由 {@link #validateAllRegistered()}
 * 强制保证。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 19:52
 * @since 1.0
 */
@Slf4j
@Component
public class HttpStatusResolver {

    /**
     * 错误码 → HTTP 状态 的注册表。
     *
     * <p>Key 为 {@link ErrorCode#code()}（字符串，如 {@code "iam.token.expired"}），
     * Value 为对应的 {@link HttpStatus}。</p>
     *
     * <p>本 Map 在 {@link #init()} 中一次性填充，之后只读，无需考虑并发。</p>
     */
    private final Map<String, HttpStatus> registry = new HashMap<>();

    /**
     * 初始化注册表（Spring {@code @PostConstruct} 回调）。
     *
     * <p><b>执行时机：</b>Bean 实例化完成后、应用对外提供服务之前。</p>
     *
     * <p><b>执行内容：</b></p>
     * <ol>
     *   <li>按模块分组，逐个注册所有错误码的 HTTP 状态映射</li>
     *   <li>调用 {@link #validateAllRegistered()} 做全量校验</li>
     *   <li>打印注册总数日志（可用于人工快速核对）</li>
     * </ol>
     *
     * <p><b>失败行为：</b>若任一错误码未注册，抛出 {@link IllegalStateException}，
     * Spring 上下文启动失败，应用无法启动。</p>
     */
    @PostConstruct
    public void init() {
        /* ==================== CommonErrorCode ==================== */
        reg(CommonErrorCode.SUCCESS, HttpStatus.OK);
        reg(CommonErrorCode.PARAM_INVALID, HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.PARAM_MISSING, HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.PARAM_TYPE_ERROR, HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.REQUEST_FORMAT_ERROR, HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(CommonErrorCode.FILE_UPLOAD_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.FILE_DOWNLOAD_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.FILE_GENERATE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.DATA_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(CommonErrorCode.SYSTEM_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.CACHE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.SERIALIZATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.SERVER_TEMP_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.INNER_INTERFACE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.OUTER_INTERFACE_ERROR, HttpStatus.BAD_GATEWAY);
        reg(CommonErrorCode.INTERFACE_FORBIDDEN, HttpStatus.FORBIDDEN);
        reg(CommonErrorCode.INTERFACE_ADDRESS_INVALID, HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.INTERFACE_TIMEOUT, HttpStatus.GATEWAY_TIMEOUT);

        /* ==================== IamAuthNErrorCode ==================== */
        reg(IamAuthNErrorCode.ACCOUNT_NOT_EXIST, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.PASSWORD_ERROR, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_NOT_LOGIN, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.CREDENTIAL_INVALID, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.CREDENTIAL_EXPIRED, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_LOGIN_ERROR, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.ACCOUNT_ALREADY_EXIST, HttpStatus.CONFLICT);
        reg(IamAuthNErrorCode.KICK_TARGET_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(IamAuthNErrorCode.LOGOUT_SUCCESS, HttpStatus.OK);
        reg(IamAuthNErrorCode.KICKOUT_SUCCESS, HttpStatus.OK);
        reg(IamAuthNErrorCode.ACCOUNT_UNLOCKED, HttpStatus.OK);
        reg(IamAuthNErrorCode.LOGOUT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(IamAuthNErrorCode.KICKOUT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        reg(IamAuthNErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_SIGNATURE_ERROR, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_MALFORMED, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_VERSION_MISMATCH, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_BLACKLISTED, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.REFRESH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.REFRESH_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);

        /* ==================== IamSessionErrorCode ==================== */
        reg(IamSessionErrorCode.SESSION_ALREADY_OFFLINE, HttpStatus.BAD_REQUEST);
        reg(IamSessionErrorCode.SESSION_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(IamSessionErrorCode.SESSION_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        reg(IamSessionErrorCode.SESSION_MAX_DEVICES_EXCEEDED, HttpStatus.FORBIDDEN);

        /* ==================== IamAuthZErrorCode ==================== */
        reg(IamAuthZErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.ROLE_NOT_ASSIGNED, HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.PERMISSION_EXPIRED, HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.ROLE_CODE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.ROLE_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.PERM_CODE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.PERM_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);

        /* ==================== IamRiskErrorCode ==================== */
        reg(IamRiskErrorCode.ACCOUNT_LOCKED, HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.FAIL_THRESHOLD_EXCEEDED, HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.CHECK_FAILED, HttpStatus.FORBIDDEN);

        /* ==================== UserErrorCode ==================== */
        reg(UserErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(UserErrorCode.USER_ALREADY_EXIST, HttpStatus.CONFLICT);
        reg(UserErrorCode.ACCOUNT_NOT_EXIST, HttpStatus.NOT_FOUND);
        reg(UserErrorCode.PHONE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(UserErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(UserErrorCode.USERNAME_ALREADY_EXISTS, HttpStatus.CONFLICT);

        /* ==================== DataExchangeErrorCode ==================== */
        reg(DataExchangeErrorCode.IMPORT_TASK_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(DataExchangeErrorCode.TASK_CANCELLED, HttpStatus.CONFLICT);

        /* ==================== FitnessErrorCode ==================== */
        reg(FitnessErrorCode.DATA_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(FitnessErrorCode.DATA_ALREADY_EXIST, HttpStatus.CONFLICT);
        reg(FitnessErrorCode.SCORE_CALCULATE_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        reg(FitnessErrorCode.DATA_IMPORT_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        reg(FitnessErrorCode.DATA_EXPORT_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);

        // 启动期校验：所有枚举的所有值必须已注册
        validateAllRegistered();

        log.info("[HTTP] HttpStatusResolver registered {} error codes", registry.size());
    }

    /**
     * 注册单个错误码的 HTTP 状态映射。
     *
     * <p><b>幂等性：</b>同一 {@code code} 重复注册会覆盖（后注册生效）。
     * 但由于错误码本身全局唯一，正常使用不会出现重复。</p>
     *
     * @param ec     错误码契约（由各模块枚举常量传入）
     * @param status 对应的 HTTP 状态
     */
    private void reg(ErrorCode ec, HttpStatus status) {
        registry.put(ec.code(), status);
    }

    /**
     * 启动期全量校验：确保所有错误码枚举的每个常量都已注册。
     *
     * <p><b>校验范围：</b>硬编码列出项目内所有 {@link ErrorCode} 枚举类，
     * 遍历其 {@code values()} 逐个检查是否已在 {@link #registry} 中。</p>
     *
     * <p><b>失败行为：</b>发现未注册的错误码时，抛出 {@link IllegalStateException}，
     * 异常信息中包含 <b>枚举类名 + 常量名 + code 值</b>，可直接定位遗漏项。</p>
     *
     * <p><b>为什么不用反射扫描？</b></p>
     * <ul>
     *   <li>反射扫描需要依赖类路径扫描工具（如 Reflections、ClassPathScanningCandidateComponentProvider），
     *       引入额外依赖且启动开销更大</li>
     *   <li>硬编码列表的维护成本极低（新增模块时加一行类字面量即可）</li>
     *   <li><b>关键收益：</b>硬编码列表本身也是「新增模块时的 checklist」——
     *       忘记在此处添加新枚举类，会导致新模块的错误码校验被跳过，
     *       但通过 CR / 代码 review 很容易发现</li>
     * </ul>
     *
     * @throws IllegalStateException 存在未注册的错误码时抛出
     */
    private void validateAllRegistered() {
        Class<?>[] groups = {
            CommonErrorCode.class,
            IamAuthNErrorCode.class,
            IamSessionErrorCode.class,
            IamAuthZErrorCode.class,
            IamRiskErrorCode.class,
            UserErrorCode.class,
            DataExchangeErrorCode.class,
            FitnessErrorCode.class,
        };
        for (Class<?> group : groups) {
            if (!group.isEnum()) continue;
            for (Object constant : group.getEnumConstants()) {
                ErrorCode ec = (ErrorCode) constant;
                if (!registry.containsKey(ec.code())) {
                    throw new IllegalStateException(
                        "错误码未注册 HTTP 状态："
                            + group.getSimpleName() + "." + ((Enum<?>) constant).name()
                            + " (code=" + ec.code() + ")"
                    );
                }
            }
        }
    }

    /**
     * 解析错误码对应的 HTTP 状态。
     *
     * <p><b>使用场景：</b>需要 {@link HttpStatus} 对象时使用（如构造
     * {@code ResponseEntity.status(status)}）。</p>
     *
     * <p><b>兜底策略：</b>如果错误码未在注册表中（理论上不可能，因为启动期已校验），
     * 记录 ERROR 日志并返回 {@link HttpStatus#INTERNAL_SERVER_ERROR}，
     * 保证调用方永远拿到非 null 的状态。</p>
     *
     * @param ec 错误码（不可为 null）
     * @return 对应的 HTTP 状态（永远非 null）
     */
    public HttpStatus resolve(ErrorCode ec) {
        HttpStatus status = registry.get(ec.code());
        if (status == null) {
            log.error("[HTTP] Unregistered error code: {}, fallback to 500", ec.code());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return status;
    }

    /**
     * 解析错误码对应的 HTTP 状态码（int 形式）。
     *
     * <p><b>使用场景：</b>需要 int 值写入响应体字段（如 {@code ApiResponse.httpCode}）
     * 或日志时使用。</p>
     *
     * <p><b>与 {@link #resolve(ErrorCode)} 的关系：</b>本方法是
     * {@code resolve(ec).value()} 的便捷封装，避免调用方重复写 {@code .value()}。</p>
     *
     * @param ec 错误码（不可为 null）
     * @return HTTP 状态码的 int 值
     */
    public int resolveValue(ErrorCode ec) {
        return resolve(ec).value();
    }

}
