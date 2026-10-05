# 图片切割算法 Rust vs Kotlin 调研

> 决策落地：2026-10-05 已按本报告结论移除全部 Rust / native 代码
> （`native/`、JNI `.so`、cinterop、Gradle native 任务），仅保留 Kotlin 实现。
> 本文作为决策记录留存。

日期：2026-10-05
测试素材：1024×1024 JPEG（`puss-boots-01.jpg`）
测试环境：Apple Silicon host，Rust release profile；Rust 与 Kotlin 使用对等算法。

## 1. 实测数据

| 环节 | Rust (native) | Kotlin/JVM |
| --- | --- | --- |
| JPEG 解码 | 4.4ms | 8ms |
| BFS 归属 bs=1，120 片 | 19.8ms | 40ms |
| BFS 归属 bs=1，300 片 | 19.9ms | 40ms |
| BFS bs=64，120 片 | 21µs | <1ms |
| JSON 序列化 | 15.9ms | — |
| JSON payload | 18.3MB（约 100 万 block_position） | — |
| Kotlin 解析 JSON（FFI 回程） | — | 95–103ms |
| `save_pieces` 栅格化+PNG | 33ms(bs=64) / 62ms(bs=1) | 无对应 |
| 线上 Voronoi 轮廓切割 | 未实现 | 1–2ms(120/300 片)，11ms(1000 片) |

复现方式（历史命令，代码已移除，无法再运行）：

```bash
# Rust 端
cd native/puzzle-core && cargo run --release --example bench
# JVM 端（bench 会先把 bs=1 JSON 写到 /tmp/rust_split_bs1.json）
./gradlew :composeApp:testDebugUnitTest \
  --tests "com.puzzle.game.engine.ImageSplitterBenchmarkTest"
# 结果在 composeApp/build/test-results/testDebugUnitTest/*.xml 的 system-out
```

## 2. 分析

1. 纯 BFS 计算 Rust 快约 2 倍（20ms vs 40ms），但线上运行路径
   （`includeBlocks=false` 的 Voronoi 半平面裁剪，只产出多边形顶点）
   在 Kotlin 上仅需 1–2ms，Rust 引擎没有实现该路径，两者不在同一量级。
2. bs=1 时 native 全链路 ≈ decode 4ms + split 20ms + JSON 序列化 16ms +
   18MB 字符串跨边界拷贝 + Kotlin 解析 95–103ms（另分配约 100 万个小对象）
   ≈ 140ms+，比纯 Kotlin BFS（40ms）慢 3 倍，比线上 Voronoi（2ms）慢约 70 倍。
   FFI 的 JSON 编组反噬全部收益。
3. Rust 真正的长板是像素级栅格化与 PNG 编码（`save_pieces`）；当前 Compose
   以多边形 Path 裁剪共享原图，不需要逐碎片位图，该优势用不上。
4. 若改用 DirectByteBuffer + 扁平 int 数组（约 4MB owner 网格）替代 JSON，
   native 路径可降至约 25ms、对 Kotlin BFS 取得约 1.6–2 倍优势，但相对线上
   2ms Voronoi 仍是劣化，且需长期维护 JNI / cinterop / xcframework 工具链。

## 2a. 对齐实验：Rust 移植线上 Voronoi 算法

把 `ImageSplitter.splitShardOutlines`（抖动网格种子 + top-36 近邻 +
半平面裁剪）逐行移植为 `examples/voronoi.rs`。该算法只需要图片宽高，
不需要解码像素，FFI 签名可改为 `(width, height, piece_count)`。

| 片数 | Rust split | JSON 序列化 | payload | Kotlin 解析 | Kotlin split |
| --- | --- | --- | --- | --- | --- |
| 120 | **0.38ms** | 16µs | 14.5 KB | <1ms | 1ms |
| 300 | 1.64ms | 44µs | 36.7 KB | <1ms | **1ms** |
| 1000 | 15.3ms | 0.17ms | 123 KB | <1ms | **11ms** |

结论：

1. FFI 编组问题消失：payload 从 18MB 降到 15–123KB，序列化与解析均 <1ms。
2. 纯计算两边基本打平：120 片 Rust 快（0.38 vs 1ms），300/1000 片
   HotSpot C2 反而更快（JIT 对紧凑浮点循环很强）。差异都在 1–2ms 内，
   且只是准备阶段的一次性开销，用户不可感知。
3. 注意 host 基准的局限：HotSpot ≠ Android ART；iOS 端是无 JIT 的
   Kotlin/Native，Rust 在 iOS 上更可能拿到真实优势（估计 1.5–2x），
   但绝对耗时仍是个位数 ms。
4. 维护成本：同一算法两份实现（含随机数/裁剪细节），后续算法演进要改两处。

## 3. 结论与建议

1. **维持 Kotlin 实现，不做 Rust 迁移**。即使把 Rust 对齐到线上 Voronoi
   算法（消除 FFI 编组问题），两边计算也只打平或互有 1ms 级胜负，无可感知收益。
2. 不要在 bs=1 下启用现有 native JSON 路径。
3. 若未来确需 native（例如 iOS 端 Kotlin/Native 性能、2000+ 片、大图，
   或服务端 / 构建期统一切割），正确姿势是移植 Voronoi 轮廓算法、
   FFI 只传宽高与多边形，而非逐像素 BFS + JSON。
4. puzzle-core 的保留场景：
   - 未来若需要逐碎片真实位图（异形阴影、物理边缘），改用二进制扁平缓冲，
     并优先离线预生成；
   - 服务端 / 构建期批量素材生产（解码 + 切割 + PNG 编码管线），避开设备端 FFI。
5. 可选优化：为 FFI 增加二进制通道（int32 owner 网格直传），优先级低。
