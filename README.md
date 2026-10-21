# Tweelix

> 一个面向原版生存的轻量客户端辅助模组  
> *A vanilla-focused lightweight client-side utility mod.*

[![License: LGPL-3.0](https://img.shields.io/badge/License-LGPL%203.0-blue.svg)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-green.svg)](https://www.minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-✓-orange.svg)](#-支持平台-platforms)

---

## 📑 目录

- [📦 支持平台](#-支持平台-platforms)
- [🔧 依赖](#-依赖-dependencies)
- [🚀 快速开始](#-快速开始-quick-start)
- [✨ 功能总览](#-功能总览-feature-overview)
- [⌨️ 命令](#️-命令-commands)
- [🎮 快捷键](#-快捷键-hotkeys)
- [📂 子配置](#-子配置-sub-configs)
- [🌐 命令提示资源包](#-命令提示资源包-command-hint-resource-pack)
- [🛠️ 构建](#️-构建-build)
- [📄 许可证](#-许可证-license)

---

## 📦 支持平台 | Platforms

| 平台 | 状态 |
|------|------|
| <img src="https://cdn.jsdelivr.net/npm/simple-icons@v7/icons/fabric.svg" width="16" height="16"> Fabric | ✅ 支持 |

**Minecraft 版本：`26.3`**（仅 Fabric 单模块）

---

## 🔧 依赖 | Dependencies

- [MaLiLib](https://github.com/maruohon/malilib) — 配置系统（必需）
- [ModMenu](https://github.com/TerraformersMC/ModMenu) — 模组菜单集成（可选）

---

## 🚀 快速开始 | Quick Start

1. 安装模组及对应依赖
2. 进入游戏，按 **`Z`** 打开配置界面
3. 勾选需要的功能，并为功能设置快捷键（可选）

> ⚠️ **注意**：所有功能默认**关闭**，请手动启用所需功能。

---

## ✨ 功能总览 | Feature Overview

<details>
<summary><b>🛠️ 通用 (Generic)</b></summary>

| 功能 | 默认 | 说明 |
|------|:----:|------|
| **打开配置界面** | `Z` | 打开 Tweelix 配置界面的快捷键 |
| **参观模式** | ❌ | 禁止破坏/放置方块以及伤害实体，适合服务器参观 |
| **复制准心目标信息** | ❌ | 复制方块、实体、背包物品的注册名(ID)、显示名称或坐标 |
| **默认提示开关** | ❌ | 为没有独立提示开关的功能提供默认值 |
| **执行告示牌命令** | ❌ | 右键点击告示牌背面，执行以 `/` 或 `!!` 开头的命令 |
| **告示牌编辑自动换行** | ❌ | 粘贴文字时自动按像素宽度换行 |
| **告示牌背面粘贴无限制** | ❌ | 背面粘贴时忽略客户端文字长度限制，配合执行命令使用 |

</details>

<details>
<summary><b>🖥️ 显示 (Display)</b></summary>

| 功能 | 默认 | 说明 |
|------|:----:|------|
| **命令提示** | ❌ | 输入命令时展示参数说明（需启用内置资源包） |
| **显示单层基岩** | ❌ | 在下界顶层高亮标记仅有一层的基岩 |
| **自定义游戏模式切换** | ❌ | 自定义 F3+F4 切换行为，可配置各模式指令 |
| **局域网端口刷新** | ❌ | 在“开放局域网”界面添加刷新按钮 |
| **显示光影包按钮** | ❌ | 在选项界面材质包按钮下方添加光影包按钮 |
| **隐藏跨队伍玩家名称** | ❌ | 按 F1 切换隐藏其他队伍的玩家名牌 |
| **显示投影文件夹按钮** | ❌ | 在 Litematica 主菜单中添加打开投影文件夹的按钮 |
| **无限夜视** | ❌ | 获得无限夜视效果，可调节亮度强度 |

</details>

<details>
<summary><b>🔧 调整 (Tweaks)</b></summary>

| 功能 | 默认 | 说明 |
|------|:----:|------|
| **平坦挖掘** | ❌ | 非潜行状态下，不会破坏高度低于玩家的方块 |
| **围墙挖掘** | ❌ | 非潜行状态下，无法挖掘位于配置方块下方的方块 |
| **添加挖掘冷却** | ❌ | 限制挖掘速度，防止连续破坏方块 |
| **移除挖掘冷却** | ❌ | 移除生存模式下默认的 5gt 挖掘冷却 |
| **灵魂出窍** | ❌ | 类似原版旁观模式，可自由移动观察 |
| **保护可疑方块** | ❌ | 非潜行状态下，禁止破坏可疑的沙子/沙砾 |
| **一键清空背包** | ❌ | 按快捷键快速丢弃配置好的物品，可设置过滤规则 |
| **挖掘黑名单** | ❌ | 禁止挖掘配置的方块名单 |

</details>

---

## ⌨️ 命令 | Commands

所有命令通过 `/tweelix` 根命令访问。

### 📋 动作命令

| 命令 | 描述 |
|------|------|
| `/tweelix openConfigGui` | 打开配置界面 |
| `/tweelix crosshairCopy` | 触发准星目标数据复制 |
| `/tweelix emptyInventory` | 一键清空背包（需先启用该功能） |
| `/tweelix exportkeys` | 导出所有命令翻译键到 `exports/command_keys.json` |

### ⏱️ 延迟执行

```text
/tweelix delay <命令1> <时间> <命令2>
```

---

## 🛠️ 构建 | Build

项目为**单项目**结构：根目录即 Fabric 模组，源码位于 `src/`，没有子模块（原 `common` / `neoforge` / `fabric` 目录均已移除）。

- 需要 **JDK 25**：Minecraft 26.3 的依赖为 class file 69，且 Loom 要求 Gradle 运行在 25 上。
  `gradle.properties` 中的 `org.gradle.java.home` / `org.gradle.java.installations.paths` 指向本机 JDK 25，换机器时按实际路径修改。
- Gradle Wrapper：9.7.1

```bash
./gradlew projects      # 只应出现根项目 tweelix（No sub-projects）
./gradlew clean build   # 产物：build/libs/tweelix-<版本>-mc26.3-fabric.jar
./gradlew runClient
```

---

## 📄 许可证 | License

本项目基于 [LGPL-3.0-or-later](LICENSE.txt) 发布。