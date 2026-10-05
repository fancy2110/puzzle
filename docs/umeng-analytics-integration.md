# 埋点与监控规范

## 1. 目标与现状

本文是故事拼图 App 的埋点、运行监控和友盟 SDK 接入基线。目标是回答四类问题：

1. 用户是否顺利完成“选择故事 -> 选择画面 -> 开始拼图 -> 完成画面 -> 继续故事”。
2. 拼图难度、提示功能和交互方式是否影响完成率。
3. 图片读取、AI 生成、图片解码和切割分别耗时多久、在哪里失败。
4. Android 与 iOS 的统计口径是否一致，是否满足隐私和应用商店要求。

当前接入状态：

| 平台 | 已接入 | 状态 |
| --- | --- | --- |
| Android | U-App `common:9.9.2`、`asms:1.8.7.2` | 已接入，默认关闭 |
| Android | U-APM | 未接入；`apm:2.0.8` 的 native 库未通过 16 KB ELF 对齐检查 |
| iOS | `UMCommon`、`UMDevice`、`UMAPM` | Pod 条件接入，默认关闭 |
| Common | 业务事件、处理错误、阶段耗时 | 已接入 |

## 2. 数据模型

所有事件使用 `puzzle_` 前缀和 snake_case，例如 `puzzle_game_start`。

### 2.1 自动公共属性

| 属性 | 含义 |
| --- | --- |
| `session_id` | 本次 App 进程会话 ID；随机生成，不是用户标识 |
| `schema_version` | 当前事件结构版本，现为 `2` |
| `language` | 当前界面语言 |

### 2.2 拼图公共属性

所有游戏、故事进度和游戏监控事件应包含：

| 属性 | 含义 |
| --- | --- |
| `game_run_id` | 单次拼图尝试 ID |
| `parent_game_run_id` | 重试前一次尝试 ID；非重试为空 |
| `story_id` | 故事 ID |
| `story_page_index` | 当前画面序号，从 0 开始 |
| `story_page_count` | 故事画面总数 |
| `theme_id` | 当前画面/主题 ID |
| `piece_count` | 碎片数量 |
| `source` | `builtin_asset`、`procedural`、`ai_generated` 或 `external_image` |
| `retry_count` | 当前画面的重试次数 |

禁止上报：完整 prompt、图片 URL、本地路径、碎片 ID、儿童姓名、设备通讯录、异常堆栈和用户输入全文。

属性值最长 128 字符，单事件最多保留 24 个属性。错误信息使用稳定的 `error_code`，不上传原始异常消息。

## 3. 核心漏斗

### 3.1 启动与首屏

```text
puzzle_app_launch
  -> puzzle_screen_view(screen=splash)
  -> puzzle_app_ready(startup_ms)
  -> puzzle_screen_view(screen=menu)
```

`startup_ms` 从 `App` 首次组合开始计算，到启动页结束为止。

### 3.2 故事选择

```text
puzzle_click(target=open_theme_picker)
  -> puzzle_theme_select(selection_type=story)
  -> puzzle_click(target=confirm_theme)
  -> puzzle_story_page_select
```

首页 Pager 切换画面使用 `puzzle_story_page_select`，不要用通用点击事件替代。

### 3.3 拼图主漏斗

```text
puzzle_game_start
  -> puzzle_monitor_timing(operation=asset_load/image_decode/*_split)
  -> puzzle_game_ready(prepare_duration_ms)
  -> puzzle_piece_place(result=success, 首块)
  -> puzzle_game_progress(25/50/75/100)
  -> puzzle_game_complete
  -> puzzle_story_scene_complete
  -> puzzle_story_complete（仅最后一幕）
```

未完成路径：

```text
puzzle_game_error -> puzzle_game_retry -> 新 game_run_id
puzzle_game_pause -> puzzle_game_resume
puzzle_game_quit
```

## 4. 事件字典

### 4.1 应用与页面

| 事件 | 触发时机 | 关键属性 |
| --- | --- | --- |
| `puzzle_app_launch` | 每次进程会话一次 | 公共属性 |
| `puzzle_app_ready` | 启动页结束，每次会话一次 | `startup_ms` |
| `puzzle_screen_view` | 页面成为栈顶 | `screen`, `source` |
| `puzzle_screen_duration` | 页面离开栈顶 | `screen`, `duration_ms`, `duration_seconds` |
| `puzzle_click` | 关键命令按钮 | `target`, `screen` |

页面 ID：`splash`、`menu`、`theme_picker`、`image_source`、`settings`、`game`。

### 4.2 故事与配置

