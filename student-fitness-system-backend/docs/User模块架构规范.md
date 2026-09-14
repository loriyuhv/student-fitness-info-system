# User 模块架构规范

> 本文档面向 AI 开发助手，用于指导 `user` 模块后续功能开发。遵循本规范可确保代码风格统一、架构清晰、易于维护和扩展。
> 
> 

---

## 一、模块定位

**user** 是系统的**用户与组织信息模块**，职责包括：

- 用户聚合根的建模与状态流转（启用、禁用、资料更新、逻辑删除/恢复）

- 用户档案维护：通用档案（user\_profile）、学生档案（student\_profile）、教师档案（teacher\_profile）

- 用户注册与批量导入落库（作为 data\_exchange 导入中台的下游写入方）

- 教师\-班级关系查询（作为数据权限过滤的数据来源）

- 当前登录用户信息查询（对外 REST 接口）

- 向 authentication 提供用户凭证数据（通过 `UserCredentialQueryPort`）

- 向 authorization 提供授权查询契约定义（`UserAuthorizationQueryPort`）

**核心原则**：

- **认证与画像分离**：`sys_user` 负责认证（账号、密码哈希、状态），档案表负责画像、学籍与教师档案信息。

- **对外通过端口与 DTO 解耦**：

    - 本模块**实现**外部模块定义的端口：`data_exchange.UserProvisioningPort`、`authentication.UserCredentialQueryPort`、`shared.TeacherClassQueryPort`

    - 本模块**定义**供外部实现的端口：`UserAuthorizationQueryPort`（由 authorization 实现）

- **领域规则收敛在领域模型**：状态与资料变更规则由聚合根自身方法承载，应用层只做编排。

### 1\.1 数据模型（表级）

|表|归属迁移脚本|说明|
|---|---|---|
|`sys_user`|`resources/db/migration/common/V1.0.01__init_auth_schema.sql`|登录账号、密码哈希、昵称、联系方式、用户类型、来源、状态、逻辑删除；本模块通过 `UserPo` 读写|
|`user_profile`|`resources/db/migration/user_org/V1.0.03__init_user_schema.sql`|用户通用档案，与 `sys_user` 1:1（`uk_user_id`）|
|`class_info`|同上|班级字典：`class_code` 业务唯一、`grade` 年级、`status` 启用状态|
|`student_profile`|同上|学生学籍档案（仅 `user_type=2` 有记录）：`uk_user_deleted`、`uk_student_no_deleted`、`uk_id_card_deleted`|
|`teacher_profile`|同上|教师档案（仅 `user_type=1` 有记录）：`uk_user_deleted`、`uk_teacher_no_deleted`|
|`teacher_class`|同上|教师\-班级多对多关系：`uk_teacher_class(teacher_id, class_id, deleted)`；用于 data\_scope=本班 的过滤|

初始化数据脚本：`resources/db/migration/user_org/V1.0.04__init_user_data.sql`、`resources/db/migration/user_org/V1.0.04__init_user_data_test.sql`。

---

## 二、架构分层（六边形 \+ DDD）

### 2\.1 分层概览

|层级|包路径|职责|依赖方向|
|---|---|---|---|
|**接口层**|`interfaces.web`|HTTP 请求处理、响应封装|→ 应用层|
|**应用层**|`application`|用例编排、端口定义、DTO|→ 领域层|
|**领域层**|`domain`|聚合根、档案模型、值对象、仓储接口、领域能力端口|无外部依赖|
|**基础设施层**|`infrastructure`|持久化、端口适配器、加密实现|→ 应用层 \+ 领域层|

### 2\.2 应用层子包规范

```Plain Text
application/
├── service/          # 应用服务（用例实现）
│   ├── command/      # 写操作服务
│   └── query/        # 读操作服务
├── port/             # 端口定义
│   └── output/       # 输出端口（由基础设施实现）
└── dto/
    └── result/       # 应用层出参 + 跨模块数据契约
```

**说明**：

- `application/port/output` 存放**本模块定义、供外部模块实现**的端口（如 `UserAuthorizationQueryPort`）

- `application/dto/result` 存放应用层出参 DTO，同时也承载**跨模块数据契约**（如 `UserAuthorizationResult`）

