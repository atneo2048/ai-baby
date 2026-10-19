# ai-baby / sql-agent

基于大模型的自然语言转 SQL 智能体。用户输入自然语言问题，系统自动完成意图识别、Schema 检索、SQL 生成、安全校验、执行与反思重试。

仓库中同时存在一个早期原型模块 `nlp2sql/`（简化版 NLP→SQL 流程，未接入 Schema 检索 / Guard / Reflection 等能力），`sql-agent/` 为当前主力模块。

---

## 架构升级说明（v2 重构）

本次重构围绕「清理废代码、消除重复逻辑、收敛单一执行管线」三个目标展开，编译通过。

### 一、删除的废代码

以下类/文件在源码中定义但未被任何业务代码引用，或仅服务于已废弃的并行管线，已全部移除：

| 类别 | 已删除文件 | 原因 |
|------|-----------|------|
| 重复管线 | `sql/service/SqlQueryService.java`、`sql/service/impl/DefaultSqlQueryService.java` | 与 `SqlAgentService` 的 generate→guard→execute 流程完全重复，仅被一个测试引用 |
| 重复抽取器 | `sql/service/SqlExtractor.java`、`sql/service/impl/DefaultSqlExtractor.java` | 仅被上述废弃管线使用；主流程通过 `SqlGenerationResultParser` 解析 JSON |
| 重复字典 | `schema/tool/BusinessKeywordDictionary.java` | 与 `SchemaKeywordDictionary` 功能重叠，且从未被注入 |
| 未使用工具 | `schema/tool/SchemaTool.java` | `@Tool` 注解的 Schema 工具，未被任何 AiService 绑定 |
| 未使用领域对象 | `domain/SqlAuditRecord.java`、`domain/RetryPolicy.java`、`domain/SchemaQuery.java`、`domain/AgentContext.java` | 从未被引用 |
| 未使用 DTO | `dto/ChatResponse.java` | Controller 直接返回 `AgentResponse` |
| 未使用方法 | `PromptBuilder.reBuild(...)` 及 `DefaultPromptBuilder` 中对应实现 | 重试逻辑未实现（TODO），方法从未被调用 |
| 未使用资源 | `prompts/sql-agent-recheck.txt` | 仅被 `reBuild` 加载 |
| 对应测试 | `SqlExtractorTest.java`、`SqlAgentPipelineTest.java` | 引用已删除的 `SqlExtractor` / `SqlQueryService` |

### 二、合并的重复逻辑

#### 1. LLM 响应清洗（三处合一）

`removeThinkBlock`、Markdown 代码围栏剥离、JSON 提取逻辑原本分散在：

- `SqlGenerationResultParser`（去 think + 去 markdown + 提 JSON）
- `IntentResultParser`（去 markdown + 提 JSON）
- `LlmEntityExtractor.cleanJson`（去 think + 去 markdown + 提 JSON，实现最完整）

**升级**：新增 `util/LlmResponseSanitizer` 工具类，统一提供 `removeThinkBlock`、`stripMarkdownFence`、`extractJson`、`sanitizeToJson`、`sanitizeToText` 五个静态方法。三个解析器全部改为调用该工具，消除 ~120 行重复代码。

#### 2. `DefaultDataValueRetriever` 数据值查询

原本存在两个几乎相同的方法：`searchValue`（用 `quoteIdentifier` 加反引号）和 `searchExactValue`（内联反引号 + `isValidIdentifier` 校验），分别被两个 `retrieve` 重载调用。

**升级**：合并为单一 `searchExactValue` 方法，统一使用反引号转义 + `isValidIdentifier` 校验，两个 `retrieve` 重载共用。

#### 3. `DefaultSchemaRetriever` 字符串匹配

`containsIgnoreCase` 与 `containsComment` 实现完全相同（均为大小写不敏感的 `contains`）。

**升级**：删除 `containsComment`，全部调用 `containsIgnoreCase`。

### 三、Bug 修复

- `DefaultSchemaService` 存在重复的 `@Slf4j` 与 `@AllArgsConstructor` 导入/注解，已清理为单份。

### 四、架构收敛

