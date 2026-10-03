<p align="center">
  <a href="https://github.com/linchuncheng">
    <img width="100" src="https://github.com/linchuncheng.png">
  </a>
</p>
<h1 align="center"><a href="https://github.com/linchuncheng/ddd4j">DDD4J 4.0</a></h1>
<h4 align="center">AI-first 极简 DDD 内核：契约 + 显式仓库 + Web 核心</h4>
<p align="center">
  <img src="https://img.shields.io/badge/language-JDK21-red.svg">
  <img src="https://img.shields.io/badge/spring%20boot-3.5.x-blue.svg">
  <img src="https://img.shields.io/badge/mybatis--plus-3.5.x-green.svg">
  <img src="https://img.shields.io/badge/tests-39%20passing-brightgreen.svg">
  <img src="https://img.shields.io/github/stars/linchuncheng/ddd4j?style=social"><br>
  如果这个项目对你有帮助，请点个 ⭐ Star 支持一下，感谢！
</p>

## 为什么推倒重写

3.x 时代，框架的核心价值是"帮人省掉 CRUD 样板代码"。AI 时代，样板代码恰恰是 AI 最擅长写的——而 3.x 为省样板付出的代价（启动时全 classpath 扫描、字符串寻址的通用 REST 端点、静态 ServiceLocator、五种 MQ 客户端全量编译）反而成了人和 AI 理解代码的最大障碍。

于是 4.0 按四条原则重写：

| 原则 | 含义 |
|------|------|
| 显式 > 魔法 | 无 classpath 扫描、无字符串寻址、全构造注入，每个行为都能 grep 到出处 |
| 单一类型 | 领域模型即持久化实体（`@TableName` 直接标在模型上），不再强制 PO/Model 双轨 |
| 最小依赖 | 框架自身只依赖 Spring Boot + MyBatis-Plus + Jackson，不依赖 Hutool |
| 内核 > 全家桶 | 只做契约、CRUD、Web 三件事；MQ/Excel/OSS 等直接用官方 starter，AI 写这些调用毫不费力 |

技术栈与 [fengqun-scm](https://github.com/linchuncheng) 后端保持一致：**JDK 21、Spring Boot 3.5.x、MyBatis-Plus 3.5.x**。响应契约与现网前端对齐（`code` 为字符串，成功 `"200"`）。

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>ddd4j-core</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</dependency>
```

### 定义模型、查询、仓储

模型即实体，一张表三段代码，全部显式：

```java
// 1. 模型：直接标注 MyBatis-Plus 注解
// ID/租户/操作人用 String（雪花ID字符串化，对齐 fengqun-scm 风格）
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String username;
    private Integer age;
    private String tenantId;
    @OnCreateBy                     // 插入时自动填充当前用户
    private String createdBy;
    @OnUpdateBy                     // 插入和更新时自动填充当前用户（无条件覆盖）
    private String updatedBy;
    @OnCreate                       // 插入时自动填充当前时间
    private LocalDateTime createTime;
    @OnUpdate                       // 插入和更新时自动填充当前时间（无条件覆盖）
    private LocalDateTime updateTime;
}

// 2. 查询：字段名 + 条件后缀 即查询条件
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends Query {
    private String username;        // username = ?
    private String usernameLike;    // username LIKE '%?%'
    private Integer ageGe;          // age >= ?
}

// 3. 仓储：继承即可，全部方法显式可查
@Repository
public class UserRepository extends MybatisRepository<User, UserQuery> {
    public UserRepository(UserMapper mapper) {
        super(mapper);
    }
}
```

> `UserMapper` 是 MyBatis-Plus 的标准要求：`public interface UserMapper extends BaseMapper<User> {}`

### 使用

```java
@Service
@RequiredArgsConstructor
public class UserAppService {
    private final UserRepository users;

    @Transactional
    public Long create(UserCreateCmd cmd) {
        User user = new User();
        user.setUsername(cmd.getUsername());
        users.insert(user);                     // 审计字段、租户ID自动处理
        return user.getId();
    }

    public Page<User> page(UserQuery query) {
        return users.page(query);               // 条件、排序、分页由 Query 声明
    }
}
```

Controller 返回任意对象，框架自动包装为统一响应；抛出 `BizException` 自动转换为失败响应：

```java
@GetMapping("/{id}")
public User detail(@PathVariable Long id) {     // 响应: {"code":"200","msg":"操作成功","data":{...}}
    return users.get(id);
}