### 2\.3 各层职责

|层级|可以做什么|不可以做什么|
|---|---|---|
|**接口层**|取当前操作人、调用应用服务、Result→Response 转换、返回统一响应|包含业务逻辑、直接访问 Repository/Mapper|
|**应用层**|编排用例、调用领域模型与端口、声明事务边界|包含领域规则、直接访问 Mapper|
|**领域层**|封装业务规则（`User.enable/disable/updatePhoneNumber/updateNickname/softDelete/restore`）、状态流转、领域异常|依赖 Spring / MyBatis\-Plus / Redis / MySQL / JSON 序列化|
|**基础设施层**|实现端口、PO↔领域模型转换、SQL 与加密等技术细节|包含业务规则|

---

## 三、类命名规范

### 3\.1 核心类命名规则

|类型|命名规则|本模块示例|
|---|---|---|
|**聚合根**|业务名词|`User`、`UserProfile`、`StudentProfile`、`TeacherProfile`|
|**值对象**|业务名词|`Gender`、`Status`、`UserType`、`UserSource`|
|**仓储接口**|模型名 \+ `Repository`|`UserRepository`、`UserProfileRepository`、`StudentProfileRepository`、`TeacherProfileRepository`、`TeacherClassRepository`|
|**应用服务（写）**|业务动词 \+ `Service`|`UserRegisterService`（接口）、`UserRegistrationService`（编排器）|
|**应用服务（读）**|业务动词 \+ `QueryService`|`UserInfoQueryService`|
|**应用层端口**|能力名词 \+ `Port` / `QueryPort`|`UserAuthorizationQueryPort`|
|**领域能力端口**|能力名词 \+ `Port`|`PasswordEncryptorPort`|
|**端口适配器**|端口名（去 Port）\+ `LocalAdapter`|`UserProvisioningLocalAdapter`、`UserCredentialQueryLocalAdapter`、`TeacherClassQueryAdapter`、`PasswordEncryptorLocalAdapter`|
|**端口适配器（外部实现）**|端口名（去 Port）\+ `LocalAdapter`|`UserAuthorizationQueryLocalAdapter`（位于 authorization 模块）|
|**跨模块契约 DTO**|业务名 \+ `Result`|`UserAuthorizationResult`|
|**应用层出参 DTO**|业务名 \+ `Result`|`UserInfoResult`|
|**Web 响应 DTO**|业务名 \+ `Response`|`UserInfoResponse`|
|**持久化对象**|业务名 \+ `Po`|`UserPo`、`UserProfilePo`、`StudentProfilePo`、`TeacherProfilePo`|
|**转换器**|业务名 \+ `Converter`|`UserConverter`、`UserProfileConverter`、`StudentProfileConverter`、`TeacherProfileConverter`|
|**Mapper**|业务名 \+ `Mapper`|`SysUserMapper`、`UserProfileMapper`、`StudentProfileMapper`、`TeacherProfileMapper`、`TeacherClassMapper`|

### 3\.2 错误示范

|❌ 错误|✅ 正确|原因|
|---|---|---|
|在领域模型上使用 `@TableName`/`@TableId`/`@TableLogic`|领域模型保持纯 POJO，映射由 `XxxPo` \+ `XxxConverter` 承担|领域层无外部框架依赖|
|应用层直接注入 Mapper 或仓储实现类|应用层依赖 Domain 仓储接口|依赖倒置|
|Controller 直接调用 Repository/Mapper|Controller 仅调用 Application Service|接口层不越层|
|持久化对象 `UserPo` 直接作为接口出入参|出参使用 `UserInfoResponse`/`UserInfoResult`|PO 不得跨层外泄|
|端口实现类不以 `LocalAdapter` 结尾|端口实现以 `LocalAdapter` 结尾（如 `UserAuthorizationQueryLocalAdapter`）|端口适配器命名规范|
|跨模块 DTO 使用 `Data` / `Info` / `DTO` / `VO` 后缀|统一使用 `Result` 后缀|与 Application 层 DTO 规范对齐|

---

## 四、DTO 规范

### 4\.1 分层 DTO 类型

