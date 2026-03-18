<p align="center">
  <a href="https://gitee.com/jensvn">
    <img width="100" src="https://foruda.gitee.com/avatar/1769321857919604470/103715_jensvn_1769321857.png">
  </a>
</p>
<h1 align="center"><a href="https://gitee.com/jensvn/ddd4j">DDD4J基础框架</a></h1>
<h4 align="center">基于DDD（领域驱动设计）并支持SaaS平台的单体微服务基础框架</h4>
<p align="center">
  <img src="https://img.shields.io/badge/language-java-red.svg">
  <img src="https://img.shields.io/hexpm/l/plug.svg">
  <img src="https://img.shields.io/badge/release-1.0.0-blue.svg">
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg">
  <img src="https://gitee.com/jensvn/ddd4j/badge/star.svg?theme=dark"><br>
  原创不易，帮忙右上角点下⭐Star，感谢您的支持。
</p>

#### 笔者在开发过程中不断汲取前辈的优秀代码经验，融入自己的代码特色，提炼高复用性代码，并对中间件进行浅封装。旨在快速搭建SaaS业务系统，减少繁琐的CRUD定义，减少不必要的xml代码书写，通过对Model、Query对象的继承，即可实现你想要的CRUD，提高整体代码效率。

> 该框架搭配[COLA-DDD架构](COLA-DDD架构.md)使用效果更佳

![COLA-DDD架构](COLA-DDD架构.png)


### 框架以组件的方式进行划分，包括：

| 组件 | 说明 | 依赖关系 |
|------|------|----------|
| base-bom | 基础依赖组件 | 无 |
| base-core | 基础核心组件 | 无 |
| base-kit | 基础工具箱 | base-core |
| base-data | 基础数据组件 | base-core |
| base-web | 基础WEB组件 | base-core |
| base-mq | 基础MQ组件 | base-core |
| base-excel | EXCEL组件 | base-core |
| base-oss | 对象存储组件 | base-core |
| base-sms | 短信组件 | base-kit |

### 整体设计理念：简洁、灵活、包容

## DDD4j框架结构

```
base
├── base-bom                         // 基础依赖组件
│   └── pom.xml                      // 基准版本依赖管理POM
├── base-core                        // 基础核心组件
│   ├── config                       // 核心配置
│   ├── context                      // 核心上下文
│   ├── contract                     // 核心契约
│   ├── kit                          // 核心工具
├── base-data                        // 基础数据组件
│   ├── annotation                   // 数据注解
│   ├── config                       // 数据配置
│   ├── kit                          // 数据工具
│   └── mybatisplus                  // MyBatis-Plus扩展
│       ├── handler                  // 处理器
│       └── injector                 // 注入器
├── base-excel                       // EXCEL组件
│   ├── annotation                   // Excel注解
│   ├── aop                          // Excel切面
│   ├── converters                   // 类型转换器
│   ├── enhance                      // 增强器
│   ├── exception                    // 异常处理
│   ├── handler                      // 处理器
│   │   ├── listener                 // 监听器
│   │   ├── sheet                    // Sheet处理器
│   │   └── style                    // 样式处理器
│   ├── head                         // 表头处理
│   ├── processor                    // 处理器
│   ├── properties                   // 配置属性
│   ├── utils                        // 工具类
│   ├── validate                     // 校验器
│   └── vo                           // 值对象
├── base-kit                         // 基础工具箱
│   ├── cache                        // 缓存工具
│   ├── enums                        // 枚举工具
│   ├── lang                         // 语言工具
│   └── web                          // WEB工具
├── base-mq                          // 基础MQ组件
│   ├── config                       // MQ配置
│   ├── core                         // MQ核心组件
│   └── impl                         // MQ实现，目前实现了Kafka、Rabbit、Redis发布订阅、RedisStream、Rocket
│       └── event                    // 事件实现
├── base-oss                         // 对象存储组件
│   ├── config                       // OSS配置
│   ├── contract                     // OSS契约
│   │   ├── dto                      // 数据传输对象
│   │   └── enums                    // 枚举定义
│   └── service                      // OSS服务
│       └── impl                     // OSS服务实现
├── base-sms                         // 短信组件
│   ├── config                       // 短信配置
│   ├── contract                     // 短信契约
│   │   ├── constant                 // 短信常量
│   │   └── dto                      // 数据传输对象
│   ├── factory                      // 短信工厂
│   └── service                      // 短信服务
├── base-web                         // 基础WEB组件
│   ├── annotation                   // WEB注解
│   ├── api                          // 基础控制器
│   ├── config                       // WEB配置
│   ├── core                         // WEB核心组件
│   ├── interceptor                  // WEB拦截器
│   └── utils                        // WEB工具类
└── pom.xml
```

## 核心版本

| 框架 | 版本 |
|------|------|
| JDK | 17+ |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| MyBatis-Plus | 3.5.5 |
| Hutool | 5.8.26 |

## 使用说明

### 环境准备

Clone代码到本地，添加为Maven工程，修改根目录 `pom.xml` 内的仓库地址为自己的私仓并 Deploy 部署

### 部署脚本

项目提供了 `deploy.sh` 脚本用于快速部署到 Maven 仓库：

| 命令 | 说明 |
|------|------|
| `./deploy.sh -s` | 部署到 snapshot 仓库 |
| `./deploy.sh -r` | 部署到 release 仓库（自动移除 -SNAPSHOT 后缀） |
| `./deploy.sh -h` | 显示帮助信息 |

**配置说明**：部署前需修改 `deploy.sh` 中的仓库配置：

```bash
SNAPSHOT_REPO_ID="snapshots"
SNAPSHOT_REPO_URL="http://your-nexus-server/repository/maven-snapshots/"

RELEASE_REPO_ID="releases"
RELEASE_REPO_URL="http://your-nexus-server/repository/maven-releases/"
```

### 集成方式

顶层pom.xml集成base-bom依赖管理，统一第三方依赖包版本

```xml
……
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.ddd4j.cloud</groupId>
            <artifactId>base-bom</artifactId>
            <version>3.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
……
```

### 组件依赖

module 的 `pom.xml` 按需引入 base 组件，集成度高的组件无需重复引入

```xml
……
<!--基础核心组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-core</artifactId>
</dependency>
<!--基础数据组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-data</artifactId>
</dependency>
<!--基础工具箱-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-kit</artifactId>
</dependency>
<!--基础MQ组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-mq</artifactId>
</dependency>
<!--基础WEB组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-web</artifactId>
</dependency>
<!--EXCEL组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-excel</artifactId>
</dependency>
<!--对象存储组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-oss</artifactId>
</dependency>
<!--短信组件-->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-sms</artifactId>
</dependency>
……
```
