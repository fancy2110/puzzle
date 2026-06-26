# 友盟埋点与监控接入说明

## 接入方式

Android 已接入友盟 U-App 与 U-APM Maven 依赖：

- `com.umeng.umsdk:common:9.9.2`
- `com.umeng.umsdk:asms:1.8.7.2`
- `com.umeng.umsdk:apm:2.0.8`

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

## 事件命名

所有事件统一使用 `puzzle_` 前缀，例如：

- `puzzle_screen_view`
- `puzzle_game_start`
- `puzzle_game_complete`

属性值统一在 common 层转为字符串，并限制单个值长度，避免上传超长提示词或错误堆栈。

## 关键路径事件

| 事件 | 触发时机 | 核心属性 |
| --- | --- | --- |
| `puzzle_screen_view` | 页面切换 | `screen`, `source` |
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