|层级|入参|出参|示例|
|---|---|---|---|
|**接口层（Controller）**|当前仅有查询接口，无入参 DTO|`XxxResponse`|`ApiResult<UserInfoResponse>`|
|**应用层（Service）**|`UserImportCommand`（来自 data\_exchange 契约）|`XxxResult` / 跨模块数据契约|`UserInfoResult`、`UserAuthorizationResult`|
|**领域层**|领域模型|领域模型|`User`、`StudentProfile`|

### 4\.2 接口层 DTO

- 响应 DTO 使用 `@Data` \+ `@Builder`，字段按前端需要做命名转换（`@JsonProperty` 映射 `user_id`、`campus_id`、`phone_number`、`user_type`）。

- `UserInfoResponse` 承载用户核心资料与 `roles`、`permissions`。

- 接口层 DTO 只做协议适配，不包含业务逻辑。

### 4\.3 应用层 DTO

- `UserInfoResult` 使用 `@Data` \+ `@Builder`，承载用户核心资料与授权信息，作为 `UserInfoQueryService` 的返回值。

- `UserAuthorizationResult` 使用 `@Getter` \+ `@Builder`，承载角色与权限编码集合，作为跨模块契约 DTO。

- 应用层 DTO **严禁**添加 Web/JSON 序列化注解。

- 跨模块契约 DTO 集合字段**永不为 null**，无数据时返回空集合。

### 4\.4 统一响应格式

所有 REST 接口返回 `ApiResult<T>`：

- 成功：`ApiResult.success(data)`

- 失败：`ApiResult.error(ResultCode, message)`

---

## 五、异常处理规范

### 5\.1 异常层次

```Plain Text
BaseException（抽象）
    ├── BizException（业务异常，400/401/403/404/409）
    └── SystemException（系统异常，500）
```

本模块现状：

- 应用层业务失败抛 `BizException`（如用户不存在 → `ResultCode.USER_NOT_FOUND`）。

- 领域模型的状态/参数不变量使用 JDK 异常（`IllegalStateException`、`IllegalArgumentException`），不引入框架异常。

- 值对象 `of(int code)` 非法编码统一抛 `IllegalArgumentException`，消息格式：`"无效的{字段名}编码：{code}"`，**使用中文**。

- 禁止"静默返回默认值"——会让脏数据无感知进入领域模型。

- 基础设施层系统故障**统一由 ****`Exception.class`**** 兜底 handler 处理**，返回 `ResultCode.SYSTEM_ERROR`，日志记录完整堆栈。

    - 理由：前端无需区分 DB / Redis / IO 故障，区分反而泄露内部信息。

    - 排查：通过日志堆栈中的异常类型区分（`DataAccessException` / `RedisConnectionFailureException`）。

    - 告警：未来可通过日志关键字规则实现，无需新增 handler。

### 5\.2 日志与异常语言规范

|类型|语言|示例|
|---|---|---|
|`log.info/warn/error/debug`|**英文**|`log.info("Batch registering {} users", size)`|
|`throw new BizException`|**中文**|`new BizException(ResultCode.USER_NOT_FOUND, "用户不存在")`|

### 5\.3 日志级别

|级别|使用场景|本模块示例|
|---|---|---|
|**ERROR**|系统故障，需人工介入|持久化失败、加密失败|
|**WARN**|异常但可自动恢复|导入行重复、日期解析失败降级、跳过无效查询|
|**INFO**|关键业务流程节点|批量注册开始/完成|
|**DEBUG**|正常流程的中间步骤|查询参数、跳过查询、协议适配|

### 5\.4 全局异常处理器覆盖

由 shared 模块统一覆盖：

- `BizException`（业务异常）

- `ConstraintViolationException`（`@Validated` \+ `@RequestParam`）

- `MethodArgumentNotValidException`（`@Valid` \+ `@RequestBody`）

- `Exception`（兜底）

本模块**不重复定义**全局异常处理器。

---

## 六、存放位置规范

### 6\.1 包路径映射

