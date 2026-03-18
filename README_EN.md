<p align="center">
  <a href="https://github.com/linchuncheng">
    <img width="100" src="https://github.com/linchuncheng.png">
  </a>
</p>
<h1 align="center"><a href="https://github.com/linchuncheng/ddd4j">DDD4J Framework</a></h1>
<h4 align="center">A DDD-based monolithic microservice foundation framework with SaaS platform support</h4>
<p align="center">English | <a href="README.md">中文</a></p>
<p align="center">
  <img src="https://img.shields.io/badge/language-JDK17-red.svg">
  <img src="https://img.shields.io/hexpm/l/plug.svg">
  <img src="https://img.shields.io/badge/snapshot-3.0.0-blue.svg">
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg">
  <img src="https://img.shields.io/github/stars/linchuncheng/ddd4j?style=social"><br>
  If this project helps you, please give it a ⭐ Star. Thanks!
</p>

#### This framework incorporates excellent coding practices from predecessors, blends in my own coding style, extracts highly reusable code, and provides lightweight encapsulation for middleware. It aims to rapidly build SaaS business systems, reduce tedious CRUD definitions, minimize unnecessary XML code, and enable CRUD operations through simple inheritance of Model and Query objects, improving overall development efficiency.

> This framework works best when combined with COLA-DDD architecture, see details below

![COLA-DDD Architecture](COLA-DDD架构.png)


### Framework Components

| Component | Description | Dependencies |
|-----------|-------------|--------------|
| base-bom | Base dependency management | None |
| base-core | Core component | None |
| base-kit | Utility toolkit | base-core |
| base-data | Data component | base-core |
| base-web | Web component | base-core |
| base-mq | Message queue component | base-core |
| base-excel | Excel component | base-core |
| base-oss | Object storage component | base-core |
| base-sms | SMS component | base-kit |

### Design Philosophy: Simple, Flexible, Inclusive

## Project Structure

```
base
├── base-bom                         // Base dependency management
│   └── pom.xml                      // BOM POM for version management
├── base-core                        // Core component
│   ├── config                       // Core configuration
│   ├── context                      // Core context
│   ├── contract                     // Core contracts
│   ├── kit                          // Core utilities
├── base-data                        // Data component
│   ├── annotation                   // Data annotations
│   ├── config                       // Data configuration
│   ├── kit                          // Data utilities
│   └── mybatisplus                  // MyBatis-Plus extensions
│       ├── handler                  // Handlers
│       └── injector                 // Injectors
├── base-excel                       // Excel component
│   ├── annotation                   // Excel annotations
│   ├── aop                          // AOP aspects
│   ├── converters                   // Type converters
│   ├── enhance                      // Enhancers
│   ├── exception                    // Exception handling
│   ├── handler                      // Handlers
│   │   ├── listener                 // Listeners
│   │   ├── sheet                    // Sheet handlers
│   │   └── style                    // Style handlers
│   ├── head                         // Header processing
│   ├── processor                    // Processors
│   ├── properties                   // Configuration properties
│   ├── utils                        // Utilities
│   ├── validate                     // Validators
│   └── vo                           // Value objects
├── base-kit                         // Utility toolkit
│   ├── cache                        // Cache utilities
│   ├── enums                        // Enum utilities
│   ├── lang                         // Language utilities
│   └── web                          // Web utilities
├── base-mq                          // MQ component
│   ├── config                       // MQ configuration
│   ├── core                         // MQ core components
│   └── impl                         // MQ implementations (Kafka, Rabbit, Redis Pub/Sub, RedisStream, Rocket)
│       └── event                    // Event implementations
├── base-oss                         // Object storage component
│   ├── config                       // OSS configuration
│   ├── contract                     // OSS contracts
│   │   ├── dto                      // Data transfer objects
│   │   └── enums                    // Enum definitions
│   └── service                      // OSS services
│       └── impl                     // Service implementations
├── base-sms                         // SMS component
│   ├── config                       // SMS configuration
│   ├── contract                     // SMS contracts
│   │   ├── constant                 // SMS constants
│   │   └── dto                      // Data transfer objects
│   ├── factory                      // SMS factory
│   └── service                      // SMS services
├── base-web                         // Web component
│   ├── annotation                   // Web annotations
│   ├── api                          // Base controllers
│   ├── config                       // Web configuration
│   ├── core                         // Web core components
│   ├── interceptor                  // Web interceptors
│   └── utils                        // Web utilities
└── pom.xml
```

## Core Versions

| Framework | Version |
|-----------|---------|
| JDK | 17+ |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| MyBatis-Plus | 3.5.5 |
| Hutool | 5.8.26 |

