---
name: ddd4j
description: DDD4J AI-first 极简 DDD 内核的复制式接入技能。当用户要求接入 ddd4j 框架、在新工程或现有工程集成 ddd4j、写新业务域的模型/Query/仓储/CRUD、或询问 ddd4j 的约定（单一类型、Query 后缀、租户隔离、审计填充）时触发。
title: DDD4J框架
category: 开发工具
---

# DDD4J 框架接入（复制式）

DDD4J 是 AI-first 极简 DDD 内核：契约 + 显式仓储 + Web 核心，约 24 个类。本技能采用**复制式接入**——把框架源码复制进目标工程，项目完全拥有代码，AI 可直接读改，不依赖私仓发版。

本目录结构：

- `SKILL.md`：接入流程与编码约定（本文件）
- `source/`：框架全部主源码，`java/` 按包路径存放，`resources/` 含自动装配声明
- `test-template/`：验收测试与支撑类（含 H2 集成测试），照抄即可验证接入是否成功

`source/` 与 `test-template/` 是框架源码快照，**勿手改**；框架更新后重新安装/更新本技能即可。

## 接入流程

### 第一步：确定复制范围（按目标工程情况二选一）

| 目标工程情况 | 复制范围 | 关键动作 |
|---|---|---|
| **全新工程**（无既有 web 约定） | `source/` 全量：contract + context + data + web + config | 无 |
| **已有统一响应/异常处理的工程**（自带 R/PageData/全局异常处理器） | 只复制 `context/` + `data/` 两个包 | 见下方"冲突处理"，避免双份 R 和双份异常处理器 |

### 第二步：复制源码

```bash
# 新工程全量：
cp -R <skill目录>/source/java/* <项目>/src/main/java/
cp -R <skill目录>/source/resources/* <项目>/src/main/resources/

# 既有工程部分接入（只取 context + data，保包路径）：
# 复制 source/java/com/ddd4j/cloud/context 与 .../data 两个目录即可
```

包名 `com.ddd4j.cloud.*` **保持不改**——`AutoConfiguration.imports` 按全限定名装配，改名即断。

若不复制 `web/` 包，必须编辑 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，**删掉 `com.ddd4j.cloud.web.Ddd4jWebConfiguration` 那一行**，否则启动报类找不到。