|类职责|包路径|
|---|---|
|Controller|`interfaces.web`|
|接口层 DTO|`interfaces.web.dto`|
|应用服务（读/写）|`application.service.command` / `application.service.query`|
|应用层出参 DTO / 跨模块契约 DTO|`application.dto.result`|
|应用层端口（供外部模块实现）|`application.port.output`|
|领域能力端口|`domain.port`|
|聚合根/领域模型|`domain.model`|
|值对象|`domain.vb`|
|仓储接口|`domain.repository`|
|持久化对象|`infrastructure.persistence.entity`|
|Mapper|`infrastructure.persistence.mapper`|
|领域↔PO 转换器|`infrastructure.persistence.converter`|
|仓储实现|`infrastructure.persistence.repository`|
|跨模块端口适配器|`infrastructure.adapter`|

### 6\.2 核心规则

|技术栈|存放位置|
|---|---|
|MySQL 所有实现|`infrastructure.persistence`|
|Redis 所有实现|本模块当前无 Redis 实现|
|文件解析|不适用（本模块无文件解析职责）|

### 6\.3 特殊放置规则

|内容|位置|理由|
|---|---|---|
|`UserType`/`Status`/`Gender`/`UserSource`|`domain.vb`|领域值对象，属于领域层|
|`UserProvisioningPort` 实现|`infrastructure.adapter`|实现 data\_exchange 定义的外部端口|
|`UserCredentialQueryPort` 实现|`infrastructure.adapter`|实现 authentication 定义的用户凭证查询端口|
|`TeacherClassQueryPort` 实现|`infrastructure.adapter`|实现 shared 定义的数据权限端口|
|`PasswordEncryptorPort` 实现|`infrastructure.adapter`|加密技术实现|
|`UserAuthorizationQueryPort`|`application.port.output`|本模块定义、**authorization 模块实现**|

---

## 七、防腐层（ACL）规范

### 7\.1 模块间依赖方向

> **粒度说明**：依赖以"子域 / 微服务"为单位描述。
> 
> `iam` 是逻辑分组，不作为编译单元，不参与依赖方向判定。
> 
> 

#### 7\.1\.1 依赖清单

**本模块实现的外部契约（外部 → user）**

- **data\-exchange → user（实现契约）**

    - 契约：`data_exchange.application.port.output.UserProvisioningPort`

    - DTO：`data_exchange.application.dto.command.UserImportCommand`、

    `data_exchange.application.dto.result.UserImportResult`

    - 实现：`infrastructure.adapter.UserProvisioningLocalAdapter`

- **authentication → user（实现契约）**

    - 契约：`authentication.application.port.output.UserCredentialQueryPort`

    - DTO：`authentication.application.dto.result.UserCredentialResult`

    - 实现：`infrastructure.adapter.UserCredentialQueryLocalAdapter`

- **shared → user（实现契约）**

    - 契约：`shared.data_permission.TeacherClassQueryPort`

    - 实现：`infrastructure.adapter.TeacherClassQueryAdapter`

**本模块定义、供外部实现的契约（user → 外部）**

- **authorization 实现 user 的契约**

    - 契约：`user.application.port.output.UserAuthorizationQueryPort`

    - DTO：`user.application.dto.result.UserAuthorizationResult`

    - 实现：`authorization.infrastructure.adapter.UserAuthorizationQueryLocalAdapter`

**本模块依赖 shared 的公共设施**

- `shared.context.RequestContextHolder`、`shared.domain.valueobject.Operator`

- `shared.exception.BizException`

- `shared.response.ApiResult` / `ResultCode`

#### 7\.1\.2 跨模块契约规范

- **归属**：调用方定义 Port \+ DTO，被调用方实现 LocalAdapter

- **位置**：

    - 端口：调用方 `application.port.output.*`

    - DTO：调用方 `application.dto.result.*`

- **命名**：

    - 端口：`能力名词 + QueryPort` / `能力名词 + Port`

    - DTO：`业务名词 + Result`（统一后缀，禁用 `Data` / `Info` / `DTO` / `VO`）

    - 本地实现：`端口名（去 Port）+ LocalAdapter`

    - 远程实现（未来）：`端口名（去 Port）+ FeignAdapter`

#### 7\.1\.3 关于 user 与 iam 的关系

在**逻辑分组**层面，user 与 iam 看似双向：