- 之前存在两条并行的 SQL 执行管线：`SqlAgentService`（带 Intent / Schema / Guard / Reflection / Audit 的完整管线）和 `DefaultSqlQueryService`（简化版 generate→guard→execute）。删除后者后，`SqlAgentService` 成为唯一入口。
- `SqlAgentService` 内部已串联 Intent→Schema→SQL 生成→Guard→执行→Reflection 重试→Audit 全链路（重试生成 SQL 部分仍为 TODO，待后续接入 `PromptBuilder` 的重试 prompt）。

---

## 第二轮清理（v2.1）

在第一轮重构基础上，进一步审查发现并处理以下冗余：

### 一、删除的死字段 / 死方法

| 位置 | 已删除 | 原因 |
|------|--------|------|
| `AgentExecutionContext` | `schemas` 字段 | 从未被 set/get；Schema 信息由 `queryContext` 承载 |
| `IntentResult` | `reason`、`aiAnalysis` 字段 | 规则分析器与 AI JSON 中会赋值，但主流程从未读取 |
| `IntentClassifier` Prompt | JSON 中的 `reason`、`aiAnalysis` 字段 | 随领域对象字段一并移除 |
| `IntentAnalyzer` | `setReason(...)` 调用 | 字段已删除 |
| `SchemaService` | `getRelations(String)` 方法 | 接口声明 + 实现均存在，但主流程从未调用；关系查询走 `SchemaRetriever.getRelations(List)` |
| `sql/` | `package-info.java` | 空文件，无任何文档内容 |

### 二、Bug 修复：Schema 刷新接口

`SchemaController.refresh()` 原先只调用 `cache.clear()`，清空缓存后并未重新从数据库加载，导致刷新后首次查询拿到空 Schema。

**修复**：改为调用 `schemaService.refresh()`，由 `DefaultSchemaService.refresh()` 执行 `loadFromDatabase()`，实现真正的清缓存 + 重载。同时 `SchemaService.refresh()` 由死方法变为被实际调用的方法。

### 三、合并的重复逻辑

#### 1. 表名规范化 `normalize`

`DefaultSchemaRetriever.normalize(String)` 与 `LocalSchemaCache.normalize(String)` 实现完全相同（`value.trim().toLowerCase()`）。

**升级**：新增 `schema/service/SchemaNames.normalize(String)` 静态工具方法，两处均改为调用，消除重复实现。

#### 2. Schema 路径补全重复计算

`DefaultSchemaRetriever.retrieve(question)` 内部已调用 `expandRelationshipPaths()` 做关系路径补全，而 `DefaultQueryContextBuilder` 在拿到 `directSchemas` 后又调用 `expandRelations(candidateTables)` 再次执行 `expandRelationshipPaths()`。第一次补全的结果在第二次被覆盖重算。

**升级**：`retrieve()` 不再内部调用路径补全，只返回直接匹配的表；路径补全统一由 `expandRelations()` 承担，避免对同一组（或超集）候选表重复执行 BFS 最短路径搜索。

---

## 升级后的项目结构

