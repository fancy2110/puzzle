package com.puzzle.game.engine.model

data class PuzzlePiece(
    val id: String,
    val pixels: Rect = Rect(),
    val blocks: Rect = Rect(),
    val items: MutableList<Position> = mutableListOf(),
    var isPlaced: Boolean = false
) {
    val width: Int get() = pixels.width
    val height: Int get() = pixels.height

    fun addBlock(block: Block) {
        val pos = block.position
        blocks.expand(pos.x, pos.y, pos.x, pos.y)
        items.add(pos)
        pixels.expand(block.rect.left, block.rect.top, block.rect.right, block.rect.bottom)
    }

    companion object {
        fun create(id: String, block: Block): PuzzlePiece {
            val pos = block.position
            return PuzzlePiece(
                id = id,
                pixels = Rect(block.rect.left, block.rect.top, block.rect.right, block.rect.bottom),
                blocks = Rect(pos.x, pos.y, pos.x, pos.y),
                items = mutableListOf(pos)
            )
        }
    }
}
