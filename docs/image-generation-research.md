# 图片生成能力技术调研与默认故事资源方案

更新日期：2026-06-25

## 目标

1. 在中国区可用的图片生成服务中，构建一套可动态输入 prompt 的生成能力。
2. 为儿童故事拼图准备默认内置故事图片：每个故事拆为 5-6 个关键画面，首批生成并内置一组完整图片。

## 推荐结论

优先级：

1. 阿里云百炼 / 通义万相：适合作为国内主通道。优势是模型体系完整、异步任务协议适合移动端后端转发、官方文档明确。
2. SiliconFlow：适合作为聚合备选通道。优势是 OpenAI-like 的图片生成 API 简洁，便于动态切换模型。
3. 火山方舟 / Seedream：适合作为后续增强通道。优势是视觉质量强，但建议等接入账号、地域、模型版本稳定后通过后端接入。
4. 其它国内云厂商能力：作为候补，不建议直接在客户端做多套深耦合接入。

产品实现建议：

- App 客户端只保留 `AIImageProvider` 抽象和 mock/procedural fallback。
- API Key、服务商选择、内容审核、下载缓存、失败重试都放到服务端。
- 客户端只发送 storyId/pageId/prompt/style/size，不直接保存长期密钥。
- 生成结果必须落到图片 URL 或本地缓存，再进入现有 Rust/native splitter。

## 服务调研

### 阿里云百炼 / 通义万相

官方文档：<https://help.aliyun.com/zh/model-studio/text-to-image-api-reference>

适配要点：

- 支持通过 DashScope/Bailian API 生成图片。
- 最新文档中的调用结构包含 `model`、`input.messages`、`parameters`。
- 异步任务形态适合服务端轮询，移动端可只拿最终图片 URL。
- 本工程已新增 `TongyiImageProvider`，支持 prompt、negativePrompt、size、batchSize、seed。

推荐模型策略：

- 默认：`wan2.6-t2i`
- 儿童故事图：1024x1024，关闭水印，开启 prompt 扩展。
- 失败策略：60 秒超时，回落到内置/程序生成图。

### SiliconFlow

官方文档：<https://docs.siliconflow.cn/cn/api-reference/images/images-generations>

适配要点：

- `/v1/images/generations` 接口结构简单。
- 支持 `model`、`prompt`、`negative_prompt`、`image_size`、`batch_size`。
- 适合作为聚合通道和快速模型切换层。
- 本工程已新增 `SiliconFlowImageProvider`。

推荐模型策略：

- 默认可配置为 `Kwai-Kolors/Kolors` 或后续平台支持的 Qwen-Image 类模型。
- 由后端下发当前可用模型，不在客户端写死业务决策。

### 火山方舟 / Seedream

官方入口：<https://www.volcengine.com/docs/82379/1541523>

适配要点：

- 适合后续作为高质量图像生成增强通道。
- 建议在服务端通过统一 `ImageGenerationRequest` 做适配，客户端无需新增分支。
- 接入前需要确认账号地域、模型 ID、计费和内容安全策略。

## 工程实现

新增/更新文件：

- `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/AIImageGenerator.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/TongyiImageProvider.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/ai/SiliconFlowImageProvider.kt`
- `composeApp/src/commonMain/kotlin/com/puzzle/game/data/BuiltinStoryImageCatalog.kt`

核心请求模型：

```kotlin
data class ImageGenerationRequest(
    val prompt: String,
    val negativePrompt: String = CHILD_SAFE_NEGATIVE_PROMPT,
    val size: String = "1024x1024",
    val batchSize: Int = 1,
    val seed: Int? = null
)
```

儿童安全负向提示词：

```text
恐怖，血腥，暴力，武器，危险动作，成人内容，惊吓表情，阴暗压抑，低清晰度，文字，水印，畸形肢体，怪异面部
```

后端建议返回结构：

```json
{
  "id": "story-page-generation-id",
  "provider": "tongyi",
  "model": "wan2.6-t2i",
  "prompt": "...",
  "imageUrl": "https://...",
  "width": 1024,
  "height": 1024,
  "safetyStatus": "pass"
}
```

## 默认故事设计

首批故事：

### 月光花园的小灯塔

年龄：4-7 岁

核心能力：观察画面、理解顺序、合作意识、温和解决问题。

内置图片资源：

- `stories/moon-garden/moon-garden-01.png`：种子醒来了
- `stories/moon-garden/moon-garden-02.png`：迷路的小蜗牛
- `stories/moon-garden/moon-garden-03.png`：花瓣桥
- `stories/moon-garden/moon-garden-04.png`：影子不见了
- `stories/moon-garden/moon-garden-05.png`：点亮灯塔
- `stories/moon-garden/moon-garden-06.png`：回家的路

### 云朵面包店

年龄：3-6 岁

核心能力：分享、颜色识别、食物与天气想象。

状态：已完成 6 幕 prompt，未批量生成。

### 星星邮差

年龄：5-8 岁

核心能力：倾听、情绪理解、空间线索推理。

状态：已完成 6 幕 prompt，未批量生成。

## 生成规范

统一正向风格：

```text
儿童绘本插画，温暖明亮，柔和几何建筑感，低刺激色彩，清晰主体，适合3-8岁儿童，无文字，无水印，无恐怖元素，1024x1024
```

拼图适配要求：

- 主体轮廓清楚，避免大片空白。
- 前景、中景、背景有明显分层，方便儿童观察。
- 不要在图里生成文字，因为切割后文字会变成干扰。
- 色彩丰富但低刺激，避免高对比闪烁感。
- 每张图必须能单独理解，也能串成完整故事。

## 后续实施建议

1. 增加后端 `/image-generation/jobs` 接口，不把密钥放在 App。
2. App 增加 prompt 输入页：主题、故事类型、画面风格、年龄段、生成按钮。
3. 图片生成完成后进入预览页，用户确认后再切割。
4. 内置故事页从 `BuiltinStoryImageCatalog` 驱动，而不是散落在 `ThemePresets`。
5. 将 `stories/*/*.png` 纳入资源压缩策略，必要时改为 WebP。