public void rename(Long id, String name) {
    User db = users.get(id);
    if (db == null) throw new BizException("用户不存在");
    db.setUsername(name);
    users.updateById(db);
}
```

## Query 条件后缀约定

字段名 = 列名驼峰 + 后缀（大小写敏感），翻译规则显式实现在 `QueryTranslator`（约 200 行，可单测、可 grep）：

| 后缀 | SQL | 示例 |
|------|-----|------|
| （无） | `=` | `username` |
| `In` / `NotIn` | `IN` / `NOT IN`，值支持集合、数组、逗号分隔字符串 | `statusIn` |
| `Like` | `LIKE '%值%'` | `nicknameLike` |
| `LikeLeft` | `LIKE '值%'`（前缀匹配） | `nicknameLikeLeft` |
| `LikeRight` | `LIKE '%值'`（后缀匹配） | `nicknameLikeRight` |
| `Not` | `!=` | `statusNot` |
| `Gt` / `Lt` / `Ge` / `Le` | `>` / `<` / `>=` / `<=` | `ageGe` |
| `Start` / `End` | `>=` / `<=`（时间范围语义化别名） | `createTimeStart` |
| `IsNull` | `IS NULL`（true）/ `IS NOT NULL`（false） | `emailIsNull` |

排序写在 `orderBys` 字段：`createTime_DESC,id_ASC`（列名经过合法性校验，防注入）。

复杂条件走逃生口，直接操作 Wrapper：`users.search(w -> w.apply("date(create_time) = {0}", today))`。

## 内置能力

### 审计字段

四个注解声明审计字段，填充发生在仓储写入时（显式、不依赖 MyBatis-Plus 内部机制）：

| 注解 | 填充时机 | 值 | 覆盖语义 |
|------|---------|-----|---------|
| `@OnCreate` | 插入 | 当前时间（LocalDateTime/LocalDate/Date/Long 毫秒） | 已有值不覆盖（可回填历史数据） |
| `@OnUpdate` | 插入 + 更新 | 当前时间 | **无条件覆盖** |
| `@OnCreateBy` | 插入 | 当前用户（`AppContext.userId()`，String） | 已有值不覆盖 |
| `@OnUpdateBy` | 插入 + 更新 | 当前用户 | **无条件覆盖** |

`update_time/update_by` 记录的就是"这次是谁改的"，所以更新时无条件覆盖；`create_*` 尊重显式赋值，便于导入和订正。上下文无用户（系统任务）时操作人保持原值。

### 租户隔离

默认开启。写入时自动追加 `tenant_id`，查询时自动过滤（基于 MyBatis-Plus `TenantLineInnerInterceptor`），租户值来自 `AppContext`（String 类型，与雪花ID字符串化风格一致）。

**缺省语义是 fail-closed**：上下文没有租户时，查询直接失败而不是静默读到全量数据——这是 SaaS 系统的安全底线：

```java
users.list(query);                 // 自动追加 tenant_id = 当前租户
users.list(query.ignoreTenant());  // 显式豁免，仅本次生效——平台级任务/系统作业用这个
```

全局豁免平台表用配置：`ddd4j.tenant.exclude-tables`（如 sys_user 等无租户列的表）。

### 请求上下文

`ContextInterceptor` 从请求头解析并写入 `AppContext`，请求结束自动清理；`traceId` 同时写入 MDC（日志 pattern 加 `%X{traceId}` 即可）和响应头。

| 请求头 | 含义 |
|--------|------|
| `X-User-Id` | 当前用户（字符串，如雪花ID） |
| `X-Tenant-Id` | 当前租户（字符串） |
| `X-Trace-Id` | 链路ID，缺省自动生成并回写响应头 |

自研认证体系（JWT/网关）只需在认证通过后把这两个响应头值写入即可接入，无需替换安全栈。

跨线程显式传递，不做任何隐式继承：

```java
executor.submit(AppContext.wrap(() -> audit(userId())));   // 携带快照，执行后清理
```

### 领域事件

进程内事件驱动：领域层/应用层发布，应用层监听处理。发布用 Spring 原生 `ApplicationEventPublisher`（显式注入，不用静态定位器）：

```java
@RequiredArgsConstructor
public class UserAppService {
    private final ApplicationEventPublisher publisher;

