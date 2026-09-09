# Data Exchange 模块架构规范

# Data Exchange 模块架构规范

> 本文档面向 AI 开发助手，用于指导后续功能开发。遵循本规范可确保代码风格统一、架构清晰、易于维护和扩展。
> 
> 

## 一、模块定位

**data\-exchange** 是系统的**导入导出中台**，职责包括：

- 文件解析（Excel / CSV / 后续扩展）

- 导入任务调度与进度追踪

- 错误收集与错误文件生成

- 导出功能（预留）

**核心原则**：与具体业务解耦，业务通过插件（Plugin）接入。

## 二、架构分层（六边形 \+ DDD）

### 2\.1 分层概览

|层级|包路径|职责|依赖方向|
|---|---|---|---|
|**接口层**|`interfaces`|HTTP 请求处理、参数校验、响应封装|→ 应用层|
|**应用层**|`application`|用例编排、端口定义、业务插件|→ 领域层|
|**领域层**|`domain`|聚合根、值对象、仓储接口、领域异常|无外部依赖|
|**基础设施层**|`infrastructure`|技术实现（Redis、MySQL、FileParser）|→ 应用层 \+ 领域层|

### 2\.2 应用层子包规范

```Plain Text
application/
├── service/          # 应用服务（用例实现）
│   ├── command/      # 写操作服务
│   └── query/        # 读操作服务
├── plugin/           # 业务插件接口与注册表
├── port/             # 端口定义
│   └── output/       # 输出端口（由基础设施实现）
├── scheduler/        # 异步调度器
├── collector/        # 错误收集器
├── generator/        # 文件生成器
└── enums/            # 应用层枚举
```

### 2\.3 各层职责

|层级|可以做什么|不可以做什么|
|---|---|---|
|**接口层**|参数校验、格式转换、调用应用服务|包含业务逻辑、直接操作数据库/Redis|
|**应用层**|编排用例、调用领域对象和端口|包含领域规则、直接操作数据库/Redis|
|**领域层**|封装业务规则、状态流转、领域异常|依赖外部框架（Spring/Redis/MySQL）|
|**基础设施层**|实现端口、技术细节封装|包含业务逻辑|

## 三、类命名规范

### 3\.1 核心类命名规则

|类型|命名规则|示例|
|---|---|---|
|**聚合根**|业务名词|`ImportTask`|
|**值对象**|业务名词|`ImportTemplate`|
|**仓储接口**|聚合根名 \+ `Repository`|`ImportTaskRepository`|
|**应用服务（写）**|业务动词 \+ `Service`|`ImportSubmissionService`|
|**应用服务（读）**|业务动词 \+ `QueryService`|`ImportTaskQueryService`|
|**输出端口**|能力名词 \+ `Port`|`RateLimiterPort`、`DistributedLockPort`|
|**端口适配器**|技术前缀 \+ 端口名 \+ `Adapter`|`RedisRateLimiterAdapter`|
|**业务插件**|业务名 \+ `Plugin`|`ImportPlugin`、`UserImportPlugin`|
|**编排器**|业务名 \+ `Orchestrator`|`ImportOrchestrator`|
|**异步调度器**|业务名 \+ `Scheduler`|`AsyncImportScheduler`|
|**DTO（请求）**|业务名 \+ `Request`|`ImportSubmissionRequest`|
|**DTO（响应）**|业务名 \+ `Response`|`ImportProgressResponse`|
|**DTO（应用层内部）**|业务名 \+ `Result`|`ImportProgressResult`|

### 3\.2 错误示范

|❌ 错误|✅ 正确|原因|
|---|---|---|
|`ExcelConstants`|`ImportConfig`|不要用技术名词做类名|
|`UserImportAdapter`|`UserImportPlugin`|技术适配器叫 Adapter，业务扩展叫 Plugin|
|`ExcelImportTemplate`|`ImportOrchestrator`|编排器不是模板类|
|`getImportPlugin()`|`getPlugin()`|类名已表达上下文，方法无需重复|

## 四、DTO 规范

### 4\.1 分层 DTO 类型

