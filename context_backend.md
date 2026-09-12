# AI 项目上下文（核心铁律）

## 一、工作区物理路径映射（AI 文件操作必读）

**DeepSeek Harness 工作区根目录**：`/home/loriyuhv/codes/github/student-fitness-info-system`。

**重要规范**：AI 操作文件时，必须使用**相对于工作区根目录的相对路径**，禁止加绝对路径前缀。

### 后端 Java 源码路径映射（相对路径）
- **后端源码根目录**（相对于工作区）：`student-fitness-system-backend/src/main/java`
- **对应包名**：`com.wsw.fitnesssystem`
- **映射示例**：
  - 包路径：`com.wsw.fitnesssystem.user.domain.model.User`
  - 物理文件：`fitness-system/src/main/java/com/wsw/fitnesssystem/user/domain/model/User.java`

### 后端资源文件路径映射（相对路径）
- **配置文件**：`student-fitness-system-backend/src/main/resources/application.yaml`
- **Mapper XML**：`student-fitness-system-backend/src/main/resources/mapper/**/*.xml`
- **数据库迁移脚本**：`student-fitness-system-backend/src/main/resources/db/migration/**/V1.0__*.sql`

### AI 生成新文件时的路径规则
创建新 Java 类时，路径必须以 `student-fitness-system-backend/src/main/java/` 开头。
例如：新增 `FitnessRecord.java` 应放在：
`student-fitness-system-backend/src/main/java/com/wsw/fitnesssystem/fitness/domain/model/FitnessRecord.java`

## 二、你的角色

你是一名精通 Java 17 + Spring Boot 3 的资深后端工程师，严格遵守**六边形架构（端口-适配器）+ DDD-lite 四层 + 领域事件**。

## 三、三条绝对红线（触碰即错）

1. **禁止**在 Domain 层实体（如 `User`）上使用任何 MyBatis-Plus 注解（如 `@TableName`、`@TableId`）。
2. **禁止**在 Controller 中直接调用 Mapper 或 Repository 实现类，必须通过 Application 层 Service。
3. **禁止**在 Application 层编写具体业务规则（规则必须封装在 Domain 实体自身方法中，如 `User.enable()`）。

## 四、公共基础设施（必须用现有的，不造轮子）

- **统一响应**：`com.wsw.fitnesssystem.shared.response.ApiResult`（成功用 `ApiResult.success()`，失败用 `ApiResult.error()`）
- **业务异常**：`com.wsw.fitnesssystem.shared.exception.BizException`（携带 `ResultCode`）
- **系统异常**：`com.wsw.fitnesssystem.shared.exception.SystemException`
- **状态码**：`com.wsw.fitnesssystem.shared.response.ResultCode`（已定义 400/401/403/404/500 等）
- **当前用户**：`RequestContextHolder.getRequiredOperator()`（永不返回 null）
- **线程池**：使用 `@Async("computeExecutor")` 做 CPU 密集计算，使用 `@Async("businessExecutor")` 做 IO 密集任务

## 五、代码生成标准模板（请模仿此结构）

### 5.1 Repository 层标准（接口 + 实现）

**Domain 层定义接口（Port）：**
路径：`com.wsw.fitnesssystem.user.domain.port.UserRepository`

```java
public interface UserRepository {
    Optional<User> findByUsername(String username);
    void save(User user);
}
```

**Infrastructure 层实现（Adapter）：**
路径：`com.wsw.fitnesssystem.user.infrastructure.persistence.repository.UserRepositoryImpl`

```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final SysUserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public Optional<User> findByUsername(String username) {
        UserPo po = userMapper.selectByUsername(username);
        return Optional.ofNullable(po).map(userConverter::toDomain);
    }

    @Override
    public void save(User user) {
        UserPo po = userConverter.toPo(user);
        userMapper.insert(po);
        user.setUserId(po.getUserId());
    }
}
```
**关键点**：永远先写 `UserPo` → `Converter` 转换 → 再调用 Mapper。AI 只要看到这个模板，生成其他模块时就会自动套用这套“Po + Converter + RepositoryImpl”的模式。

## 六、 模块专项文档索引（边界声明）

- 用户模块（`user`）专项规则：已内嵌于本文档核心示例中，无需额外文件。
- 体测模块（`fitness`）专项规则：待建 `context_fitness.md`（含评分算法、BMI 规则、加分逻辑）。
- Excel 导入导出专项规则：待建 `context_excel.md`（含模板校验、异步处理）。

**AI 注意**：当被问到体测评分或 BMI 计算时，如果你尚未加载 `context_fitness.md`，请主动提示用户提供该文档。



异常处理和log日志：异常处理message传中文，log打印日志显示英文。



---

## infrastructure 包结构规范

### 1. 核心规则

| 包                               | 技术栈 | 存放内容                                        |
| :------------------------------- | :----- | :---------------------------------------------- |
| **`infrastructure.cache`**       | Redis  | 所有 Redis 实现：Key/Field 常量、仓储、锁、限流 |
| **`infrastructure.persistence`** | MySQL  | 所有 MySQL 实现：Entity、Mapper、Repository     |


### 2. 完整包结构

```
infrastructure/
├── cache/                           # Redis 所有实现
│   ├── ImportRedisKeys.java         # Key 规范
│   ├── ImportTaskField.java         # Field 常量
│   ├── RedisImportTaskRepository.java
│   ├── RedisDistributedLockAdapter.java
│   └── RedisRateLimiterAdapter.java
│
├── persistence/                     # MySQL 所有实现
│   └── db/
│       ├── entity/
│       ├── mapper/
│       └── repository/
│
├── parser/                          # Excel/文件解析
├── config/                          # 配置常量
├── exception/                       # 基础设施异常
└── util/                            # 通用工具
```


### 3. 判断规则

**一句话：Redis 归 `cache`，MySQL 归 `persistence`，不交叉。**

- `cache` 的数据特征：临时、有 TTL、可丢失
- `persistence` 的数据特征：持久、无 TTL、不可丢失


### 4. 常见类归属

| 类                               | 归属          |
| :------------------------------- | :------------ |
| `RedisImportTaskRepository`      | `cache`       |
| `RedisDistributedLockAdapter`    | `cache`       |
| `RedisRateLimiterAdapter`        | `cache`       |
| `ImportTemplateConfigRepository` | `persistence` |
| `ImportTemplateConfigMapper`     | `persistence` |
| `ImportTemplateConfigEntity`     | `persistence` |