    @Transactional
    public Long create(UserCreateCmd cmd) {
        User user = new User();
        users.insert(user);
        publisher.publishEvent(new UserCreatedEvent(user));   // 领域层定义事件类
        return user.getId();
    }
}
```

事件定义：继承 `DomainEvent<T>`，自带 `eventId` / `occurredOn` / `get()`：

```java
public class UserCreatedEvent extends DomainEvent<User> {
    public UserCreatedEvent(User payload) { super(payload); }
}
```

应用层监听：

```java
@Async
@EventListener
public void onUserCreated(UserCreatedEvent event) {
    // 异步处理器内 AppContext 自动传播，可直接取用户/租户/traceId
}
```

- 事务内发布、提交后处理：`@TransactionalEventListener`
- 异步上下文传播默认开启（`ddd4j.context-propagation=false` 关闭）；应用自定义任务执行器 Bean 时需自行应用 `AppContextTaskDecorator`
- 跨服务 MQ 事件不在内核范围：直接用官方 MQ client

### 统一响应与异常

| 场景 | 响应 |
|------|------|
| 成功 | `{"code":"200","msg":"操作成功","data":...}` |
| `BizException("xxx")` | `{"code":"500","msg":"xxx"}` |
| `BizException(403, "xxx")` | `{"code":"403","msg":"xxx"}` |
| 参数校验失败 | `{"code":"400","msg":"keyword 不能为空"}` |
| 未知异常 | `{"code":"500","msg":"操作失败"}`，服务端记录 traceId |

HTTP 状态保持 200，业务码在 body 中。标注 `@RawResponse` 的端点跳过包装。

## 配置项

```yaml
ddd4j:
  tenant:
    enabled: true          # 租户隔离开关，默认 true
    column: tenant_id      # 租户字段名，默认 tenant_id
    exclude-tables: []     # 不参与租户隔离的表
  web:
    enabled: true          # 上下文/响应包装/全局异常开关，默认 true
    user-header: X-User-Id
    tenant-header: X-Tenant-Id
    trace-header: X-Trace-Id
  data-config:
    db-type: mysql         # 分页方言
  context-propagation: true  # 异步任务自动传播 AppContext（关闭后 @Async 处理器拿不到用户/租户）
```

## COLA 分层建议

![COLA-DDD架构](COLA-DDD架构.png)

框架只约束依赖方向，不约束工程结构。推荐分层：

```
调用流向：外部 → Adapter → Application → Domain
实现关系：Adapter 实现 Api.service、Domain.repo
```

| 分层 | 职责 |
|------|------|
| api | 对外契约：dto、event、service 接口 |
| adapter | http / rpc / mq / repo 实现（仓储实现放这里） |
| application | 用例编排、事务边界、事件处理 |
| domain | 聚合行为（纯函数、不变式、状态机）、repo 接口 |

### 模型与实体：单一类型（强制约定）

同一张表**只允许存在一份数据定义**：领域模型即持久化实体（标注 `@TableName`），直接作为 `Repository` 的泛型参数。领域层放的是**行为**（如周期推算、金额分摊这类纯函数），不是数据的第二份拷贝。

| 位置 | 放什么 | 不放什么 |
|------|--------|----------|
| 数据类（每个上下文一个包，如 `entity` 或 `domain/model`） | 唯一的数据类：`@TableName` + 业务方法 | — |
| domain | 纯函数计算器、聚合行为 | 与数据类平行的 Model 镜像 |

理由：AI 改代码时只修改上下文里的文件，两份字段定义必然走向不一致；反射式拷贝（BeanUtils/copyProperties）让这种不一致**编译器看不见、运行时才丢数据**。消灭重复 = 消灭这类静默错误。

确需分离的场景（富聚合需要构造纪律、一个聚合来自多张表），必须**显式分离**：仓储落在 adapter 层操作 PO，领域模型独立存在，转换用手写映射或 MapStruct（编译期校验漏字段）——**禁止任何反射拷贝**：

```java
// adapter/repo 层：操作 PO
@Repository
public class OrderRepository extends MybatisRepository<OrderPO, OrderQuery> { ... }

// application 层：显式转换，字段接线编译期可见
Order order = new Order(po.getId(), po.getItems(), po.getState());
```

## 从 3.x 迁移

| 3.x | 4.0 |
|-----|-----|
| `Model.save()` / `query.page()` 充血模型 | `repository.insert(model)` / `repository.page(query)` 显式仓储 |
| `@DAO(entity, model, query)` 双轨映射 | 模型即实体，一个类 |
| `RepositoryContext` classpath 扫描 | 无扫描，泛型直接解析 |
| `/{model}/*` 字符串寻址通用端点 | 每个聚合显式 Controller / Repository |
| `R.code` Integer，成功 `0` | `R.code` 字符串，成功 `"200"`（对齐现网） |
| `Page.size / current` | `Page.pageSize / currentPage / totalPages` |
| 9 模块：kit/web/data/mq/excel/oss/sms… | 单模块内核，其余用官方 starter |
| `TransmittableThreadLocal` 隐式传递 | `AppContext.wrap()` 显式传递 |
| `base-sms` 博士通/阿里云、模板管理业务域 | 移出框架，属于业务工程 |

## 设计取舍（明确不做）

- 不做 MQ / Excel / OSS / SMS / 缓存 / 分布式锁封装——官方 starter 已经足够好
- 不做 Feign / Nacos / Sentinel 集成——按项目需要自行引入
- 不做多 MQ 实现抽象——需要时业务直接用官方 client
- 不追求"零 Controller / 零 Repository"——显式代码是 AI 可维护性的前提

## 构建

```bash
mvn clean verify    # 39 个测试（含 H2 集成测试）
./deploy.sh -s      # 发布 SNAPSHOT 到私仓（脚本需先配置仓库地址）
```