## Getting Started

### Prerequisites

Clone the repository, add as a Maven project, modify the repository URL in the root `pom.xml` to your private repository, and deploy.

### Deployment Script

The project provides a `deploy.sh` script for quick deployment to Maven repositories:

| Command | Description |
|---------|-------------|
| `./deploy.sh -s` | Deploy to snapshot repository |
| `./deploy.sh -r` | Deploy to release repository (auto-removes -SNAPSHOT suffix) |
| `./deploy.sh -h` | Show help information |

**Configuration**: Modify the repository settings in `deploy.sh` before deployment:

```bash
SNAPSHOT_REPO_ID="snapshots"
SNAPSHOT_REPO_URL="http://your-nexus-server/repository/maven-snapshots/"

RELEASE_REPO_ID="releases"
RELEASE_REPO_URL="http://your-nexus-server/repository/maven-releases/"
```

### Integration

Add base-bom dependency management in your root `pom.xml` to unify third-party dependency versions:

```xml
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
```

### Component Dependencies

Add required base components in your module's `pom.xml`. Components with higher integration don't need to be re-imported:

```xml
<!-- Core component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-core</artifactId>
</dependency>
<!-- Data component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-data</artifactId>
</dependency>
<!-- Utility toolkit -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-kit</artifactId>
</dependency>
<!-- MQ component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-mq</artifactId>
</dependency>
<!-- Web component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-web</artifactId>
</dependency>
<!-- Excel component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-excel</artifactId>
</dependency>
<!-- Object storage component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-oss</artifactId>
</dependency>
<!-- SMS component -->
<dependency>
    <groupId>com.ddd4j.cloud</groupId>
    <artifactId>base-sms</artifactId>
</dependency>
```

## COLA-DDD Architecture

![COLA-DDD Architecture](COLA-DDD架构.png)

This architecture uses DDD as the core for business modeling, Clean Architecture/Hexagonal Architecture as the guiding principle for layering and dependencies, integrates CQRS/EDA for parameter-level read-write separation and domain event-driven design, and leverages COLA to provide standardized layering and project scaffolding, creating a highly maintainable and easily extensible enterprise-level application architecture.

### Architecture Design Principles

| Principle | Description |
|-----------|-------------|
| DDD | Domain-centered design using bounded contexts, aggregates, entities, and value objects to build domain models |
| Clean Architecture / Hexagonal Architecture | Separates technical implementation from business logic, outer layers depend on inner layers, domain layer remains unpolluted by external details |
| CQRS | Command Query Responsibility Segregation, improving maintainability and performance optimization potential |
| EDA | Event-Driven Architecture, domain events defined at API layer, internal events at domain layer, processed by adapters (MQ) and application layer (Event) handlers |
| COLA Specification | Provides layering standards and engineering practices, emphasizing module responsibility subdivision, dependency constraints, and extension point mechanisms |

### Layer Responsibilities

| Layer | Dependencies | Responsibilities & Key Packages |
|-------|--------------|--------------------------------|
| API Layer | - | Exposes service interfaces; `common`, `dto`, `event`, `service` |
| Adapter Layer | Application, API, Domain | Adapts external requests to internal calls; `http`, `rpc`, `mq`, `job`, `repo` |
| Application Layer | Domain | Orchestrates business use cases, controls transaction boundaries; `service`, `event` |
| Domain Layer | None | Encapsulates core business rules; aggregate packages `command`, `query`, `model`, `repo`, common package `common` (`constant`, `event`, `vo`) |
| Infrastructure Layer | - | Provides engineering configuration; `config` |

### Dependency Flow

```
Call flow: External → Adapter → Application → Domain
Implementation: Adapter implements Api.service, Domain.repo
```

- Outer layers depend on inner layers, domain layer has no external dependencies
- API layer defines `service` abstract interfaces, implemented by adapter layer
- Domain layer defines `repo` repository interfaces, implemented by adapter layer
- Infrastructure layer provides engineering configuration, technical components are encapsulated in DDD4j framework

## Project Structure