|层级|入参|出参|示例|
|---|---|---|---|
|**接口层（Controller）**|`XxxRequest`|`XxxResponse`|`ImportSubmissionRequest` → `ApiResult<ImportProgressResponse>`|
|**应用层（Service）**|`XxxCommand` / `XxxQuery`|`XxxResult`|`ImportSubmissionCommand` → `ImportProgressResult`|
|**领域层**|领域对象|领域对象|`ImportTask` → `ImportTask`|

### 4\.2 接口层 DTO

```Java
// Request：前端入参（参数 > 2 时抽取）
@Getter
@Setter
public class ImportSubmissionRequest {
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}

// Response：返回前端（配合 ApiResult<T>）
@Getter
@Builder
public class ImportProgressResponse {
    private int total;
    private int processed;
    @JsonProperty("success_count")  // 前端蛇形命名
    private int successCount;
    // ...
}
```

### 4\.3 应用层 DTO

```Java
// Result：应用层内部返回
@Getter
@Builder
public class ImportProgressResult {
    private int total;
    private int processed;
    private ImportStatus status;
    // 业务计算方法放在 Result 中
    public int getPercent() { ... }
}
```

### 4\.4 统一响应格式

```Java
// 所有接口返回 ApiResult<T>
ApiResult.success(data);   // 成功
ApiResult.error(ResultCode.PARAM_INVALID, "业务类型不能为空");  // 失败
```

## 五、异常处理规范

### 5\.1 异常层次

```Plain Text
BaseException（抽象）
    ├── BizException（业务异常，400/401/403/409）
    └── SystemException（系统异常，500）
```

### 5\.2 日志与异常语言规范

|类型|语言|示例|
|---|---|---|
|`log.info/warn/error`|**英文**|`log.info("Task submitted, taskId={}", taskId);`|
|`throw new BizException`|**中文**|`throw new BizException(PARAM_INVALID, "业务类型不能为空");`|

### 5\.3 日志级别

|级别|使用场景|
|---|---|
|**ERROR**|系统故障，需人工介入（数据库异常、IO异常）|
|**WARN**|异常但可自动恢复（重复提交、限流触发）|
|**INFO**|关键业务流程节点（任务提交、任务完成）|
|**DEBUG**|正常流程的中间步骤（文件转存成功、锁释放）|

### 5\.4 全局异常处理器覆盖

```Java
// 必须覆盖的核心异常
BizException.class              // 业务异常
ConstraintViolationException.class  // @Validated + @RequestParam
MethodArgumentNotValidException.class  // @Valid + @RequestBody
Exception.class                  // 兜底
```

## 六、存放位置规范

### 6\.1 包路径映射

|类职责|包路径|
|---|---|
|Controller|`interfaces`|
|应用服务（读）|`application.service.query`|
|应用服务（写）|`application.service.command`|
|端口接口|`application.port.output`|
|端口适配器|`infrastructure.cache`（Redis）或 `infrastructure.persistence`（MySQL）|
|业务插件|`application.plugin`|
|聚合根/值对象|`domain.model` 或 `domain.vo`|
|仓储接口|`domain.repository`|
|领域异常|`domain.exception`|
|枚举|`domain.enums`（领域）或 `application.enums`（应用）|
|基础设施异常|`infrastructure.exception`|
|工具类|`infrastructure.util`|

### 6\.2 核心规则

|技术栈|存放位置|
|---|---|
|Redis 所有实现|`infrastructure.cache`|
|MySQL 所有实现|`infrastructure.persistence`|
|文件解析|`infrastructure.parser`|

### 6\.3 特殊放置规则

|内容|位置|理由|
|---|---|---|
|`ImportBizType` 枚举|`application.enums`|业务类型是“插件查找的索引”，属于应用层配置|
|`ImportStatus` 枚举|`domain.enums`|任务状态是聚合根的核心属性，属于领域层|

## 七、防腐层（ACL）规范

### 7\.1 模块间依赖方向

```Plain Text
data_exchange（本模块）→ user（外部模块）  ✅ 单向依赖；
业务模块（user / fitness 等）可以依赖 data_exchange 模块的 Port 接口和 DTO，这是依赖反转的正常体现；
业务模块禁止直接依赖 data_exchange 模块的 domain 层或 infrastructure 层；
data_exchange 模块不依赖任何具体业务模块；
user（外部模块）→ data_exchange  ❌ 禁止反向依赖；
```

### 7\.2 跨模块调用方式

