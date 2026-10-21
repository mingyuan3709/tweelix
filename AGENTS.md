# Project Instructions

This file provides context for AI assistants working on this project.

## Project Type: Fabric 客户端模组（Minecraft 26.3，单项目）

- 单项目结构：根目录即 Fabric 模组，源码在 `src/`，无子模块
- Gradle Wrapper 9.7.1，需要 JDK 25（`gradle.properties` 的 `org.gradle.java.home`）
- 构建/验收命令：`./gradlew clean build`
- 运行客户端：`./gradlew runClient`

### Documentation
See README.md for project overview.

### Version Control
This project uses Git. See .gitignore for excluded files.

## Agent Guidance

<!-- How should an AI agent approach this project? Fill in tool gotchas, -->
<!-- file patterns to avoid, and anything that helps a model navigate -->
<!-- the codebase without reading every file. -->

- **CodeWhale reads this file as:** AGENTS.md (canonical cross-agent project instructions). <!-- WHALE.md is deprecated; put CodeWhale-specific authority policy in .codewhale/constitution.json -->
- **Read-only surface:** <!-- Which directories can the agent read but not write? -->
- **Never edit:** <!-- Files that are generated, vendored, or owned by another tool -->
- **Always test with:** `./gradlew clean build`

## Architecture

单项目 Fabric 模组。源码、资源、Mixin 配置与 accessWidener 全部位于根目录 `src/`。

### Entry Points
`src/main/java/ink/mingyuan/tweelix/Tweelix.java`（Fabric `ModInitializer`）

### Key Modules
- `src/main/java/ink/mingyuan/tweelix/` — 全部业务代码
- `src/main/resources/fabric.mod.json` — 模组元数据与入口
- `src/main/resources/tweelix.mixins.json` — Mixin 列表
- `src/main/resources/tweelix.accesswidener` — 访问拓宽

### Data Flow
Fabric 入口 `Tweelix` → `TweelixCommon` → `InitHandler`（MaLiLib 初始化）→ 各 feature / 事件监听。

## Cache Stability

<!-- DeepSeek V4 uses a byte-stable prefix cache (128-token granularity). -->
<!-- Keeping these things stable turn-over-turn saves ~90% on input tokens. -->

- **Frequently-rebuilt files:** <!-- Generated code, lockfiles, build artifacts → mark as cache-churn -->
- **Stable scaffolding:** <!-- Config files, project instructions, model cards → keep byte-stable -->
- **Append, don't reorder:** <!-- New context goes at the end of the request; reordering invalidates cache -->

## Guidelines

- Follow existing code style and patterns
- Write tests for new functionality
- Keep changes focused and atomic
- Document public APIs
- Update this file when project conventions change
