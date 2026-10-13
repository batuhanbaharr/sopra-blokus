package service.bot

import entity.Block
import service.RootService

/**
 * easy bot that picks a random valid move
 * go through all blocks and positions in random order
 * and return the first valid one it finds
 */
class BotEasy : BotStrategy {

    override fun findNextMove(rootService: RootService): Pair<Block, Pair<Int, Int>>? {
        val game = rootService.currentGame
        val board = game.board ?: return null
        val boardHeight = board.size
        val boardWidth = board[0].size
        val currentColor = rootService.gameService.getCurrentColor()
        val shuffledBlocks = currentColor.blocks.shuffled()
        for (block in shuffledBlocks) {
            val result = tryAllVariants(block, boardWidth, boardHeight, rootService)
            if (result != null) return result
        }
        return null
    }
    /** try all rotations and mirror for a block and find a valid position */
    private fun tryAllVariants(block: Block, boardWidth: Int, boardHeight: Int,
        rootService: RootService): Pair<Block, Pair<Int, Int>>? {
        for (rotation in 0 until 4) {
            for (mirrored in listOf(false, true)) {
                var testBlock = block.copy()
                repeat(rotation) {
                    testBlock = rootService.playerActionService.rotateBlock(testBlock, false)
                }
                if (mirrored) {
                    testBlock = rootService.playerActionService.mirrorBlock(testBlock, false)
                }
                val result = tryRandomPositions(testBlock, boardWidth, boardHeight, rootService)
                if (result != null) return result
            }
        }
        return null
    }
    /** shuffle all positions and return the first valid one */
    private fun tryRandomPositions(testBlock: Block, boardWidth: Int, boardHeight: Int,
        rootService: RootService): Pair<Block, Pair<Int, Int>>? {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until boardHeight) {
            for (x in 0 until boardWidth) {
                positions.add(Pair(x, y))
            }
        }
        positions.shuffle()
        for (pos in positions) {
            if (rootService.playerActionService.isValidBlockPlacement(testBlock, pos)) {
                return Pair(testBlock, pos)
            }
        }
        return null
    }
}