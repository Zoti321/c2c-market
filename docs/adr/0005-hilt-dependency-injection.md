# 引入 Hilt 依赖注入

项目采用 MVVM + Repository 分层，依赖项（Retrofit、Room、Repository）随功能增长而增多。Hilt 为 Android 官方推荐 DI 方案，与 KSP 配合减少样板代码，便于测试与模块化。分层约束不变：UI 只通过 ViewModel 的 StateFlow 驱动，ViewModel 依赖接口而非 Retrofit / DAO。

**Status**: accepted

**Considered options**: Hilt + KSP（选用）、Koin（拒绝，非官方推荐）、手动 Factory（拒绝，无法 scale）

## DI 图（目标）

```
Screen → @HiltViewModel ViewModel
  └─ Repository ← FakeStoreApi (@Singleton)
                ← Room DAO (@Singleton)
```

## Hilt Module

| Module | `@InstallIn` | 内容 |
|--------|--------------|------|
| `di/NetworkModule.kt` | `SingletonComponent` | Json、OkHttp、Retrofit、FakeStoreApi |
| `di/DatabaseModule.kt` | `SingletonComponent` | Room Database、DAO |
| `di/RepositoryModule.kt` | `SingletonComponent` | `@Binds` Repository 接口 → 实现 |

## 入口

- `C2CApplication` — `@HiltAndroidApp`，Manifest `android:name`
- `MainActivity` — `@AndroidEntryPoint`
- Screen — `hiltViewModel()`