- user 实现 authentication 定义的契约 → `user → authentication`

- authorization 实现 user 定义的契约 → `authorization → user`

在**子域 / 微服务粒度**上是单向链条，无循环依赖：

- `user → authentication`

- `authorization → user`

未来拆微服务时，各子域独立部署，通过 Feign 通信，契约不变、方向不变。

### 7\.2 跨模块调用方式

|场景|实现方式|本模块示例|
|---|---|---|
|外部模块定义端口，本模块实现（本地调用）|Adapter 实现端口，置于 `infrastructure.adapter`|`UserProvisioningPort` → `UserProvisioningLocalAdapter`|
|外部模块定义端口，本模块实现（本地调用）|Adapter 实现端口，置于 `infrastructure.adapter`|`UserCredentialQueryPort` → `UserCredentialQueryLocalAdapter`|
|本模块定义端口，外部模块实现|端口置于 `application.port.output`，Adapter 由外部模块提供|`UserAuthorizationQueryPort` → `UserAuthorizationQueryLocalAdapter`（位于 authorization）|
|shared 定义端口，本模块实现|Adapter 实现端口|`TeacherClassQueryPort` → `TeacherClassQueryAdapter`|
|微服务拆分（未来）|端口 \+ 远程 Adapter（Feign/HTTP）|远程实现尚未落地|

### 7\.3 端口实现类命名

|端口接口|本地实现|远程实现（预留）|
|---|---|---|
|`UserProvisioningPort`（data\_exchange 定义）|`UserProvisioningLocalAdapter`|`UserProvisioningFeignAdapter`|
|`UserCredentialQueryPort`（authentication 定义）|`UserCredentialQueryLocalAdapter`|`UserCredentialQueryFeignAdapter`|
|`PasswordEncryptorPort`（本模块定义）|`PasswordEncryptorLocalAdapter`|—|
|`TeacherClassQueryPort`（shared 定义）|`TeacherClassQueryAdapter`|—|
|`UserAuthorizationQueryPort`（本模块定义，authorization 实现）|`UserAuthorizationQueryLocalAdapter`（位于 authorization）|`UserAuthorizationQueryFeignAdapter`（位于 user）|

### 7\.4 允许的依赖范围

外部模块依赖 user 模块时，**仅限以下内容**：

|允许依赖|示例|
|---|---|
|跨模块数据契约|`application.dto.result.UserAuthorizationResult`|
|本模块定义、外部实现的端口|`application.port.output.UserAuthorizationQueryPort`（authorization 实现）|
|本模块实现的端口（供依赖倒置）|`application.port.output.UserProvisioningPort`、`application.port.output.UserCredentialQueryPort`、`shared.data_permission.TeacherClassQueryPort`|

**禁止依赖**：

|禁止依赖|示例|
|---|---|
|领域模型|`domain.model.User`、`domain.model.StudentProfile`|
|仓储接口|`domain.port.*`、`domain.repository.*`|
|持久化对象与技术实现|`infrastructure.persistence.entity.*`、`infrastructure.persistence.mapper.*`|
|应用服务实现类|`application.service.*`|

---

## 八、API 路径规范

### 8\.1 RESTful 路径设计

```Plain Text
GET    /user/info        # 查询当前登录用户信息
```

除上述接口外，本模块当前未提供其他 REST 接口。

### 8\.2 路径命名原则

- 模块名用 `user`

- 资源路径保持简洁，动词表达操作

- 新增接口必须经 Application Service，不得直接访问 Repository/Mapper

- 所有接口统一返回 `ApiResult<T>`

---

## 九、配置与常量规范

### 9\.1 常量类命名

- 本模块当前无独立常量类或配置类；如需新增，避免使用技术名词做类名。

- 日期格式等业务策略参数已外部化至 `UserApplicationProperties`（前缀 `user.application`）。

- 配置项：`user.application.data-import.date-format`，默认 `yyyy-MM-dd`。

- 新增业务策略参数时，统一追加至 `UserApplicationProperties`，不硬编码于实现类。

### 9\.2 缓存 Key 规范

- 本模块当前无 Redis 缓存实现。