```
sql-agent/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/ai/baby/sqlagent/
    │   │   ├── SqlAgentApplication.java          # Spring Boot 启动类
    │   │   │
    │   │   ├── agent/
    │   │   │   └── IntentClassifier.java         # LangChain4j AiService 意图分类接口
    │   │   │
    │   │   ├── analyzer/                         # 意图分析（规则 + AI 兜底）
    │   │   │   ├── IntentAnalyzer.java
    │   │   │   └── AIIntentAnalyzer.java
    │   │   │
    │   │   ├── config/                           # 配置
    │   │   │   ├── AIConfig.java                 # 注册 IntentClassifier Bean
    │   │   │   └── LLMProperties.java            # llm.* 配置属性
    │   │   │
    │   │   ├── controller/                       # REST 入口
    │   │   │   ├── ChatController.java           # /chat/chat 主问答接口
    │   │   │   └── SchemaController.java         # /schema/refresh 缓存刷新
    │   │   │
    │   │   ├── domain/                           # 核心领域模型
    │   │   │   ├── AgentExecutionContext.java    # 单次执行上下文（贯穿全链路）
    │   │   │   ├── AgentExecutionRecord.java     # 审计记录
    │   │   │   ├── AgentResponse.java            # 对外响应
    │   │   │   ├── AgentStage.java               # 执行阶段枚举
    │   │   │   ├── GuardResult.java              # SQL 安全检查结果
    │   │   │   ├── IntentResult.java             # 意图分析结果
    │   │   │   ├── IntentType.java               # 意图类型枚举
    │   │   │   ├── QueryResult.java              # SQL 执行结果
    │   │   │   ├── ReflectionDecision.java       # 反思决策
    │   │   │   ├── SqlErrorType.java             # SQL 错误类型枚举
    │   │   │   ├── SqlGenerationResult.java      # SQL 生成结果（sql/explanation/risk）
    │   │   │   └── SqlStatementType.java         # SQL 语句类型枚举
    │   │   │
    │   │   ├── dto/
    │   │   │   └── ChatRequest.java              # 问答请求体
    │   │   │
    │   │   ├── enums/
    │   │   │   └── RiskLevel.java                # 风险等级枚举
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── SqlBlockedException.java      # SQL 被 Guard 拦截
    │   │   │   └── SqlExecutionException.java    # SQL 执行失败
    │   │   │
    │   │   ├── formatter/
    │   │   │   └── SchemaFormatter.java          # Schema → Prompt 文本格式化
    │   │   │
    │   │   ├── guard/                            # SQL 安全校验
    │   │   │   ├── SqlGuard.java
    │   │   │   └── DefaultSqlGuard.java          # 基于 JSqlParser，仅允许 SELECT
    │   │   │
    │   │   ├── llm/                              # 大模型客户端
    │   │   │   ├── ChatModelFactory.java
    │   │   │   └── DefaultChatModelFactory.java
    │   │   │
    │   │   ├── parser/                           # LLM 响应解析
    │   │   │   ├── Parser.java                   # 泛型解析接口
    │   │   │   ├── IntentResultParser.java
    │   │   │   └── SqlGenerationResultParser.java
    │   │   │
    │   │   ├── prompt/                           # Prompt 构建
    │   │   │   ├── PromptBuilder.java
    │   │   │   ├── DefaultPromptBuilder.java
    │   │   │   └── PromptTemplateLoader.java
    │   │   │
    │   │   ├── schema/                           # Schema 检索子域
    │   │   │   ├── domain/                       # Schema 相关领域模型
    │   │   │   │   ├── ColumnInfo.java
    │   │   │   │   ├── DataValueMatch.java
    │   │   │   │   ├── ExtractedEntities.java
    │   │   │   │   ├── QueryContext.java         # 查询上下文（Schema+实体+值+关系）
    │   │   │   │   ├── SchemaInfo.java
    │   │   │   │   ├── SchemaKeyword.java
    │   │   │   │   ├── SchemaMatch.java
    │   │   │   │   └── SchemaRelation.java
    │   │   │   ├── service/
    │   │   │   │   ├── DataValueRetriever.java   # 实体值数据库验证
    │   │   │   │   ├── EntityExtractor.java      # LLM 实体提取
    │   │   │   │   ├── QueryContextBuilder.java  # 组装 QueryContext
    │   │   │   │   ├── SchemaCache.java          # Schema 缓存接口
    │   │   │   │   ├── SchemaNames.java          # 表名规范化工具
    │   │   │   │   ├── SchemaRetriever.java      # Schema 检索 + 关系补全
    │   │   │   │   ├── SchemaService.java        # 数据库元数据加载
    │   │   │   │   └── impl/
    │   │   │   │       ├── DefaultDataValueRetriever.java
    │   │   │   │       ├── DefaultQueryContextBuilder.java
    │   │   │   │       ├── DefaultSchemaRetriever.java
    │   │   │   │       ├── DefaultSchemaService.java
    │   │   │   │       ├── LlmEntityExtractor.java
    │   │   │   │       └── LocalSchemaCache.java
    │   │   │   └── tool/
    │   │   │       └── SchemaKeywordDictionary.java  # 业务关键词 → 表映射
    │   │   │
    │   │   ├── service/                          # Agent 核心服务
    │   │   │   ├── SqlAgentService.java          # 主编排管线（唯一入口）
    │   │   │   ├── AgentExecutionAuditService.java
    │   │   │   ├── ReflectionService.java        # 执行失败反思
    │   │   │   ├── SqlErrorClassifier.java       # SQL 错误分类
    │   │   │   └── impl/
    │   │   │       ├── DefaultAgentExecutionAuditService.java
    │   │   │       ├── DefaultReflectionService.java
    │   │   │       └── DefaultSqlErrorClassifier.java
    │   │   │
    │   │   ├── sql/                              # SQL 生成
    │   │   │   └── service/
    │   │   │       ├── SqlGenerator.java
    │   │   │       └── impl/DefaultSqlGenerator.java
    │   │   │
    │   │   ├── tool/
    │   │   │   └── DatabaseTool.java             # JDBC 执行 SQL
    │   │   │
    │   │   └── util/
    │   │       └── LlmResponseSanitizer.java     # LLM 响应清洗（去 think / markdown / 提 JSON）
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── init-sql/init.sql
    │       └── prompts/sql-agent.txt
    │
    └── test/java/com/ai/baby/sqlagent/test/
        ├── DataValueRetrieverTest.java
        ├── DefaultReflectionServiceTest.java
        ├── DefaultSqlErrorClassifierTest.java
        ├── EntityExtractorTest.java
        ├── QueryContextTest.java
        ├── SchemaCacheTest.java
        ├── SchemaRetrieverTest.java
        ├── SqlAgentE2ETest.java
        ├── SqlGeneratorTest.java
        └── SqlGuardTest.java
```