```
{project}
├── {project}-api                     // API module
│   └── src/main/java/{package}/api
│       ├── common                   // Common definitions (constants, enums, exceptions, utilities)
│       └── {context}                // Context (e.g., user, order)
│           ├── dto                  // External data transfer objects
│           ├── event                // Context events (MQ)
│           └── service              // Service interfaces
├── {project}-web                     // Bootstrap module
│   ├── src/main/java/{package}
│   │   ├── {Project}Application.java // Application entry point
│   │   ├── adapter                   // Adapter layer
│   │   │   ├── http                  // HTTP adapters
│   │   │   │   ├── admin              // Admin endpoints
│   │   │   │   └── client             // Client endpoints
│   │   │   ├── job                   // Scheduled job adapters
│   │   │   ├── mq                    // MQ consumer adapters
│   │   │   ├── repo                  // Repository adapters
│   │   │   │   ├── dao               // Data access objects
│   │   │   │   ├── entity            // Persistence entities
│   │   │   │   └── impl              // Repository implementations
│   │   │   └── rpc                   // RPC adapters
│   │   ├── application               // Application layer
│   │   │   ├── event                 // Event handlers
│   │   │   └── service               // Application services
│   │   ├── domain                    // Domain layer
│   │   │   ├── {aggregate}           // Aggregates
│   │   │   │   ├── command           // Commands
│   │   │   │   ├── model             // Models
│   │   │   │   ├── query             // Queries
│   │   │   │   └── repo              // Repository interfaces
│   │   │   └── common                // Common domain
│   │   │       ├── constant          // Constants
│   │   │       ├── event             // Internal events
│   │   │       └── vo                // Value objects
│   │   └── infrastructure            // Infrastructure layer
│   │       └── config                // Configuration
│   └── src/main/resources
│       ├── application.yml
│       └── mapper                    // MyBatis mapper files
└── pom.xml
```

## Demo Example

### Demo Project Structure

```
demo
├── demo-api                           // API module
│   └── src/main/java/com/example/demo/api
│       ├── common                      // Common definitions
│       └── user                        // User context
│           ├── dto                     // DTOs
│           │   ├── UserCreateCmd.java
│           │   └── UserExcelDTO.java
│           ├── event                   // Domain events
│           │   └── UserSyncMQEvent.java
│           └── service                 // Service interfaces
│               └── UserDubboService.java
├── demo-web                           // Bootstrap module
│   ├── src/main/java/com/example/demo
│   │   ├── DemoApplication.java        // Entry point
│   │   ├── adapter                     // Adapter layer
│   │   │   ├── http                    // HTTP adapters
│   │   │   │   ├── admin                // Admin endpoints
│   │   │   │   │   └── UserAdminController.java
│   │   │   │   └── client               // Client endpoints
│   │   │   │       └── UserClientController.java
│   │   │   ├── mq                      // MQ consumers
│   │   │   │   └── UserMQListener.java
│   │   │   ├── repo                    // Repository adapters
│   │   │   │   ├── dao                 // DAOs
│   │   │   │   │   └── UserDAO.java
│   │   │   │   ├── entity              // Entities
│   │   │   │   │   └── UserPO.java
│   │   │   │   └── impl                // Implementations
│   │   │   │       └── UserRepositoryImpl.java
│   │   │   └── rpc                    // RPC adapters
│   │   │       └── UserDubboServiceImpl.java
│   │   ├── application                 // Application layer
│   │   │   ├── event                   // Event handlers
│   │   │   │   └── UserEventHandler.java
│   │   │   └── service                 // Application services
│   │   │       └── UserAppService.java
│   │   ├── domain                      // Domain layer
│   │   │   ├── common                  // Common domain
│   │   │   │   ├── event                // Internal events
│   │   │   │   │   └── UserCreatedEvent.java
│   │   │   │   └── vo                  // Value objects
│   │   │   │       └── UserStateVO.java
│   │   │   └── user                    // User domain
│   │   │       ├── command             // Commands
│   │   │       │   └── UserCreateCmd.java
│   │   │       ├── model               // Models
│   │   │       │   └── User.java
│   │   │       ├── query               // Queries
│   │   │       │   └── UserQuery.java
│   │   │       └── repo                // Repository interfaces
│   │   │           └── UserRepository.java
│   │   └── infrastructure              // Infrastructure layer
│   │       └── config                  // Configuration
│   │           └── RocketMQConfig.java
│   └── src/main/resources
│       ├── application.yml
│       └── mapper                      // MyBatis mappers
│           └── UserMapper.xml
└── pom.xml
```

### Module Dependencies

| Module | Dependencies | Description |
|--------|--------------|-------------|
| demo-api | None | Interface definitions, depended by other modules |
| demo-web | demo-api | Bootstrap entry, aggregates all layers |

### API Layer

#### Data Transfer Objects

```java
package com.example.demo.api.user.dto;

import lombok.Data;

@Data
public class UserCreateCmd {
    private String username;
    private String nickname;
    private String email;
}
```

