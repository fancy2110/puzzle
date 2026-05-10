package com.puzzle.game.engine

import com.puzzle.game.engine.model.Block
import com.puzzle.game.engine.model.Position
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.engine.model.Rect
import kotlin.random.Random

class ImageSplitter(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val blockSize: Int = 64
) {
    private val columns: Int = (imageWidth / blockSize) + 1
    private val rows: Int = (imageHeight / blockSize) + 1

    private val blocks: MutableList<MutableList<Block>> = mutableListOf()

    fun split(pieceCount: Int): Pair<List<List<Block>>, List<PuzzlePiece>> {
        initBlocks()

        val seedBlocks = selectRandomSeeds(pieceCount)
        val pieces = expandPieces(seedBlocks)

        return Pair(blocks, pieces)
    }

    private fun initBlocks() {
        blocks.clear()
        for (y in 0 until rows) {
            val row = mutableListOf<Block>()
            val top = (y * blockSize).coerceAtMost(imageHeight)
            val bottom = (top + blockSize).coerceAtMost(imageHeight)
            if (top >= bottom) break

            for (x in 0 until columns) {
                val left = (x * blockSize).coerceAtMost(imageWidth)
                val right = (left + blockSize).coerceAtMost(imageWidth)
                if (left >= right) break

                val rect = Rect(left, top, right, bottom)
                val pos = Position(y, x)
                row.add(Block(pos, rect))
            }
            blocks.add(row)
        }
    }

    private fun selectRandomSeeds(count: Int): List<Pair<Position, Int>> {
        val allPositions = mutableListOf<Position>()
        for (y in blocks.indices) {
            for (x in blocks[y].indices) {
                allPositions.add(Position(y, x))
            }
        }

        allPositions.shuffle(Random)
        val seedCount = count.coerceAtMost(allPositions.size)

        return allPositions.take(seedCount).mapIndexed { index, pos ->
            blocks[pos.y][pos.x].taken()
            Pair(pos, index)
        }
    }

    private fun expandPieces(seeds: List<Pair<Position, Int>>): List<PuzzlePiece> {
        val pieces = seeds.map { (pos, index) ->
            val block = blocks[pos.y][pos.x]
            PuzzlePiece.create("piece_$index", block)
        }

        val directions = listOf(
            Position(0, -1),
            Position(0, 1),
            Position(-1, 0),
            Position(1, 0)
        )

        var queue: MutableList<Triple<Position, Int, Int>> = seeds.map { (pos, idx) ->
            Triple(pos, idx, 0)
        }.toMutableList()

        var iteration = 0
        val maxIterations = rows * columns * 2

        while (queue.isNotEmpty() && iteration < maxIterations) {
            iteration++
            queue.shuffle(Random)

            val nextQueue = mutableListOf<Triple<Position, Int, Int>>()
            var remainingEmpty = 0

            for ((pos, pieceIndex, _) in queue) {
                val shuffledDirs = directions.shuffled(Random).take(2)

                for (dir in shuffledDirs) {
                    val ny = pos.y + dir.y
                    val nx = pos.x + dir.x
                    if (ny in blocks.indices && nx in blocks[0].indices) {
                        val neighborBlock = blocks[ny][nx]
                        if (!neighborBlock.isTaken) {
                            neighborBlock.taken()
                            pieces[pieceIndex].addBlock(neighborBlock)
                            nextQueue.add(Triple(Position(ny, nx), pieceIndex, 0))
                        }
                    }
                }
            }

            for (y in blocks.indices) {
                for (x in blocks[y].indices) {
                    if (!blocks[y][x].isTaken) remainingEmpty++
                }
            }

            if (remainingEmpty == 0) break
            if (nextQueue.isEmpty() && remainingEmpty > 0) {
                for (y in blocks.indices) {
                    for (x in blocks[y].indices) {
                        if (!blocks[y][x].isTaken) {
                            blocks[y][x].taken()
                            val nearestPiece = pieces.indices.random(Random)
                            pieces[nearestPiece].addBlock(blocks[y][x])
                            nextQueue.add(Triple(Position(y, x), nearestPiece, 0))
                        }
                    }
                }
            }

            queue = nextQueue
        }

        return pieces
    }
}
