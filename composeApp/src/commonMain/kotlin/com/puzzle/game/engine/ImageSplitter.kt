package com.puzzle.game.engine

import com.puzzle.game.engine.model.Block
import com.puzzle.game.engine.model.Position
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.engine.model.Rect
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class ImageSplitter(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val blockSize: Int = PuzzleConfig.PIXEL_BLOCK_SIZE
) {
    private val columns: Int = ceilDiv(imageWidth, blockSize)
    private val rows: Int = ceilDiv(imageHeight, blockSize)
    private val cellCount: Int = rows * columns

    fun split(pieceCount: Int, includeBlocks: Boolean = true): Pair<List<List<Block>>, List<PuzzlePiece>> {
        if (imageWidth <= 0 || imageHeight <= 0 || pieceCount <= 0 || cellCount <= 0) {
            return emptyList<List<Block>>() to emptyList()
        }

        if (!includeBlocks) {
            return emptyList<List<Block>>() to splitShardOutlines(pieceCount)
        }

        return splitBlockOwnership(pieceCount)
    }

    private fun splitBlockOwnership(pieceCount: Int): Pair<List<List<Block>>, List<PuzzlePiece>> {
        val seedCount = pieceCount.coerceAtMost(cellCount)
        val owner = IntArray(cellCount) { -1 }
        val pieces = MutableList(seedCount) { index -> FastPiece(index, keepItemCoordinates = true) }
        val queue = IntArray(cellCount)
        var head = 0
        var tail = 0

        for ((pieceIndex, cell) in sampleSeedCells(seedCount).withIndex()) {
            owner[cell] = pieceIndex
            pieces[pieceIndex].add(cell)
            queue[tail++] = cell
        }

        var remaining = cellCount - seedCount
        while (head < tail && remaining > 0) {
            val cell = queue[head++]
            val pieceIndex = owner[cell]
            val row = cell / columns
            val col = cell - row * columns
            val startDirection = Random.nextInt(4)

            var claimed = 0
            for (step in 0 until 4) {
                val next = neighborCell(row, col, (startDirection + step) and 3)
                if (next >= 0 && owner[next] < 0) {
                    owner[next] = pieceIndex
                    pieces[pieceIndex].add(next)
                    queue[tail++] = next
                    remaining--
                    claimed++
                    if (claimed == 2) break
                }
            }
        }

        if (remaining > 0) {
            var cursor = 0
            for (cell in 0 until cellCount) {
                if (owner[cell] < 0) {
                    while (pieces[cursor].count == 0) {
                        cursor = (cursor + 1) % seedCount
                    }
                    owner[cell] = cursor
                    pieces[cursor].add(cell)
                    cursor = (cursor + 1) % seedCount
                }
            }
        }

        val puzzlePieces = pieces.map { it.toPuzzlePiece(blockSize, columns, imageWidth, imageHeight) }.shuffled()
        return buildBlocks() to puzzlePieces
    }

    private fun splitShardOutlines(pieceCount: Int): List<PuzzlePiece> {
        val seedCount = pieceCount.coerceAtMost(max(1, imageWidth * imageHeight))
        val seeds = sampleShardSeeds(seedCount)
        val neighborLimit = 36.coerceAtMost((seedCount - 1).coerceAtLeast(0))
        val pieces = ArrayList<PuzzlePiece>(seedCount)

        for (index in seeds.indices) {
            val seed = seeds[index]
            var polygon = imageBoundsPolygon()
            val neighbors = nearestSeedIndices(index, seeds, neighborLimit)

            for (neighborIndex in neighbors) {
                polygon = clipToCloserHalfPlane(polygon, seed, seeds[neighborIndex])
                if (polygon.size < 3) break
            }

            if (polygon.size < 3) {
                polygon = fallbackShardPolygon(seed, seedCount)
            }

            pieces.add(polygon.toPuzzlePiece(index))
        }

        return pieces.shuffled()
    }

    private fun sampleShardSeeds(seedCount: Int): List<ShardPoint> {
        val columns = ceil(kotlin.math.sqrt(seedCount.toDouble())).toInt().coerceAtLeast(1)
        val rows = ceil(seedCount.toDouble() / columns.toDouble()).toInt().coerceAtLeast(1)
        val cellWidth = imageWidth.toFloat() / columns.toFloat()
        val cellHeight = imageHeight.toFloat() / rows.toFloat()
        val seeds = ArrayList<ShardPoint>(seedCount)

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (seeds.size == seedCount) return seeds
                val jitterX = (Random.nextFloat() - 0.5f) * cellWidth * 0.72f
                val jitterY = (Random.nextFloat() - 0.5f) * cellHeight * 0.72f
                val x = ((col + 0.5f) * cellWidth + jitterX).coerceIn(0.5f, imageWidth - 0.5f)
                val y = ((row + 0.5f) * cellHeight + jitterY).coerceIn(0.5f, imageHeight - 0.5f)
                seeds.add(ShardPoint(x, y))
            }
        }

        return seeds
    }

    private fun imageBoundsPolygon(): MutableList<ShardPoint> {
        return mutableListOf(
            ShardPoint(0f, 0f),
            ShardPoint(imageWidth.toFloat(), 0f),
            ShardPoint(imageWidth.toFloat(), imageHeight.toFloat()),
            ShardPoint(0f, imageHeight.toFloat())
        )
    }

    private fun fallbackShardPolygon(seed: ShardPoint, seedCount: Int): MutableList<ShardPoint> {
        val approximateSide = kotlin.math.sqrt((imageWidth * imageHeight).toDouble() / seedCount.toDouble()).toFloat()
        val radiusX = (approximateSide * 0.45f).coerceAtLeast(1f)
        val radiusY = (approximateSide * 0.45f).coerceAtLeast(1f)
        val left = (seed.x - radiusX).coerceIn(0f, imageWidth.toFloat())
        val top = (seed.y - radiusY).coerceIn(0f, imageHeight.toFloat())
        val right = (seed.x + radiusX).coerceIn(left + 1f, imageWidth.toFloat())
        val bottom = (seed.y + radiusY).coerceIn(top + 1f, imageHeight.toFloat())

        return mutableListOf(
            ShardPoint(left, top),
            ShardPoint(right, top + radiusY * 0.22f),
            ShardPoint(right - radiusX * 0.18f, bottom),
            ShardPoint(left + radiusX * 0.14f, bottom - radiusY * 0.18f)
        )
    }

    private fun nearestSeedIndices(index: Int, seeds: List<ShardPoint>, limit: Int): IntArray {
        if (limit <= 0) return IntArray(0)

        val bestIndices = IntArray(limit) { -1 }
        val bestDistances = FloatArray(limit) { Float.POSITIVE_INFINITY }
        val seed = seeds[index]

        for (candidateIndex in seeds.indices) {
            if (candidateIndex == index) continue
            val candidate = seeds[candidateIndex]
            val dx = candidate.x - seed.x
            val dy = candidate.y - seed.y
            val distance = dx * dx + dy * dy

            var insertAt = -1
            for (slot in 0 until limit) {
                if (distance < bestDistances[slot]) {
                    insertAt = slot
                    break
                }
            }
            if (insertAt >= 0) {
                for (slot in limit - 1 downTo insertAt + 1) {
                    bestDistances[slot] = bestDistances[slot - 1]
                    bestIndices[slot] = bestIndices[slot - 1]
                }
                bestDistances[insertAt] = distance
                bestIndices[insertAt] = candidateIndex
            }
        }

        var count = 0
        while (count < limit && bestIndices[count] >= 0) count++
        return bestIndices.copyOf(count)
    }

    private fun clipToCloserHalfPlane(
        polygon: MutableList<ShardPoint>,
        seed: ShardPoint,
        other: ShardPoint
    ): MutableList<ShardPoint> {
        if (polygon.isEmpty()) return polygon

        val a = 2f * (other.x - seed.x)
        val b = 2f * (other.y - seed.y)
        val c = other.x * other.x + other.y * other.y - seed.x * seed.x - seed.y * seed.y
        val clipped = ArrayList<ShardPoint>(polygon.size + 2)

        var previous = polygon.last()
        var previousValue = a * previous.x + b * previous.y - c
        var previousInside = previousValue <= 0.0001f

        for (current in polygon) {
            val currentValue = a * current.x + b * current.y - c
            val currentInside = currentValue <= 0.0001f

            if (currentInside != previousInside) {
                val denominator = previousValue - currentValue
                if (denominator != 0f) {
                    val t = (previousValue / denominator).coerceIn(0f, 1f)
                    clipped.add(
                        ShardPoint(
                            previous.x + (current.x - previous.x) * t,
                            previous.y + (current.y - previous.y) * t
                        )
                    )
                }
            }
            if (currentInside) clipped.add(current)

            previous = current
            previousValue = currentValue
            previousInside = currentInside
        }

        return clipped
    }

    private fun MutableList<ShardPoint>.toPuzzlePiece(index: Int): PuzzlePiece {
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        for (point in this) {
            if (point.x < minX) minX = point.x
            if (point.y < minY) minY = point.y
            if (point.x > maxX) maxX = point.x
            if (point.y > maxY) maxY = point.y
        }

        val left = minX.roundToInt().coerceIn(0, imageWidth - 1)
        val top = minY.roundToInt().coerceIn(0, imageHeight - 1)
        val right = max((maxX.roundToInt()).coerceIn(left + 1, imageWidth), left + 1)
        val bottom = max((maxY.roundToInt()).coerceIn(top + 1, imageHeight), top + 1)
        val outline = map {
            Position(
                y = it.y.roundToInt().coerceIn(0, imageHeight),
                x = it.x.roundToInt().coerceIn(0, imageWidth)
            )
        }

        return PuzzlePiece(
            id = "piece_$index",
            pixels = Rect(left, top, right, bottom),
            blocks = Rect(left, top, right, bottom),
            items = mutableListOf(),
            outline = outline,
            isPlaced = false
        )
    }

    private fun sampleSeedCells(seedCount: Int): IntArray {
        val seeds = IntArray(seedCount)
        val selected = HashSet<Int>(seedCount * 2)
        var index = 0
        while (index < seedCount) {
            val cell = Random.nextInt(cellCount)
            if (selected.add(cell)) {
                seeds[index++] = cell
            }
        }
        return seeds
    }

    private fun neighborCell(row: Int, col: Int, direction: Int): Int {
        return when (direction) {
            0 -> if (col > 0) row * columns + col - 1 else -1
            1 -> if (col + 1 < columns) row * columns + col + 1 else -1
            2 -> if (row > 0) (row - 1) * columns + col else -1
            else -> if (row + 1 < rows) (row + 1) * columns + col else -1
        }
    }

    private fun buildBlocks(): List<List<Block>> {
        return List(rows) { row ->
            List(columns) { col ->
                val left = (col * blockSize).coerceAtMost(imageWidth)
                val top = (row * blockSize).coerceAtMost(imageHeight)
                val right = (left + blockSize).coerceAtMost(imageWidth)
                val bottom = (top + blockSize).coerceAtMost(imageHeight)
                Block(
                    position = Position(row, col),
                    rect = Rect(left, top, right, bottom),
                    isTaken = true
                )
            }
        }
    }

    private fun ceilDiv(value: Int, divisor: Int): Int {
        return ((value + divisor - 1) / divisor).coerceAtLeast(1)
    }

    private data class ShardPoint(val x: Float, val y: Float)

    private inner class FastPiece(
        private val index: Int,
        private val keepItemCoordinates: Boolean
    ) {
        private val cells = ArrayList<Int>()
        var count: Int = 0
            private set
        private var minCol: Int = Int.MAX_VALUE
        private var minRow: Int = Int.MAX_VALUE
        private var maxCol: Int = Int.MIN_VALUE
        private var maxRow: Int = Int.MIN_VALUE

        fun add(cell: Int) {
            val row = cell / columns
            val col = cell - row * columns
            if (keepItemCoordinates) {
                cells.add(cell)
            }
            count++
            if (col < minCol) minCol = col
            if (row < minRow) minRow = row
            if (col > maxCol) maxCol = col
            if (row > maxRow) maxRow = row
        }

        fun toPuzzlePiece(
            blockSize: Int,
            columns: Int,
            imageWidth: Int,
            imageHeight: Int
        ): PuzzlePiece {
            val items = if (keepItemCoordinates) {
                val positions = ArrayList<Position>(cells.size)
                for (cell in cells) {
                    val row = cell / columns
                    val col = cell - row * columns
                    positions.add(Position(row, col))
                }
                positions
            } else {
                mutableListOf()
            }

            val left = (minCol * blockSize).coerceAtMost(imageWidth)
            val top = (minRow * blockSize).coerceAtMost(imageHeight)
            val right = ((maxCol + 1) * blockSize).coerceAtMost(imageWidth)
            val bottom = ((maxRow + 1) * blockSize).coerceAtMost(imageHeight)

            return PuzzlePiece(
                id = "piece_$index",
                pixels = Rect(left, top, right, bottom),
                blocks = Rect(minCol, minRow, maxCol, maxRow),
                items = items,
                isPlaced = false
            )
        }
    }
}
