package com.puzzle.game.engine

import java.io.File
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis
import kotlin.test.Test

/**
 * Host-JVM benchmark for the Kotlin image splitter.
 * Output is written to stdout; see system-out in the test XML report.
 */
class ImageSplitterBenchmarkTest {
    private val imagePath = File(
        "src/commonMain/composeResources/files/stories/puss-boots/puss-boots-01.jpg"
    )

    private fun bench(label: String, runs: Int = 5, warmup: Int = 2, block: () -> Unit) {
        repeat(warmup) { block() }
        val times = List(runs) { measureTimeMillis { block() } }
        println("BENCH | $label | first=${times.first()}ms | best=${times.min()}ms | all=$times")
    }

    @Test
    fun benchmark() {
        // 1. Decode
        var width = 1024
        var height = 1024
        bench("decode ImageIO JPEG 1024") {
            val img = ImageIO.read(imagePath)
            width = img.width
            height = img.height
        }

        for (pieceCount in listOf(120, 300)) {
            // 2a. BFS ownership, block_size=1
            bench("BFS ownership bs=1 pieces=$pieceCount") {
                ImageSplitter(width, height, 1).split(pieceCount, includeBlocks = true)
            }
        }

        // 2b. Coarse grid
        bench("BFS ownership bs=64 pieces=120") {
            ImageSplitter(width, height, 64).split(120, includeBlocks = true)
        }

        // 3. Shipping runtime path: Voronoi-style outline split (includeBlocks=false)
        for (pieceCount in listOf(120, 300, 1000)) {
            bench("Voronoi outlines (shipping) pieces=$pieceCount") {
                ImageSplitter(width, height, 1).split(pieceCount, includeBlocks = false)
            }
        }
    }
}