```java
package com.example.demo.api.user.dto;

import com.ddd4j.cloud.excel.annotation.ExcelLine;
import lombok.Data;

@Data
public class UserExcelDTO {
    @ExcelLine
    private Integer lineNum;
    private String username;
    private String nickname;
    private String email;
}
```

#### RPC Service Interface

```java
package com.example.demo.api.user.service;

import com.example.demo.api.user.dto.UserCreateCmd;
import com.example.demo.domain.user.model.User;

public interface UserDubboService {
    
    Long createUser(UserCreateCmd cmd);
    
    User getById(Long id);
    
    void updateStatus(Long id, Integer status);
}
```

#### Domain Events

```java
package com.example.demo.api.user.event;

import com.ddd4j.cloud.core.contract.MQEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSyncMQEvent extends MQEvent {
    private Long userId;
    private String action; // CREATE, UPDATE, DELETE
}
```

### Domain Layer

#### Model

```java
package com.example.demo.domain.user.model;

import com.ddd4j.cloud.core.contract.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Model {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private Long tenantId;
}
```

#### Query

```java
package com.example.demo.domain.user.query;

import com.ddd4j.cloud.core.contract.Query;
import com.example.demo.domain.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends Query<User> {
    // Exact match
    private Long id;
    private String username;
    private Integer status;
    private Long tenantId;
    
    // Fuzzy query (suffix 'Like' auto-converts to LIKE '%xxx%')
    private String nicknameLike;
    
    // In query (suffix 'In' auto-converts to IN (...))
    private String emailIn;
    
    // Range query
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}
```

#### Command

```java
package com.example.demo.domain.user.command;

import lombok.Data;

@Data
public class UserCreateCmd {
    private String username;
    private String nickname;
    private String email;
}
```

#### Repository Interface

```java
package com.example.demo.domain.user.repo;

import com.ddd4j.cloud.core.contract.BaseRepository;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;

public interface UserRepository extends BaseRepository<User, UserQuery> {
}
```

#### Internal Events

```java
package com.example.demo.domain.common.event;

import com.ddd4j.cloud.core.contract.DomainEvent;
import com.example.demo.domain.user.model.User;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends DomainEvent<User> {
    
    public UserCreatedEvent(User user) {
        super(user);
    }
}
```

### Adapter Layer

#### HTTP Adapters

##### Admin Endpoints

```java
package com.example.demo.adapter.http.admin;

import com.example.demo.api.user.dto.UserCreateCmd;
import com.example.demo.api.user.dto.UserExcelDTO;
import com.example.demo.application.service.UserAppService;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.R;
import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.annotation.ImportExcel;
import com.ddd4j.cloud.web.api.AggregateController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Management - Admin")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserAdminController implements AggregateController {
    private final UserAppService userAppService;
    
    @Operation(summary = "Create user")
    @PostMapping("/create")
    public R<Void> create(@RequestBody UserCreateCmd cmd) {
        userAppService.create(cmd);
        return R.ok();
    }
    
    @Operation(summary = "Page query")
    @PostMapping("/page")
    public R<Page<User>> page(@RequestBody UserQuery query) {
        return R.ok(userAppService.page(query));
    }
    
    @Operation(summary = "Export Excel")
    @ExportExcel(name = "User List")
    @GetMapping("/export")
    public List<UserExcelDTO> export(UserQuery query) {
        return query.list();
    }
    
    @Operation(summary = "Import Excel")
    @PostMapping("/import")
    public R<Void> importExcel(@ImportExcel List<UserExcelDTO> users) {
        userAppService.importUsers(users);
        return R.ok();
    }
    
    // Auto-generated CRUD interfaces from AggregateController:
    // GET  /{model}/detail/{id}   - Get by ID
    // POST /{model}/list          - List query
    // POST /{model}/exist         - Check existence
    // POST /{model}/count         - Count
    // POST /{model}/modify        - Update
    // POST /{model}/save          - Save (create or update)
    // POST /{model}/remove/{ids}  - Batch delete
}
```

##### Client Endpoints

```java
package com.example.demo.adapter.http.client;

import com.example.demo.application.service.UserAppService;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.R;
import com.ddd4j.cloud.web.api.AggregateController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management - Client")
@RestController
@RequestMapping("/client/user")
@RequiredArgsConstructor
public class UserClientController implements AggregateController {
    private final UserAppService userAppService;
    
    @Operation(summary = "Page query")
    @PostMapping("/page")
    public R<Page<User>> page(@RequestBody UserQuery query) {
        return R.ok(userAppService.page(query));
    }
    
    // Auto-generated CRUD interfaces from AggregateController:
    // GET  /{model}/detail/{id}   - Get by ID
    // POST /{model}/list          - List query
}
```

