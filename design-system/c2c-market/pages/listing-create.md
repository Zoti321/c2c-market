# Create Listing Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** [约定 v2.2 发布挂牌规格](https://github.com/Zoti321/c2c-market/issues/22)

## Layout

- `Scaffold` + `TopAppBar`「发布挂牌」/「编辑挂牌」+ 返回
- 单列 `Column` + `verticalScroll`
- 顺序：图片选择区（1:1 虚线框 + 「选择图片」）→ 标题 → 价格 → 分类 Dropdown → 描述
- 底部固定或表单末 **Primary Button**「发布」/「保存」（`tertiary` 绿，同 MVP CTA）

## Components

- 图片预览：`AsyncImage`（Coil）+ 圆角 12dp
- 分类：M3 `ExposedDropdownMenuBox`，选项同 FakeStore 四分类中文名
- 校验失败：`SupportingText` error 色

## States

- 提交中：按钮 disabled + 小 Indicator
- 成功：Snackbar + pop

## Navigation

- 入口：我的 Tab「发布挂牌」
- 编辑：`listing/edit/{catalogId}`