- `TeacherClassQueryAdapter` 注释提出未来缓存键形如 `user:teacher:classes:{campusId}:{userId}`，尚未实现。

### 9\.3 外部化配置规范

- 本模块已定义 `UserApplicationProperties`（前缀 `user.application`），承载业务策略参数。

- 批量导入的批量大小与导入侧校验规则由 data\_exchange 的 `data-exchange.application.*` 配置提供（`ImportApplicationProperties`），本模块不重复定义。

- 并行加密使用的线程池为 shared 提供的 `computeExecutor`。

---

## 十、Git Commit 规范

```Plain Text
type(scope): subject

type:
  feat     新功能
  fix      修复
  refactor 重构（不改变功能）
  docs     文档
  test     测试
  chore    构建/工具
```

scope 使用模块名或子能力，例如 `user`、`user-import`、`user-profile`、`data-permission`。

示例：

```Plain Text
feat(user): 新增教师-班级关系查询端口实现
fix(user): 修复批量导入中日期解析失败未降级的问题
refactor(user): 收敛用户注册服务的事务边界
```

---

## 十一、典型协作流程

### 11\.1 当前用户信息查询

1. `interfaces.web.UserController` 通过 `RequestContextHolder.getRequiredOperator()` 获取 `Operator`

2. 调用 `application.service.query.UserInfoQueryService.getCurrentUserInfo(operator)`

3. 通过 `UserRepository.findByCampusIdAndUserId` 获取 `User`；不存在抛 `BizException(ResultCode.USER_NOT_FOUND)`

4. 通过 `UserAuthorizationQueryPort.findByUserIdAndCampusId` 获取角色与权限

（由 authorization 模块的 `UserAuthorizationQueryLocalAdapter` 实现）

5. 组装 `UserInfoResult` → Controller 转换为 `UserInfoResponse` → 返回 `ApiResult`

### 11\.2 用户批量导入落库

1. data\_exchange 完成文件解析与校验后调用 `UserProvisioningPort.importUsers`

2. `infrastructure.adapter.UserProvisioningLocalAdapter` 委托 `UserRegistrationService.batchRegister`

3. `UserRegistrationService`：文件内用户名分组去重 → `UserRepository.findExistingUsernames` 批量查库 → 通过查重的数据使用 `computeExecutor` 并行执行 `PasswordEncryptorPort.encode`

4. `UserRegisterService.registerBatch`（声明 `@Transactional`）：逐行判定文件内重复/库内已存在 → `doRegisterSingleUser` 依次落 `User`、`UserProfile`，并按用户类型落 `StudentProfile` 或 `TeacherProfile`

5. 返回逐行 `UserImportResult`（行号、成功标记、userId、错误原因、脱敏后的行数据）

### 11\.3 教师任教班级查询（数据权限）

1. shared 的 `DataPermissionAspect` 依赖 `TeacherClassQueryPort`

2. `infrastructure.adapter.TeacherClassQueryAdapter` 委托 `TeacherClassRepository.findClassIdsByUserIdAndCampusId`

3. `infrastructure.persistence.repository.DbTeacherClassRepository` 委托 `TeacherClassMapper.selectClassIdsByUserIdAndCampusId`

4. SQL 以 `teacher_class` JOIN `teacher_profile`，同时过滤 `status=1`、`deleted=0` 并做 `campus_id` 隔离

### 11\.4 认证数据查询（供 authentication）

1. `user.infrastructure.adapter.UserCredentialQueryLocalAdapter` 实现 authentication 的

`UserCredentialQueryPort` 契约

2. 通过 `UserRepository.findByUsername` 或 `findByCampusIdAndUserId` 查询 `User`

3. 组装 `UserCredentialResult`（含密码哈希、用户类型、状态），**不携带档案字段**

4. authentication 侧通过依赖注入获取该 Port，**不感知 user 模块的内部结构**

**演进**：微服务阶段本流程拆分为

- user 侧暴露 REST Controller（`/internal/user/credential-query`）

- authentication 侧新增 `UserCredentialQueryFeignAdapter`

- 调用方代码零改动

---

**版本**：1\.0

**更新日期**：2026/09/14

**维护者**：loriyuhv