|场景|实现方式|示例|
|---|---|---|
|本地调用|本模块定义 Port 接口，外部模块实现该接口（Adapter），完成本地方法调用。|示例：`UserProvisioningPort` 由 data\_exchange 定义，user 模块的 `LocalUserProvisioningAdapter` 实现。|
|微服务拆分（未来）|本模块定义 Port 接口，本模块内部实现 Feign Adapter，通过 HTTP/RPC 远程调用外部模块的 API。外部模块只需暴露 REST API，不需要知道本模块的 Port。|示例：data\_exchange 模块的 `UserProvisioningFeignAdapter` 通过 FeignClient 远程调用 user 服务的 REST API。|

### 7\.3 端口实现类命名

|端口接口|本地实现|远程实现（预留）|
|---|---|---|
|`UserProvisioningPort`|`UserProvisioningLocalAdapter`|`UserProvisioningFeignAdapter`|
|`DistributedLockPort`|`RedisDistributedLockAdapter`|`ZookeeperDistributedLockAdapter`|

### 7\.4 允许的依赖范围

业务模块（如 user）依赖 data\_exchange 时，**仅限以下内容**：

|允许依赖|示例|
|---|---|
|`application.port.output.*`|Port 接口|
|`application.dto.command.*`|跨模块入参 DTO|
|`application.dto.result.*`|跨模块出参 DTO|

**禁止依赖**：

|禁止依赖|示例|
|---|---|
|`domain.model.*`|聚合根|
|`domain.repository.*`|仓储接口|
|`infrastructure.*`|基础设施实现|

## 八、API 路径规范

### 8\.1 RESTful 路径设计

```Plain Text
POST   /import/submit         # 提交导入
GET    /import/progress       # 查询进度
GET    /import/types          # 业务类型列表
GET    /import/error/download # 错误文件下载
POST   /import/cancel         # 取消任务
GET    /import/template       # 模板下载
```

### 8\.2 路径命名原则

- 模块名用 `data_exchange` 而非 `excel`（通用性）

- 用动词表达操作（submit、cancel、download）

- 资源用复数（types、errors）

## 九、配置与常量规范

### 9\.1 常量类命名

|❌ 旧命名|✅ 新命名|说明|
|---|---|---|
|`ExcelConstants`|`ImportConfig`|配置类，不是常量工具类|
|`ExcelRedisKeys`|`ImportRedisKeys`|Redis Key 规范|

### 9\.2 Redis Key 规范

```Plain Text
import:{模块}:{维度}:{标识}

示例：
  import:task:{taskId}          → 任务进度
  import:lock:file:{md5}        → 文件防重锁
  import:limit:user:{userId}    → 用户限流
```

### 9\.3 外部化配置规范

#### 9\.3\.1 核心原则

|原则|说明|
|---|---|
|分层配置|应用层配置与基础设施层配置分离（各自独立 Properties 类）|
|外部化|所有可调参数从 YAML 注入，代码中无硬编码|
|类型安全|使用 @ConfigurationProperties 绑定|
|默认值兜底|Properties 类中设默认值，YAML 未配置时使用默认值|

#### 9\.3\.2 配置归属

|配置类型|归属|示例|
|---|---|---|
|业务策略（批量大小、校验规则、展示策略）|应用层|batch\.default\-size、validation\.username\-max\-length|
|技术参数（文件限制、Redis TTL、限流窗口）|基础设施层|file\.max\-size、redis\.task\-ttl\-hours|

#### 9\.3\.3 YAML 结构

```YAML
data-exchange:
  application:
    file:
      max-size: 200MB
      allowed-extensions: [.xlsx, .xls]
    batch:
      default-size: 500
      stream-threshold: 10000
    validation:
      username-max-length: 50
      password-min-length: 6
    display:
      error-msg-max-count: 10
  infrastructure:
    temp-file:
      root-dir: import
      file-name: data.xlsx
    redis:
      task-ttl-hours: 24
      lock-ttl-minutes: 60
      error-msg-max-length: 500
    rate-limit:
      window-seconds: 60
      max-count: 5
```

#### 9\.3\.4 配置类结构