| 事件 | 触发时机 | 关键属性 |
| --- | --- | --- |
| `puzzle_theme_select` | 选择故事或主题 | `selection_type`, `story_id`, `theme_id` |
| `puzzle_story_page_select` | Pager 切换画面 | `story_id`, `story_page_index`, `theme_id` |
| `puzzle_piece_count_change` | 数量跨越 10 的步进 | `piece_count` |
| `puzzle_story_scene_complete` | 完成一幕 | 游戏公共属性、完成耗时、错误次数 |
| `puzzle_story_complete` | 完成最后一幕 | 游戏公共属性、完成耗时、错误次数 |

### 4.3 拼图

| 事件 | 触发时机 | 关键属性 |
| --- | --- | --- |
| `puzzle_game_start` | 普通、AI、外部图片均统一进入主漏斗 | 游戏公共属性 |
| `puzzle_ai_game_start` | AI 请求开始 | 游戏公共属性、`prompt_length` |
| `puzzle_ai_fallback` | AI 结果不可用，回退程序图 | `reason`，仅使用枚举值 |
| `puzzle_game_ready` | 图片和碎片准备完成 | `prepare_duration_ms`, 图片尺寸、网格尺寸 |
| `puzzle_piece_place` | 首块、最后一块或错误放置 | `result`, `input_method`, `placed_count`, `wrong_attempt_count` |
| `puzzle_game_progress` | 首次达到 25/50/75/100% | `progress_percent`, `elapsed_seconds` |
| `puzzle_game_complete` | 所有碎片正确 | `elapsed_seconds`, `total_attempt_count` |
| `puzzle_game_pause` | 从进行态暂停 | `placed_count`, `elapsed_seconds` |
| `puzzle_game_resume` | 从暂停态继续 | `placed_count`, `elapsed_seconds` |
| `puzzle_game_retry` | 错误页重试 | `retry_count`, 原 `game_run_id` |
| `puzzle_game_quit` | 生成、游戏或错误状态退出 | `phase`, `placed_count`, `wrong_attempt_count` |
| `puzzle_celebration_dismiss` | 关闭完成庆祝层 | 游戏公共属性 |

正确放置不逐片全量上报。最多 300 片时，全量事件会增加成本并污染分析；当前只保留首块、里程碑和最后一块。错误放置仍逐次上报，以评估难度与提示效果。

## 5. 监控事件

### 5.1 耗时

统一事件：`puzzle_monitor_timing`。

| `component` | `operation` | 说明 |
| --- | --- | --- |
| `game` | `asset_load` | 内置故事图片读取 |
| `game` | `ai_generate` | AI 服务生成 |
| `game` | `ai_image_download` | AI 图片下载 |
| `game` | `image_decode` | PNG/JPEG 解码 |
| `game` | `kotlin_split` | Kotlin 碎片切割 |
| `game` | `native_split` | Native 碎片切割 |
| `game` | `procedural_generate_split` | 程序图生成与切割 |
| `game` | `prepare_total` | 点击开始到可交互总耗时 |

建议告警线：

- `app_ready.startup_ms` P95 > 2500 ms。
- `prepare_total` P95 > 10 秒。
- `monitor_error / game_start` > 1%。
- `game_ready / game_start` < 98%。
- `game_complete / game_ready` 按 `piece_count` 分组持续下降。

### 5.2 处理错误

统一事件：`puzzle_monitor_error`，字段为 `component`、`operation`、`error_code`、`fatal=false`。

当前覆盖：

- 内置资源读取失败：`resource/read_builtin_asset/resource_not_found`。
- 游戏准备失败：`game/prepare_game/generation_failed`。
- 图片处理失败：`game/image_decode/image_processing_failed`。
- AI 图片下载失败：`network/ai_image_download/download_failed`。
- AI 回退失败：`game/ai_fallback/procedural_fallback_failed`。

该事件只覆盖可恢复错误，不等价于崩溃、ANR、OOM 和 native crash 监控。

## 6. 关键按钮

| 页面 | `target` |
| --- | --- |
| Menu | `start_game`, `open_theme_picker`, `open_image_source`, `open_settings` |
| ThemePicker | `back`, `select_story`, `confirm_theme` |
| ImageSource | `back`, `image_source_builtin`, `image_source_current_theme`, `image_source_ai_generate` |
| Settings | `back`, `settings_open_image_source`, `toggle_sound`, `toggle_reference`, `change_language` |
| Game | `game_top_pause`, `pause_resume`, `pause_quit`, `cancel_piece_selection`, `position_hint_toggle`, `error_retry`, `go_to_menu`, `next_story_page`, `choose_another_story`, `celebration_dismiss` |

## 7. 平台配置

