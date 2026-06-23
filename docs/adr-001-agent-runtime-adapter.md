# ADR-001: 使用 AgentRuntime Adapter 架构

## 状态

提议中

## 背景

项目目标是开发一个类似 `jetbrains-cc-gui` 的 JetBrains AI 编程插件，同时支持未来扩展更多 Agent 工具，例如 Claude Code、Codex、OpenCode、Aider、ACP-compatible Agent 等。

参考项目 `jetbrains-cc-gui` 已经证明了 ToolWindow + React Webview + Java bridge + Node bridge 的可行性，但它的会话层和发送流程已经明显绑定 Claude/Codex。如果继续把新 Agent 直接加到同一套分支逻辑中，后续会出现 UI、Session、Permission、History 和 Settings 到处判断具体 Agent 的问题。

## 决策

采用 `AgentRuntime` + Adapter 架构：

- UI 层只依赖统一 Agent 事件和 capability。
- Session 层只管理通用 Agent session。
- Permission 层只处理统一权限请求。
- 每个具体 Agent 通过 Adapter 接入。
- AgentHost 负责进程、HTTP、SSE、stdio、JSON-RPC 等通信细节。

## 备选方案

### 方案 A：直接复制 jetbrains-cc-gui 并加入 OpenCode 分支

优点：

- 第一版开发最快。
- 可以复用较多现有思路。

缺点：

- 每新增一个 Agent 都要改主流程。
- 具体 Agent 的 raw format 容易泄漏到 UI。
- 权限、历史和设置会快速变复杂。

### 方案 B：AgentRuntime Adapter 架构

优点：

- 新增 Agent 主要新增 Adapter。
- UI、Session、Permission、History 主流程稳定。
- 可以为所有 Adapter 设计统一 contract tests。
- 更适合长期支持多 Agent。

缺点：

- 第一版需要先定义协议。
- 对现有 Claude/Codex/OpenCode 能力需要做归一化。

## 影响

更容易：

- 新增 OpenCode。
- 新增 ACP-compatible Agent。
- 做权限和历史的统一体验。
- 针对 Adapter 做独立测试。

更困难：

- 第一版需要额外设计 core model。
- 某些 Agent 的独有能力需要通过 capability 或 extension event 暴露。

## 结论

选择方案 B。

但第一版接口必须保持克制，只覆盖：

- health
- startSession
- sendMessage
- abort
- listSessions
- loadMessages
- approvePermission
- dispose

当第二个真实 Agent 接入后，再根据实际重复点扩展接口。