#### Repository Adapters

##### Persistence Entity

```java
package com.example.demo.adapter.repo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ddd4j.cloud.data.annotation.OnCreate;
import com.ddd4j.cloud.data.annotation.OnUpdate;
import com.ddd4j.cloud.data.annotation.TenantId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class UserPO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String email;
    
    private Integer status;
    
    @TenantId
    private Long tenantId;
    
    @OnCreate
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @OnUpdate
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

##### Data Access Object

```java
package com.example.demo.adapter.repo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ddd4j.cloud.core.contract.annotation.DAO;
import com.example.demo.adapter.repo.entity.UserPO;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DAO(entity = UserPO.class, model = User.class, query = UserQuery.class)
public interface UserDAO extends BaseMapper<UserPO> {
}
```

##### Repository Implementation

```java
package com.example.demo.adapter.repo.impl;

import com.example.demo.adapter.repo.dao.UserDAO;
import com.example.demo.adapter.repo.entity.UserPO;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;
import com.example.demo.domain.user.repo.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserDAO userDAO;
    
    public UserRepositoryImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    // BaseRepository provides default CRUD operations
}
```

#### MQ Consumer Adapter

```java
package com.example.demo.adapter.mq;

import com.ddd4j.cloud.core.contract.annotation.MQEventListener;
import com.example.demo.api.user.event.UserSyncMQEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMQListener {
    
    @MQEventListener(topic = "USER_SYNC", tags = "CREATE")
    public void onUserSync(UserSyncMQEvent event) {
        log.info("Received user sync message: userId={}, action={}", event.getUserId(), event.getAction());
    }
}
```

#### RPC Adapter

```java
package com.example.demo.adapter.rpc;

import com.example.demo.api.user.dto.UserCreateCmd;
import com.example.demo.api.user.service.UserDubboService;
import com.example.demo.application.service.UserAppService;
import com.example.demo.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class UserDubboServiceImpl implements UserDubboService {
    
    private final UserAppService userAppService;
    
    @Override
    public Long createUser(UserCreateCmd cmd) {
        return userAppService.create(cmd);
    }
    
    @Override
    public User getById(Long id) {
        return userAppService.getById(id);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        userAppService.updateStatus(id, status);
    }
}
```

### Application Layer

#### Application Service

```java
package com.example.demo.application.service;

import com.example.demo.api.user.dto.UserCreateCmd;
import com.example.demo.api.user.dto.UserExcelDTO;
import com.example.demo.domain.common.event.UserCreatedEvent;
import com.example.demo.api.user.event.UserSyncMQEvent;
import com.example.demo.domain.user.model.User;
import com.example.demo.domain.user.query.UserQuery;
import com.ddd4j.cloud.core.contract.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAppService {
    
    @Transactional
    public Long create(UserCreateCmd cmd) {
        User user = User.builder().username(cmd.getUsername()).nickname(cmd.getNickname()).email(cmd.getEmail()).build();
        // Rich domain model: call save() directly
        user.save();
        // Publish domain event
        new UserCreatedEvent(user).publish();
        // Publish MQ event
        UserSyncMQEvent.builder().userId(user.getId()).action("CREATE").build().publish();
        return user.getId();
    }
    
    public User getById(Long id) {
        return UserQuery.builder().id(id).build().one();
    }
    
    public Page<User> page(UserQuery query) {
        return query.page();
    }
    
    public void updateStatus(Long id, Integer status) {
        User.builder().id(id).status(status).build().update();
    }
    
    public void delete(Long id) {
        UserQuery.builder().id(id).build().delete();
    }
    
    @Transactional
    public void importUsers(List<UserExcelDTO> users) {
        users.forEach(vo -> {
            User.builder().username(vo.getUsername()).nickname(vo.getNickname()).email(vo.getEmail()).build().save();
        });
    }
}
```

#### Event Handler

```java
package com.example.demo.application.event;

import com.example.demo.domain.common.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventHandler {
    
    @Async
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        log.info("User created successfully: {}", event.get().getUsername());
        // Send notifications, sync data, etc.
    }
}
```

### Infrastructure Layer

#### Configuration

```java
package com.example.demo.infrastructure.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQConfig {
    // RocketMQ configuration
}
```

### Configuration File

```yaml
# application.yml
spring:
  application:
    name: demo-service
  datasource:
    url: jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

# DDD4J configuration
ddd4j:
  mq:
    impl: rocket
    default-topic: DEMO_TOPIC
    namespace: demo
```
