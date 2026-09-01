/**
 * 和后端 Java ResultCode 枚举完全对齐
 * 编码分段规则：
 *  200xxx  成功
 *  400xxx  参数 / 请求错误
 *  401xxx  认证 / Token / 会话
 *  403xxx  权限 / 风控账号锁定禁用
 *  404xxx  数据不存在
 *  409xxx  业务冲突
 *  422xxx  体测业务错误
 *  500xxx  系统错误
 *  600xxx  外部 / 内部接口调用
 */
export enum ResultCode {
  /* ==================== 成功 200xxx ==================== */
  SUCCESS = 200000,
  LOGOUT_SUCCESS = 200101,
  KICKOUT_SUCCESS = 200102,

  AUTH_USER_LOGIN_ERROR = 200001,

  /* ==================== 认证登录 401xxx ==================== */
  AUTH_ACCOUNT_NOT_EXIST = 401001,
  AUTH_PASSWORD_ERROR = 401002,
  AUTH_USER_NOT_LOGIN = 401003,
  AUTH_CREDENTIAL_INVALID = 401004,
  AUTH_USER_NOT_FOUND = 401005,

  /* ==================== Token模块 401101~401199 ==================== */
  TOKEN_INVALID = 401101,
  TOKEN_EXPIRED = 401102,
  TOKEN_SIGNATURE_ERROR = 401103,
  TOKEN_MALFORMED = 401104,
  TOKEN_VERSION_MISMATCH = 401105,
  TOKEN_BLACKLISTED = 401106,
  REFRESH_TOKEN_INVALID = 401111,
  REFRESH_TOKEN_EXPIRED = 401112,

  /* ==================== Session会话模块 401201~401299 ==================== */
  SESSION_NOT_FOUND = 401201,
  SESSION_ALREADY_OFFLINE = 401202,
  SESSION_TOKEN_INVALID = 401203,
  SESSION_MAX_DEVICES_EXCEEDED = 401204,
  SESSION_LOGOUT_FAILED = 401205,
  SESSION_KICKOUT_FAILED = 401206,

  /* ==================== 权限模块 403001~403099 ==================== */
  PERMISSION_DENIED = 403001,
  ROLE_NOT_ASSIGNED = 403002,
  PERMISSION_EXPIRED = 403003,

  /* ==================== 风控模块 403101~403199 ==================== */
  RISK_ACCOUNT_LOCKED = 403101,
  RISK_ACCOUNT_DISABLED = 403102,
  RISK_FAIL_THRESHOLD_EXCEEDED = 403103,
  RISK_ACCOUNT_UNLOCKED = 403104,
  RISK_CHECK_FAILED = 403105,

  /* ==================== 用户模块 404001~404099 ==================== */
  USER_NOT_FOUND = 404001,
  USER_ALREADY_EXIST = 404002,

  /* ==================== 参数错误 400xxx ==================== */
  PARAM_INVALID = 400001,
  PARAM_MISSING = 400002,
  PARAM_TYPE_ERROR = 400003,
  REQUEST_FORMAT_ERROR = 400004,

  /* ==================== 资源不存在 404xxx ==================== */
  FITNESS_DATA_NOT_FOUND = 404101,
  FILE_NOT_FOUND = 404003,
  IMPORT_TASK_NOT_FOUND = 404105,

  /* ==================== 业务冲突 409xxx ==================== */
  AUTH_ACCOUNT_ALREADY_EXIST = 409001,
  FITNESS_DATA_ALREADY_EXIST = 409101,
  TASK_CANCELLED = 409002,

  /* ==================== 体测业务 422xxx ==================== */
  FITNESS_SCORE_CALCULATE_ERROR = 422101,
  FITNESS_DATA_IMPORT_ERROR = 422102,
  FITNESS_DATA_EXPORT_ERROR = 422103,

  /* ==================== 系统错误 500xxx ==================== */
  SYSTEM_ERROR = 500000,
  DATABASE_ERROR = 500001,
  CACHE_ERROR = 500002,
  FILE_UPLOAD_ERROR = 500003,
  FILE_DOWNLOAD_ERROR = 500004,
  SERVER_TEMP_ERROR = 500005,
  LOGOUT_FAILED = 500101,
  KICKOUT_FAILED = 500102,

  /* ==================== 内外接口调用 600xxx ==================== */
  INNER_INTERFACE_ERROR = 600001,
  OUTER_INTERFACE_ERROR = 600002,
  INTERFACE_FORBIDDEN = 600003,
  INTERFACE_ADDRESS_INVALID = 600004,
  INTERFACE_TIMEOUT = 600005,
}

/**
 * 全部Token/凭证失效，需要跳转到登录页的错误码集合
 * 和后端401段认证失败编码一一对应
 */
export const TOKEN_ERROR_CODES = [
  ResultCode.AUTH_USER_NOT_LOGIN,
  ResultCode.AUTH_CREDENTIAL_INVALID,
  ResultCode.TOKEN_INVALID,
  ResultCode.TOKEN_EXPIRED,
  ResultCode.TOKEN_SIGNATURE_ERROR,
  ResultCode.TOKEN_MALFORMED,
  ResultCode.TOKEN_VERSION_MISMATCH,
  ResultCode.TOKEN_BLACKLISTED,
  ResultCode.REFRESH_TOKEN_INVALID,
  ResultCode.REFRESH_TOKEN_EXPIRED,
  ResultCode.SESSION_TOKEN_INVALID,
]
