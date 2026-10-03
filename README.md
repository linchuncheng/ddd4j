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
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private Integer age;
    private Long tenantId;
    @OnCreate                       // 插入时自动填充当前时间
    private LocalDateTime createTime;
    @OnUpdate                       // 插入和更新时自动填充当前时间
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

### 租户隔离

默认开启。写入时自动追加 `tenant_id`，查询时自动过滤（基于 MyBatis-Plus `TenantLineInnerInterceptor`），租户值来自 `AppContext`。当前请求没有租户时不过滤（适合平台级任务）。

```java
users.list(query);                 // 自动追加 tenant_id = 当前租户
users.list(query.ignoreTenant());  // 显式跳过，仅本次生效
```

### 请求上下文

`ContextInterceptor` 从请求头解析并写入 `AppContext`，请求结束自动清理；`traceId` 同时写入 MDC（日志 pattern 加 `%X{traceId}` 即可）和响应头。

| 请求头 | 含义 |
|--------|------|
| `X-User-Id` | 当前用户 |
| `X-Tenant-Id` | 当前租户 |
| `X-Trace-Id` | 链路ID，缺省自动生成并回写响应头 |

跨线程显式传递，不做任何隐式继承：

```java
executor.submit(AppContext.wrap(() -> audit(userId())));   // 携带快照，执行后清理
```

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
| domain | 聚合、业务规则、repo 接口 |

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