```Java
// 应用层：application/config/ImportApplicationProperties.java
@ConfigurationProperties(prefix = "data-exchange.application")
public class ImportApplicationProperties {
    private FileConfig file = new FileConfig();
    private BatchConfig batch = new BatchConfig();
    // ... 内部类略
}

// 基础设施层：infrastructure/config/ImportInfrastructureProperties.java
@ConfigurationProperties(prefix = "data-exchange.infrastructure")
public class ImportInfrastructureProperties {
    private TempFileConfig tempFile = new TempFileConfig();
    private RedisConfig redis = new RedisConfig();
    // ... 内部类略
}
```

## 十、Git Commit 规范

```Plain Text
type(scope): subject

type:
  feat    新功能
  fix     修复
  refactor 重构（不改变功能）
  docs    文档
  test    测试
  chore   构建/工具

示例：
  refactor(import): 优化导入编排器参数命名与注释
  feat(export): 新增导出任务创建接口
  fix(import): 修复空文件处理时状态未正确标记
```

## 十一、代码示例集

### 11\.1 Controller \+ Request/Response

```Java
@Validated
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportSubmissionService submissionService;
    private final ImportTaskQueryService queryService;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResult<String> submitImport(@Valid ImportSubmissionRequest request) {
        String taskId = submissionService.submit(
            ImportBizType.fromCode(request.getBizType()),
            request.getFile(),
            RequestContextHolder.getRequiredOperator().userId()
        );
        return ApiResult.success(taskId);
    }

    @GetMapping("/progress")
    public ApiResult<ImportProgressResponse> getProgress(@RequestParam String taskId) {
        ImportProgressResult result = queryService.getProgressOrThrow(taskId);
        return ApiResult.success(buildResponse(result));
    }
}
```

### 11\.2 应用服务 \+ Command

```Java
@Service
@RequiredArgsConstructor
public class ImportTaskCommandService {

    private final ImportTaskRepository repository;

    public void cancelTask(String taskId) {
        ImportTask task = repository.findById(taskId)
            .orElseThrow(() -> new BizException(IMPORT_TASK_NOT_FOUND, "任务不存在"));
        if (!task.isRunning()) {
            throw new BizException(PARAM_INVALID, "任务未在运行中");
        }
        repository.requestCancel(taskId);
        log.info("Cancellation requested: taskId={}", taskId);
    }
}
```

### 11\.3 领域聚合根

```Java
@Getter
public class ImportTask {
    private final String taskId;
    private ImportStatus status;
    private int total;
    private int successCount;
    private int failCount;
    private List<String> errorSummary;

    public ImportTask(String taskId) {
        this.taskId = taskId;
        this.status = ImportStatus.INIT;
        this.errorSummary = new ArrayList<>();
    }

    public void start(int total) {
        if (this.status != ImportStatus.INIT) {
            throw new IllegalStateException("Task already started");
        }
        this.total = total;
        this.status = ImportStatus.PROCESSING;
    }

    public void recordBatch(int success, int fail, List<String> errors) {
        this.successCount += success;
        this.failCount += fail;
        if (errors != null) {
            this.errorSummary.addAll(errors);
        }
    }

    public void finishSuccess() {
        this.status = ImportStatus.FINISHED;
    }

    // 仅 Repository 层可调用（重建聚合根用）
    protected ImportTask(String taskId, ImportStatus status, int total, int processed,
                         int successCount, int failCount, List<String> errorSummary) {
        // ...
    }
}
```

### 11\.4 输出端口 \+ 适配器

```Java
// 端口接口（application.port.output）
public interface RateLimiterPort {
    void checkRateLimit(Long userId);
}

// 适配器实现（infrastructure.cache）
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiterAdapter implements RateLimiterPort {

    private final StringRedisTemplate redis;

    @Override
    public void checkRateLimit(Long userId) {
        // Redis 限流实现
        String key = ImportRedisKeys.rateLimitKey(userId);
        Long current = redis.opsForValue().increment(key);
        if (current == 1) {
            redis.expire(key, Duration.ofSeconds(60));
        }
        if (current > 5) {
            log.warn("Rate limit exceeded: userId={}, current={}", userId, current);
            throw new BizException(PARAM_INVALID, "请求过于频繁，请等待60秒后再试");
        }
    }
}
```

---

**版本**：1\.0  

**更新日期**：2026\-09\-09  

**维护者**：架构组

