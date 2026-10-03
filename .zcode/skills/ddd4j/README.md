# ddd4j skill

DDD4J（AI-first 极简 DDD 内核：契约 + 显式仓储 + Web 核心）的**复制式接入技能**：把框架源码直接复制进目标工程，项目完全拥有代码，AI 可直接读改，不依赖私仓发版。

## 这是什么

- `SKILL.md` —— 给 CodingAgent 读的作业指引：接入流程、冲突矩阵、编码约定
- `source/` —— 框架全部主源码（24 个类，按包路径存放，整棵复制即接入）
- `test-template/` —— H2 集成验收测试，照抄即可验证接入是否成功

`source/` 与 `test-template/` 是框架源码快照，勿手改。

## 安装

拷贝本目录到任意支持 SKILL.md 约定的 Agent 技能目录（默认跨 Agent 目录）：

```bash
cp -R ddd4j ~/.agents/skills/ddd4j   # 或 ~/.claude/skills/ 等任意 Agent 技能目录
```

## 使用

装好后对任意 CodingAgent 说：

- 「接入 ddd4j」—— 新工程全量复制，或既有工程部分接入（自动处理与已有 R/租户/认证的冲突）
- 「用 ddd4j 写一个新业务域」—— 按单一类型约定起手模型/Query/仓储
- 「用 ddd4j 改造 <业务域>」—— 存量 LambdaQueryWrapper 渐进迁移，先评估再转换

## 前提

JDK 21 / Spring Boot 3.5.x / MyBatis-Plus 3.5.x（响应契约：code 字符串，成功 `"200"`）。

## 更新

框架更新后，重新安装/更新本技能即可获取最新源码快照。
