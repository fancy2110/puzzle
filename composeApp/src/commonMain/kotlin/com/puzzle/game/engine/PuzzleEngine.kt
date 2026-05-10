package com.puzzle.game.engine

import com.puzzle.game.engine.model.Block
import com.puzzle.game.engine.model.PuzzlePiece

class PuzzleEngine {
    var imageWidth: Int = 0
        private set
    var imageHeight: Int = 0
        private set

    var blocks: List<List<Block>> = emptyList()
        private set
    var pieces: List<PuzzlePiece> = emptyList()
        private set

    fun loadImage(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
    }

    fun splitImage(pieceCount: Int, blockSize: Int = 64) {
        val splitter = ImageSplitter(imageWidth, imageHeight, blockSize)
        val (blocksResult, piecesResult) = splitter.split(pieceCount)
        blocks = blocksResult
        pieces = piecesResult
    }

    fun shufflePieces(): List<PuzzlePiece> {
        return pieces.shuffled()
    }
}
