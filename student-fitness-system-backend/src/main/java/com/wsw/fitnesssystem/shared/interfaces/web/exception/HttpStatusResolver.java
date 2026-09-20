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
 * ErrorCode → HttpStatus 映射器（接口层）。
 *
 * <p><b>职责：</b>把业务错误码翻译为 HTTP 状态码，供接口层构造响应时使用。
 * 是 HTTP 语义的唯一入口，业务代码不感知 HTTP。</p>
 *
 * <p><b>为什么显式注册而不是按 code 前缀推断？</b></p>
 * <ul>
 *   <li>前缀约定是隐式耦合，重构时容易漏改</li>
 *   <li>600xxx 段存在多个 HTTP 语义（502/403/400/504），前缀无法覆盖</li>
 *   <li>显式注册 + 启动期校验，新增错误码忘记注册 → 应用启动失败</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/20 19:52
 * @since 1.0
 */
@Slf4j
@Component
public class HttpStatusResolver {

    private final Map<String, HttpStatus> registry = new HashMap<>();

    @PostConstruct
    public void init() {
        /* ==================== CommonErrorCode ==================== */
        reg(CommonErrorCode.SUCCESS,                    HttpStatus.OK);
        reg(CommonErrorCode.PARAM_INVALID,              HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.PARAM_MISSING,              HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.PARAM_TYPE_ERROR,           HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.REQUEST_FORMAT_ERROR,       HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.FILE_NOT_FOUND,             HttpStatus.NOT_FOUND);
        reg(CommonErrorCode.FILE_UPLOAD_ERROR,          HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.FILE_DOWNLOAD_ERROR,        HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.FILE_GENERATE_ERROR,        HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.DATA_ALREADY_EXISTS,        HttpStatus.CONFLICT);
        reg(CommonErrorCode.SYSTEM_ERROR,               HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.DATABASE_ERROR,             HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.CACHE_ERROR,                HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.SERVER_TEMP_ERROR,          HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.INNER_INTERFACE_ERROR,      HttpStatus.INTERNAL_SERVER_ERROR);
        reg(CommonErrorCode.OUTER_INTERFACE_ERROR,      HttpStatus.BAD_GATEWAY);
        reg(CommonErrorCode.INTERFACE_FORBIDDEN,        HttpStatus.FORBIDDEN);
        reg(CommonErrorCode.INTERFACE_ADDRESS_INVALID,  HttpStatus.BAD_REQUEST);
        reg(CommonErrorCode.INTERFACE_TIMEOUT,          HttpStatus.GATEWAY_TIMEOUT);

        /* ==================== IamAuthNErrorCode ==================== */
        reg(IamAuthNErrorCode.ACCOUNT_NOT_EXIST,        HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.PASSWORD_ERROR,           HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_NOT_LOGIN,           HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.CREDENTIAL_INVALID,       HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_NOT_FOUND,           HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.CREDENTIAL_EXPIRED,       HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.USER_LOGIN_ERROR,         HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.ACCOUNT_ALREADY_EXIST,    HttpStatus.CONFLICT);
        reg(IamAuthNErrorCode.LOGOUT_SUCCESS,           HttpStatus.OK);
        reg(IamAuthNErrorCode.KICKOUT_SUCCESS,          HttpStatus.OK);
        reg(IamAuthNErrorCode.ACCOUNT_UNLOCKED,         HttpStatus.OK);
        reg(IamAuthNErrorCode.LOGOUT_FAILED,            HttpStatus.INTERNAL_SERVER_ERROR);
        reg(IamAuthNErrorCode.KICKOUT_FAILED,           HttpStatus.INTERNAL_SERVER_ERROR);
        reg(IamAuthNErrorCode.TOKEN_INVALID,            HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_EXPIRED,            HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_SIGNATURE_ERROR,    HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_MALFORMED,          HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_VERSION_MISMATCH,   HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.TOKEN_BLACKLISTED,        HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.REFRESH_TOKEN_INVALID,    HttpStatus.UNAUTHORIZED);
        reg(IamAuthNErrorCode.REFRESH_TOKEN_EXPIRED,    HttpStatus.UNAUTHORIZED);

        /* ==================== IamSessionErrorCode ==================== */
        reg(IamSessionErrorCode.SESSION_ALREADY_OFFLINE,      HttpStatus.BAD_REQUEST);
        reg(IamSessionErrorCode.SESSION_NOT_FOUND,            HttpStatus.NOT_FOUND);
        reg(IamSessionErrorCode.SESSION_TOKEN_INVALID,        HttpStatus.UNAUTHORIZED);
        reg(IamSessionErrorCode.SESSION_MAX_DEVICES_EXCEEDED, HttpStatus.FORBIDDEN);

        /* ==================== IamAuthZErrorCode ==================== */
        reg(IamAuthZErrorCode.PERMISSION_DENIED,        HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.ROLE_NOT_ASSIGNED,        HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.PERMISSION_EXPIRED,       HttpStatus.FORBIDDEN);
        reg(IamAuthZErrorCode.ROLE_CODE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.ROLE_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.PERM_CODE_ALREADY_EXISTS, HttpStatus.CONFLICT);
        reg(IamAuthZErrorCode.PERM_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);

        /* ==================== IamRiskErrorCode ==================== */
        reg(IamRiskErrorCode.ACCOUNT_LOCKED,           HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.ACCOUNT_DISABLED,         HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.FAIL_THRESHOLD_EXCEEDED,  HttpStatus.FORBIDDEN);
        reg(IamRiskErrorCode.CHECK_FAILED,             HttpStatus.FORBIDDEN);

        /* ==================== UserErrorCode ==================== */
        reg(UserErrorCode.USER_NOT_FOUND,           HttpStatus.NOT_FOUND);
        reg(UserErrorCode.USER_ALREADY_EXIST,       HttpStatus.CONFLICT);
        reg(UserErrorCode.ACCOUNT_NOT_EXIST,        HttpStatus.NOT_FOUND);
        reg(UserErrorCode.PHONE_ALREADY_EXISTS,     HttpStatus.CONFLICT);
        reg(UserErrorCode.EMAIL_ALREADY_EXISTS,     HttpStatus.CONFLICT);
        reg(UserErrorCode.USERNAME_ALREADY_EXISTS,  HttpStatus.CONFLICT);

        /* ==================== DataExchangeErrorCode ==================== */
        reg(DataExchangeErrorCode.IMPORT_TASK_NOT_FOUND, HttpStatus.NOT_FOUND);
        reg(DataExchangeErrorCode.TASK_CANCELLED,        HttpStatus.CONFLICT);

        /* ==================== FitnessErrorCode ==================== */
        reg(FitnessErrorCode.DATA_NOT_FOUND,           HttpStatus.NOT_FOUND);
        reg(FitnessErrorCode.DATA_ALREADY_EXIST,       HttpStatus.CONFLICT);
        reg(FitnessErrorCode.SCORE_CALCULATE_ERROR,    HttpStatus.UNPROCESSABLE_ENTITY);
        reg(FitnessErrorCode.DATA_IMPORT_ERROR,        HttpStatus.UNPROCESSABLE_ENTITY);
        reg(FitnessErrorCode.DATA_EXPORT_ERROR,        HttpStatus.UNPROCESSABLE_ENTITY);

        // 启动期校验：所有枚举的所有值必须已注册
        validateAllRegistered();

        log.info("[HTTP] HttpStatusResolver registered {} error codes", registry.size());
    }

    private void reg(ErrorCode ec, HttpStatus status) {
        registry.put(ec.code(), status);
    }

    /**
     * 启动期校验：遍历所有 ErrorCode 枚举，确保每个常量都已注册。
     * <p>新增错误码忘记注册 → 应用启动失败，把运行时隐患前移到启动期。</p>
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
     * @param ec 错误码
     * @return HTTP 状态（永远非 null）
     */
    public HttpStatus resolve(ErrorCode ec) {
        HttpStatus status = registry.get(ec.code());
        if (status == null) {
            log.error("[HTTP] 未注册的错误码：{}，兜底 500", ec.code());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return status;
    }

    /** 便捷方法：直接返回 int 形式的 HTTP 状态码 */
    public int resolveValue(ErrorCode ec) {
        return resolve(ec).value();
    }

}