默认关闭上报，避免开发包误发数据。

Android `gradle.properties`：

```properties
UMENG_ANALYTICS_ENABLED=true
UMENG_ANDROID_APP_KEY=your_android_app_key
UMENG_CHANNEL=official
```

iOS Build Settings：

```text
UMENG_ANALYTICS_ENABLED=true
UMENG_APP_KEY=your_ios_app_key
UMENG_CHANNEL=app_store
```

合规初始化（已实现）：App 启动时只做“预初始化”——Android 端 `PlatformAnalytics.attach()` 仅读取 manifest 配置，iOS 端只注册 Kotlin 回调钩子，均不启动友盟 SDK。首次启动展示监护人同意门（`ConsentGate`，系统返回键不可绕过），同意后调用 `PlatformAnalytics.initialize()` 正式初始化并记录同意版本（`privacy_consent_version`），拒绝则不初始化、不上报并退出 App（iOS 仅提供返回同意路径）。隐私政策以简中/繁中/英文/日文四种语言打包在应用内，并在设置页提供入口。政策版本变更（`PrivacyConsent.VERSION`）会再次触发同意门。

待办：正式发布前仍需法务确认监护人同意文案、SDK 清单公示页和儿童个人信息保护规则。

## 8. 友盟 SDK 调研与决策

友盟当前产品矩阵包括 U-App、U-Push、U-Verify、U-Share、U-Link 和 App 性能监控。结合本产品现状：

| SDK | 价值 | 建议 | 原因与前置条件 |
| --- | --- | --- | --- |
| U-App | 高 | 保留并完善 | 适合故事漏斗、难度、留存和功能使用分析 |
| U-APM | 高 | iOS 保留；Android 暂缓 | 崩溃、ANR、OOM、启动与网络性能很有价值；Android 当前最新版 native 库未通过 16 KB 对齐 |
| U-Push | 中 | 有家长模式和内容运营后接入 | 可用于新故事和亲子共读提醒；必须有通知授权、家长控制、频控、服务端推送策略和厂商通道配置 |
| U-Share | 低到中 | 暂不接入 | 仅“家长分享完成卡片”有价值；先使用系统分享面板，减少第三方 SDK 与隐私面 |
| U-Link | 低 | 活动投放后评估 | 需要深链路由、落地页和渠道归因，目前产品没有对应入口 |
| U-Verify | 低 | 不接入 | 当前无账号和手机号登录；儿童 App 不应为非必要功能收集手机号 |
| 广告/变现 SDK | 不适合 | 不接入 | 与当前儿童故事体验和最小化采集原则冲突 |

推荐顺序：

1. 先完成 U-App 数据质量、隐私同意和仪表盘。
2. 等友盟 Android U-APM 发布 16 KB 兼容版本后重新做 APK/AAB ELF 检查，再恢复崩溃与 ANR 监控。
3. 产品具备家长模式、服务端内容运营和通知策略后，再评估 U-Push。
4. 分享优先走 Android Sharesheet / iOS ShareLink，不急于引入 U-Share。

## 9. 验证清单

发布前使用友盟集成测试和本地日志逐项验证：

1. 每个事件只出现一次，`session_id` 在一次进程中稳定。
2. 重试生成新的 `game_run_id`，并通过 `parent_game_run_id` 关联。
3. 普通、AI、外部图片都产生统一的 `game_start -> game_ready`。
4. 25/50/75/100 里程碑不重复。
5. 完成最后一幕时同时出现 `story_scene_complete` 和 `story_complete`。
6. 已完成后返回首页不产生错误的 `game_quit`。
7. 事件中没有 prompt、URL、路径、碎片 ID 和异常堆栈。
8. Android APK/AAB 中所有 arm64-v8a、x86_64 ELF 通过 16 KB 对齐检查。
9. 用户拒绝隐私政策时，友盟 SDK 不正式初始化、不上报。
10. 首次启动必现同意门；同意版本与当前 `PrivacyConsent.VERSION` 一致时再次启动不再展示。
11. 同意门展示期间日志中不存在任何 `puzzle_` 事件或友盟初始化记录。

## 10. 官方参考

- [友盟开发者中心与产品矩阵](https://developer.umeng.com/docs/67963/detail/2930256)
- [U-Verify 产品与平台接入范围](https://developer.umeng.com/docs/143070/cate/143070)
- [友盟稳定性监控能力](https://developer.umeng.com/docs/119267/detail/194578)
- [友盟合规初始化原则](https://developer.umeng.com/docs/147377/detail/2847371)
- [Android 16 KB 页面兼容要求](https://developer.android.com/guide/practices/page-sizes)