---

## 核心执行管线

`SqlAgentService.chat(question)` 是系统唯一入口，执行流程：

```
question
  │
  ├─ 1. analyzeIntent      IntentAnalyzer（规则优先，AI 兜底）
  ├─ 2. retrieveSchema     QueryContextBuilder → SchemaRetriever + EntityExtractor + DataValueRetriever
  ├─ 3. generateSql        SqlGenerator（PromptBuilder + ChatModel + SqlGenerationResultParser）
  ├─ 4. executeWithRetry   循环（最多 MAX_RETRIES=2 次）：
  │     ├─ guardSql        SqlGuard（JSqlParser，仅允许 SELECT）
  │     ├─ executeSql      DatabaseTool
  │     └─ 失败时：SqlErrorClassifier → ReflectionService → regenerateSql（带反馈重新生成 SQL）
  ├─ 5. buildResponse
  └─ 6. record             AgentExecutionAuditService
```

---

## 第三轮完善（v2.2）

### 一、重试逻辑落地

之前 `executeWithRetry` 中 `regenerateSql` 为 TODO，`ReflectionService` 返回的 `feedback` 从未被使用。本轮完成闭环：

- `QueryContext` 新增 `feedback` 字段（首次生成为 null，重试时由 `SqlAgentService` 写入）。
- `DefaultPromptBuilder.build` 检测到 `feedback` 非空时，在 Prompt 末尾追加「上一次 SQL 执行失败，请根据反馈重新生成」段落。
- `SqlAgentService.regenerateSql(context, feedback)` 将反馈写入 `QueryContext` 后重新调用 `sqlGenerator.generate`，得到修正后的 SQL 再次进入 Guard → Execute 循环。
- 重试上限仍为 `MAX_RETRIES = 2`，由 `ReflectionService` 判断错误类型是否适合重试（语法/字段/表不存在可重试，连接/权限/超时不可重试）。

### 二、全项目代码格式化

对 `sql-agent/src/main/java` 下全部 68 个源文件进行格式化整理，统一为 Java 标准风格：

- 统一 4 空格缩进（修复了部分文件 2 空格缩进，如 `DefaultChatModelFactory`、`IntentClassifier`）。
- 消除语句中间不必要的换行（如 `context.setStage(\n AgentStage.FAILED)` 合并为单行）。
- 规范 import 顺序：`java.*` → `javax.*` → 第三方包（`org.*`、`com.*`、`lombok.*`、`dev.*` 等）。
- 方法签名参数较少时合并为单行，避免无意义的跨行。
- 修复 `ChatController` 的 `@Autowired` 字段注入为构造器注入。
- 移除多余空行、行尾空格、重复 import。
- `LLMProperties.prefix` 常量改为符合 Java 常量命名规范的 `PREFIX`。

---

## 编译

```bash
cd sql-agent
JAVA_HOME=/path/to/jdk-21 mvn compile
```
