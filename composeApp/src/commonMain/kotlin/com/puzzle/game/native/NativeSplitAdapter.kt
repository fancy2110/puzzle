package com.puzzle.game.native

import androidx.compose.ui.graphics.ImageBitmap
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.readFileBytes
import com.puzzle.logger.PuzzleLog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class NativeSplitAdapter {

    var pieces: List<PuzzlePiece> = emptyList(); private set
    var gridCols: Int = 0; private set
    var gridRows: Int = 0; private set
    var imageSize: Pair<Int, Int> = Pair(0, 0); private set
    var correctPositions: Map<String, Pair<Int, Int>> = emptyMap(); private set

    /** Loaded piece images keyed by piece id. */
    var pieceBitmaps: Map<String, ImageBitmap> = emptyMap(); private set

    private var engine: NativePuzzleEngine? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun loadAndSplit(imageBytes: ByteArray, pieceCount: Int, blockSize: Int = 64, tempDir: String? = null): Boolean {
        close()

        val eng = NativePuzzleEngine()
        if (!eng.isAvailable) { PuzzleLog.w("NativeSplit", "lib not available"); return false }
        if (!eng.loadImage(imageBytes)) {
            val err = eng.lastError() ?: "unknown"; eng.close()
            PuzzleLog.e("NativeSplit", "loadImage: $err"); return false
        }

        val result = eng.split(pieceCount, blockSize) ?: run {
            val err = eng.lastError() ?: "unknown"; eng.close()
            PuzzleLog.e("NativeSplit", "Split: $err"); return false
        }

        pieces = result.pieces.map { np ->
            PuzzlePiece(
                id = np.id,
                pixels = com.puzzle.game.engine.model.Rect(np.pixel_left, np.pixel_top,
                    np.pixel_left + np.pixel_width, np.pixel_top + np.pixel_height),
                blocks = np.block_positions.let { bps ->
                    if (bps.isEmpty()) com.puzzle.game.engine.model.Rect()
                    else com.puzzle.game.engine.model.Rect(bps.minOf { it.x }, bps.minOf { it.y },
                        bps.maxOf { it.x }, bps.maxOf { it.y })
                },
                items = np.block_positions.map { com.puzzle.game.engine.model.Position(it.y, it.x) }.toMutableList(),
                isPlaced = false
            )
        }.shuffled()

        correctPositions = pieces.mapNotNull { p ->
            if (p.items.isEmpty()) null
            else { val c = p.items[p.items.size / 2]; p.id to Pair(c.y, c.x) }
        }.toMap()

        gridCols = result.grid_cols; gridRows = result.grid_rows
        imageSize = Pair(result.image_width, result.image_height)
        engine = eng

        PuzzleLog.i("NativeSplit", "Split OK: ${result.image_width}×${result.image_height}px " +
            "grid=${result.grid_cols}×${result.grid_rows} bs=${result.block_size} pieces=${pieces.size}")

        // Save PNGs to cache and load them back as bitmaps
        if (tempDir != null) {
            val jsonStr = eng.savePieces(tempDir, blockSize)
            if (jsonStr != null) {
                PuzzleLog.i("NativeSplit", "Pieces saved to: $tempDir ($jsonStr)")
                pieceBitmaps = loadBitmapsFromCache(tempDir, jsonStr)
                PuzzleLog.i("NativeSplit", "Loaded ${pieceBitmaps.size} piece bitmaps")
            } else {
                PuzzleLog.w("NativeSplit", "savePieces failed: ${eng.lastError() ?: "unknown"}")
            }
        }

        return true
    }

    private fun loadBitmapsFromCache(dir: String, jsonStr: String): Map<String, ImageBitmap> {
        return try {
            val filenames = json.parseToJsonElement(jsonStr).jsonArray
            filenames.mapNotNull { el ->
                val name = el.jsonPrimitive.content
                val path = "$dir/$name"
                val bytes = readFileBytes(path)
                if (bytes != null) {
                    val bmp = decodeToImageBitmap(bytes)
                    if (bmp != null) name.removeSuffix(".png") to bmp else null
                } else null
            }.toMap()
        } catch (e: Exception) {
            PuzzleLog.w("NativeSplit", "Failed to load bitmaps: ${e.message}")
            emptyMap()
        }
    }

    fun close() { engine?.close(); engine = null; pieces = emptyList(); pieceBitmaps = emptyMap() }
}
