# 友盟埋点与监控接入说明

## 接入方式

Android 已接入友盟 U-App Maven 依赖：

- `com.umeng.umsdk:common:9.9.2`
- `com.umeng.umsdk:asms:1.8.7.2`

Android 暂不接入 `com.umeng.umsdk:apm:2.0.8`。该版本携带的
`libcrashsdk.so`、`libucrash-core.so` 和 `libucrash.so` 未按 16 KB ELF
LOAD 边界对齐，不满足 targetSdk 35+ 的 Google Play 发布要求。待友盟发布
兼容版本后再恢复 U-APM；当前 U-App 页面与业务事件统计不受影响。

iOS 已补充 `iosApp/Podfile`：

- `UMCommon`
- `UMDevice`
- `UMAPM`

iOS Swift 侧通过 `canImport(UMCommon)` 做条件编译。未安装 Pods 时不影响现有工程编译；执行 `pod install` 并使用 `iosApp.xcworkspace` 后，会启用真实友盟初始化和事件上报。

## 配置

默认不启用上报，避免开发环境误发数据。

### Android

通过 Gradle properties 配置：

```properties
UMENG_ANALYTICS_ENABLED=true
UMENG_ANDROID_APP_KEY=your_android_app_key
UMENG_CHANNEL=official
```

这些值会注入 AndroidManifest：

- `UMENG_APPKEY`
- `UMENG_CHANNEL`
- `UMENG_ANALYTICS_ENABLED`

### iOS

通过 Xcode build settings 或 `project.yml` 覆盖：

- `UMENG_ANALYTICS_ENABLED`
- `UMENG_APP_KEY`
- `UMENG_CHANNEL`

默认值：

- `UMENG_ANALYTICS_ENABLED=false`
- `UMENG_APP_KEY=""`
- `UMENG_CHANNEL=app_store`

## 埋点规范

### 事件命名

所有事件统一使用 `puzzle_` 前缀，例如：

- `puzzle_screen_view`
- `puzzle_screen_duration`
- `puzzle_click`
- `puzzle_game_start`
- `puzzle_game_complete`

规则：

- 事件名使用小写 snake_case。
- 业务事件使用“对象 + 动作”，例如 `game_start`、`piece_place`。
- 通用按钮点击统一使用 `puzzle_click`，通过 `target` 区分按钮。
- 页面进入统一使用 `puzzle_screen_view`。
- 页面离开统一使用 `puzzle_screen_duration`。
- 属性名使用小写 snake_case。
- 属性值统一在 common 层转为字符串，并限制单个值长度，避免上传超长提示词或错误堆栈。

### 页面停留时长

页面停留时长由 `App` 中的导航状态统一记录：

- 页面进入：发送 `puzzle_screen_view`。
- 页面离开：发送 `puzzle_screen_duration`。
- 计时口径：从当前 `Screen` 成为栈顶开始，到该 `Screen` 不再是栈顶为止。
- 上报字段：`screen`、`duration_ms`、`duration_seconds`。

### 按钮点击

关键步骤按钮统一发送 `puzzle_click`：

- `target`：按钮语义，不用中文文案，例如 `start_game`、`open_settings`、`pause_resume`。
- `screen`：按钮所在页面，例如 `menu`、`settings`、`game`。
- 需要区分同一动作不同入口时，补充 `entry_point`，例如 AI 生成页的 `source_card` 和 `prompt_button`。

当前已覆盖的关键按钮：

| 页面 | target |
| --- | --- |
| Menu | `start_game`, `open_theme_picker`, `open_image_source`, `open_settings` |
| ThemePicker | `back`, `select_story`, `confirm_theme` |
| ImageSource | `back`, `image_source_builtin`, `image_source_current_theme`, `image_source_ai_generate` |
| Settings | `back`, `settings_open_image_source`, `toggle_sound`, `toggle_reference` |
| Game | `game_top_pause`, `pause_resume`, `pause_quit`, `cancel_piece_selection`, `position_hint_toggle`, `error_retry`, `go_to_menu`, `play_again`, `celebration_dismiss` |

## 关键路径事件

| 事件 | 触发时机 | 核心属性 |
| --- | --- | --- |
| `puzzle_screen_view` | 页面切换 | `screen`, `source` |
| `puzzle_screen_duration` | 页面离开 | `screen`, `duration_ms`, `duration_seconds` |
| `puzzle_click` | 首页/设置/游戏关键按钮 | `target`, `screen` |
| `puzzle_theme_select` | 选择故事或主题 | `story_id`, `theme_id`, `story_page_index`, `selection_type` |
| `puzzle_story_page_select` | 首页故事分页切换 | `story_id`, `theme_id`, `story_page_index` |
| `puzzle_piece_count_change` | 碎片数量按 10 的步进变化 | `piece_count` |
| `puzzle_game_start` | 普通拼图开始 | `story_id`, `theme_id`, `piece_count`, `source` |
| `puzzle_ai_game_start` | AI 拼图开始 | `story_id`, `theme_id`, `piece_count`, `prompt_length` |
| `puzzle_ai_fallback` | AI 无可用图片回退程序图 | `theme_id`, `piece_count`, `reason` |
| `puzzle_game_ready` | 拼图生成完成，进入可玩状态 | `piece_count`, `image_width`, `image_height`, `grid_cols`, `grid_rows`, `block_size`, `has_piece_bitmaps` |
| `puzzle_piece_place` | 放置碎片成功或错误 | `result`, `piece_count`, `placed_count`, `elapsed_seconds` |
| `puzzle_game_complete` | 拼图完成 | `story_id`, `theme_id`, `piece_count`, `elapsed_seconds` |
| `puzzle_game_error` | 生成或运行错误 | `phase`, `message` |
| `puzzle_game_pause` | 暂停 | `elapsed_seconds`, `placed_count` |
| `puzzle_game_resume` | 继续 | `elapsed_seconds`, `placed_count` |
| `puzzle_game_retry` | 错误页重试 | 无 |
| `puzzle_game_quit` | 返回首页/退出游戏 | `phase`, `piece_count`, `placed_count`, `elapsed_seconds` |
| `puzzle_celebration_dismiss` | 关闭完成庆祝层 | 无 |

## 设计约束

- 不上传 `piece_id`、完整 prompt、图片 URL、用户输入全文等高基数或敏感字段。
- 友盟 SDK 初始化集中在平台入口，业务埋点集中在 common 层。
- Android 使用手动页面采集，页面浏览由 `puzzle_screen_view` 统一记录。
- iOS 在 Swift 入口初始化友盟，同时把 KMP common 事件桥接到 `MobClick.event`。
- 发布前需要完成隐私政策、SDK 清单、用户同意后初始化策略的产品确认。
- 新增按钮时优先接入 `Analytics.click(...)`，不要新增只有按钮文案差异的独立事件。
- 新增业务结果时再扩展 `AnalyticsEvent`，并同步更新本文档。