### 第三步：补依赖（pom）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
<!-- MP 3.5.9+ 分页/多租户拦截器独立成包 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
</dependency>
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
```

技术基线：JDK 21、Spring Boot 3.5.x、MyBatis-Plus 3.5.x、Lombok。测试验收另需（test scope）：`spring-boot-starter-test`、`spring-boot-starter-web`、`spring-boot-starter-validation`、`h2`。

### 第四步：处理与既有设施的冲突

| 目标工程已有 | 处理方式 |
|---|---|
| 自己的 `MybatisPlusInterceptor`（含自家租户处理器） | ddd4j 的拦截器 Bean 标了 `@ConditionalOnMissingBean`，自动让位，无冲突。若还想用 `Query.ignoreTenant()`，在自己租户处理器的 `ignoreTable` 里加一行桥接：`if (TenantManager.isIgnored()) return true;`（`import com.ddd4j.cloud.data.TenantManager`） |
| 自己的 R / GlobalExceptionHandler | 不要复制 ddd4j 的 `web/` 包（见第一步）；响应契约对齐 ddd4j 的（code 字符串 `"200"` 成功）则前端零感知 |
| 自己的认证体系（JWT/网关） | 认证通过后把用户/租户写入 `AppContext`：`AppContext.current().setUserId(...)` / `setTenantId(...)`，并保证请求结束 `AppContext.clear()`；或写两个响应头 `X-User-Id`/`X-Tenant-Id` 交给 `ContextInterceptor`（仅全量接入时有） |
| 上下文无用户的系统任务 | 审计操作人保持原值不填充，属正常行为 |

### 第五步：验收

把 `test-template/` 复制到项目 `src/test/`（包路径保持），跑 `RepositoryIntegrationTest`：它用 H2 验证 CRUD、分页、租户隔离、审计填充四件事，全绿即接入成功。

## 存量业务改造

改造 = 把存量服务的手写 `LambdaQueryWrapper` 换成 Query 声明 + Repository 调用。**按业务域渐进，一次一个域**，与裸 Mapper 存量代码并存，不搞全局一刀切。

### 改造前评估（值不值得改）

| 信号 | 结论 |
|---|---|
| 查询条件 ≥3 个；同一 wrapper 构造在多个方法重复出现；大量 `cond != null, X::getY, val` 三段式 | 值得改，收益最大 |
| 只有一两个条件的简单查询 | 不改，样板低于收益 |
| 完全动态拼装（报表引擎、通用导出）、一次性脚本 | 不改；复杂条件留给 `search(w -> ...)` 逃生口 |

先列出目标域内所有手写 wrapper 的位置和逐个评估，与用户确认后再动手。

### 转换模式（wrapper ↔ Query 字段对照）

| wrapper 写法 | Query 字段 |
|---|---|
| `w.eq(status != null, E::getStatus, status)` | `private String status;`（null/空白自动跳过，**条件开关直接删掉**——这是最大样板来源） |
| `w.like(hasText(kw), E::getSkuName, kw)` | `private String skuNameLike;` |
| `w.in(coll != null, E::getId, ids)` | `private List<String> idIn;`（ID 类型随工程） |
| `w.ge(from != null, E::getCreateTime, from)` | `private LocalDateTime createTimeStart;`（时间范围用 `Start`/`End`，语义比 `Ge`/`Le` 清楚） |
| `w.orderByDesc(E::getCreateTime)` | `private String orderBys = "createTime_DESC";` |
| `new Page<>(current, size)` + `mapper.selectPage` | `repository.page(query)`，分页参数在 Query 上 |

转换规则：

- 条件字段名 = 列名驼峰 + 后缀，Controller 可以直接接收 Query 作入参（swagger 自动成文档）
- 原 wrapper 里的**固定条件**（恒真业务条件，如"未删除"）由服务层显式赋值或落到 `search()` 逃生口，Query 字段只承载可变入参
- 一次只转换一个服务方法，转完跑该域测试再继续

### 存量实体：保留 BaseEntity，不改

存量实体继续继承 BaseEntity、继续走自家 FieldFill 填充——与 ddd4j 审计体系互不干扰（注解体系不同，互为空操作），`MybatisRepository<M, Q>` 对实体零要求。**改造期间不要把存量实体换成注解风格**（纯 churn 无收益）；只有新建实体才用 `@OnCreate*` 注解风格。

### 验收清单（每个域）

1. 域级集成测试：参照 `test-template/RepositoryIntegrationTest` 覆盖该域主要查询，断言 CRUD/分页/租户行为
2. 回归三点：分页 total 与改造前一致；租户过滤行为不变（改造不触碰租户拦截器）；列表结果抽样对比
3. 收口标准：该域内不再有 `new LambdaQueryWrapper`（个别合理保留需注释原因）

### 建议指令（可直接对 AI 说）

> 用 ddd4j 改造 &lt;业务域&gt;：先按改造前评估列出该域所有手写 wrapper 的位置和逐个结论（值得/不值得 + 原因），我确认后逐方法转换，每个域补集成测试并跑验收清单。

## 编码约定（写代码时强制遵守）

### 单一类型（最重要）

同一张表**只允许一份数据定义**：模型即实体（`@TableName` 直接标注），直接作为 `Repository` 泛型参数。domain 层放行为（纯函数、不变式），**不放数据的第二份拷贝**——不要造 `XxxModel` + `XxxPO` 双胞胎。确需分离（富聚合/多表）必须显式映射（手写或 MapStruct），**禁止反射拷贝**（BeanUtils/copyProperties）。

### 仓储：显式方法调用

```java
@Repository
public class UserRepository extends MybatisRepository<User, UserQuery> {
    public UserRepository(UserMapper mapper) { super(mapper); }
}
// users.insert(u) / updateById / deleteById / get / one / list / page / count / exists
// 逃生口：users.search(w -> w.apply(...))，禁止 resurrect ActiveRecord 风格
```

### Query 条件后缀（字段名 = 列名驼峰 + 后缀，大小写敏感）

| 后缀 | SQL | 后缀 | SQL |
|---|---|---|---|
| （无） | `=` | `Ge` / `Le` | `>=` / `<=` |
| `In` / `NotIn` | `IN` / `NOT IN` | `Start` / `End` | `>=` / `<=`（时间范围） |
| `Like` | `LIKE '%v%'` | `IsNull` | `IS NULL`（true）/ `IS NOT NULL`（false） |
| `LikeLeft` / `LikeRight` | `LIKE 'v%'` / `LIKE '%v'` | `orderBys` | `createTime_DESC,id_ASC`（防注入校验） |
| `Not` | `!=` | `Gt` / `Lt` | `>` / `<` |

### 审计四注解（填充发生在仓储写入时）

| 注解 | 时机 | 值 | 覆盖语义 |
|---|---|---|---|
| `@OnCreate` | 插入 | 当前时间 | 已有值不覆盖（可回填历史） |
| `@OnUpdate` | 插入+更新 | 当前时间 | 无条件覆盖 |
| `@OnCreateBy` | 插入 | 当前用户（String） | 已有值不覆盖 |
| `@OnUpdateBy` | 插入+更新 | 当前用户 | 无条件覆盖 |

### 租户：fail-closed

上下文无租户时查询直接失败（不静默读全量）。平台任务显式豁免：`query.ignoreTenant()` 或 `TenantManager.ignoring(...)`；无租户列的表走配置 `ddd4j.tenant.exclude-tables`。租户值为 String。

### 响应与异常

`R`：`{code, msg, data}`，code 字符串，成功 `"200"`；`BizException(msg)` 自动转失败响应；参数校验失败 400；未知异常 500 + 服务端记 traceId。HTTP 状态恒 200，`@RawResponse` 跳过包装。

### 领域事件（进程内）

- 定义：`public class UserCreatedEvent extends DomainEvent<User>`（载荷 + eventId + occurredOn）
- 发布：简便方式 `new UserCreatedEvent(user).publish()`（经 SpringContext 桥）；显式方式注入 `ApplicationEventPublisher` 后 `publisher.publishEvent(event)`
- 监听：应用层 `@EventListener`（同步）/ `@Async @EventListener`（异步，AppContext 自动传播）/ `@TransactionalEventListener`（事务提交后处理）
- 异步传播由 Spring 默认任务执行器上的 `AppContextTaskDecorator` 实现（`ddd4j.context-propagation` 默认开）；应用自定义任务执行器 Bean 时自行应用该装饰器
- 跨服务 MQ 事件不在内核范围，直接用官方 MQ client

### 上下文跨线程

不做隐式继承：`executor.submit(AppContext.wrap(task))` 携带快照、执行后清理。userId/tenantId 均为 String。

## 配置项

```yaml
ddd4j:
  tenant:
    enabled: true
    column: tenant_id
    exclude-tables: []
  web:
    enabled: true            # 只复制 data/context 接入时不会装配，无需关心
    user-header: X-User-Id
    tenant-header: X-Tenant-Id
    trace-header: X-Trace-Id
  data-config:
    db-type: mysql
  context-propagation: true  # 异步任务自动传播 AppContext
```
