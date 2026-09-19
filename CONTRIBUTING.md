# 贡献与开发流程

本项目采用 **Feature Branch + Pull Request + CI 门禁** 工作流，所有代码变更经 PR 合入 `main`，不直接向主分支推送。

## 工作流概览

```
规格锁定 → /implement 写代码 → Feature Branch → Push → PR → CI Green → Merge to main
```

| 阶段 | 术语 | 说明 |
|------|------|------|
| 本地开发 | **Feature Branch** | 从 `main` 切出短期功能分支 |
| 推送 | **Push / Publish Branch** | 推送到远程 `origin` |
| 发起合并 | **Pull Request (PR)** | 在 GitHub 创建 PR，目标分支为 `main` |
| 自动化检查 | **CI / Status Checks** | GitHub Actions 运行构建与测试 |
| 合并门禁 | **CI Gate / Required Checks** | 全部检查通过后才允许合并 |
| 修复循环 | **Fix → Push → Re-run CI** | 失败则修复并推送，直至 **CI Green** |
| 合入主分支 | **Merge to main** | PR 合并后删除功能分支（推荐） |

## 标准步骤

### 1. 同步主分支并创建功能分支

```bash
git checkout main
git pull origin main
git checkout -b feature/简短描述
```

分支命名建议：

- `feature/首页-paging` — 新功能
- `fix/购物车空态` — 缺陷修复
- `docs/贡献流程` — 仅文档变更

### 2. 开发与本地验证

```bash
./gradlew assembleDebug
./gradlew test
```

提交信息使用**简体中文**，聚焦「为什么」而非罗列文件。

### 3. 推送并创建 PR

```bash
git push -u origin feature/简短描述
gh pr create --title "..." --body "..."
```

PR 描述应说明：关联的规格 / Issue（如有）、变更摘要、自测情况。

### 4. 等待 CI 通过

- PR 页面查看 **Checks** / **Status Checks**
- 全部 ✅ 即 **CI Green**，方可合并
- 若 ❌：**Fix the build** → 推送新 commit → CI 自动重跑；必要时在 Actions 页 **Re-run failed jobs**

### 5. 合并到 main

- 优先使用 **Squash and Merge**（保持 `main` 历史简洁）
- 合并后删除远程功能分支
- 本地同步：

```bash
git checkout main
git pull origin main
git branch -d feature/简短描述
```

## 当前 v2 开发分支

v2 阶段的所有变更——**文档、规格落地与 `/implement` 代码**——统一提交到：

```
feature/v2
```

| 变更类型 | 示例 | 提交到 |
|---------|------|--------|
| 路线图 / 工作流文档 | `CONTRIBUTING.md`、README 路线图链接 | `feature/v2` |
| 规格产出 | research 文档、设计原型、ADR 补充 | `feature/v2` |
| v2 功能代码 | 搜索、挂牌、地址、WorkManager 等 | `feature/v2` |

```bash
git checkout feature/v2
git pull origin feature/v2
# 开发、commit…
git push origin feature/v2
```

v2 全部完成并通过各切片验收后，将 `feature/v2` 以 **一个 PR** 合入 `main`。

> 不要在 `main` 上直接开发 v2；`main` 保持 MVP 可发布基线。

## 与路线图的关系

MVP 已合入 `main`（[#1](https://github.com/Zoti321/c2c-market/issues/1) 关闭、[#11](https://github.com/Zoti321/c2c-market/issues/11) 验收通过）。当前 [v2 路线图 (#19)](https://github.com/Zoti321/c2c-market/issues/19) 跟踪**决策、规格与验收**，不跟踪实现票：

1. **Wayfinder 决策票** — 锁定各切片规格（research / grilling / prototype）
2. **`/implement`** — 在 `feature/v2` 上按规格写代码
3. **PR + CI** — v2 全部完成后开一个 PR 合入 `main`
4. **验收票** — 各切片代码就绪后手工验收

## 分支保护（目标配置）

`main` 分支建议启用 **Branch Protection Rules**：

- 禁止直接 push
- 合并前必须通过 Required Status Checks
- （可选）需要 PR Review

> CI Workflow 配置就绪后，在 GitHub 仓库 Settings → Branches 中启用上述规则。

## 相关文档

- [`README.md`](README.md) — 项目总览
- [`docs/agents/issue-tracker.md`](docs/agents/issue-tracker.md) — Issue 与 Wayfinder 约定
- [`AGENTS.md`](AGENTS.md) — Agent 协作指引
