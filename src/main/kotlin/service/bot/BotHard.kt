package service.bot

import entity.Block
import entity.ColorType
import entity.Color
import service.RootService
import kotlin.math.abs

/**
 *hard bot that try to play smart moves
 * prefer big blocks first and try to keep future move options open
 */
class BotHard : BotStrategy {

    override fun findNextMove(rootService: RootService): Pair<Block, Pair<Int, Int>>? {
        val currentColor = rootService.gameService.getCurrentColor()

        // sort blocks so we use bigger ones first
        val sortedBlocks = currentColor.blocks.sortedByDescending { countSquares(it) }
        var bestMove: Pair<Block, Pair<Int, Int>>? = null
        var bestScore = Int.MIN_VALUE

        for (block in sortedBlocks) {
            val result = findBestVariant(block, bestScore, rootService)
            if (result != null && result.second > bestScore) {
                bestScore = result.second
                bestMove = result.first
            }
        }
        return bestMove
    }

    /** try all rotations and mirror for block and return best scoring move */
    private fun findBestVariant(block: Block, currentBest: Int, rootService: RootService):
        Pair<Pair<Block, Pair<Int, Int>>, Int>? {
        var bestMove: Pair<Block, Pair<Int, Int>>? = null
        var bestScore = currentBest

        for (rotation in 0 until 4) {
            for (mirrored in listOf(false, true)) {
                var testBlock = block.copy()
                repeat(rotation) {
                    testBlock = rootService.playerActionService.rotateBlock(testBlock, false)
                }
                if (mirrored) {
                    testBlock = rootService.playerActionService.mirrorBlock(testBlock, false)
                }
                val result = findBestPosition(testBlock, bestScore, rootService)
                if (result != null && result.second > bestScore) {
                    bestScore = result.second
                    bestMove = result.first
                }
            }
        }
        return if (bestMove != null) Pair(bestMove, bestScore) else null
    }
    /** try all positions for a block variant and return the best one */
    private fun findBestPosition(testBlock: Block, currentBest: Int, rootService: RootService):
            Pair<Pair<Block, Pair<Int, Int>>, Int>? {
        val board = rootService.currentGame.board ?: return null
        val boardHeight = board.size
        val boardWidth = board[0].size
        var bestMove: Pair<Block, Pair<Int, Int>>? = null
        var bestScore = currentBest
        for (y in 0 until boardHeight) {
            for (x in 0 until boardWidth) {
                val pos = Pair(x, y)
                if (!rootService.playerActionService.isValidBlockPlacement(testBlock, pos)) continue
                val score = evaluateMove(testBlock, pos, rootService)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = Pair(testBlock.copy(), pos)
                }
            }
        }
        return if (bestMove != null) Pair(bestMove, bestScore) else null
    }

    /**
     * give a score to a move based on block size, free corners,
     * opponent blocking and how much it limits opponent future moves
     */
    private fun evaluateMove(block: Block, pos: Pair<Int, Int>, rootService: RootService): Int {
        var score = 0
        val x = pos.first
        val y = pos.second
        // bigger blocks are worth more
        score += countSquares(block) * 8
        // check neighbors for each square of the block
        score += scoreNeighbors(block, x, y, rootService)
        // check how much this move blocks opponent future moves
        score += scoreOpponentBlocking(block, x, y, rootService)
        // adjust score based on game phase
        score += scorePosition(x, y, rootService)
        return score
    }
    /** check diagonal and straight neighbors for each square */
    private fun scoreNeighbors(block: Block, x: Int, y: Int, rootService: RootService): Int {
        var score = 0
        val height = block.blueprint.size
        val width = block.blueprint[0].size
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                if (block.blueprint[dy][dx] == 1) {
                    score += checkDiagonals(x + dx, y + dy, rootService)
                    score += checkStraightNeighbors(x + dx, y + dy, rootService)
                }
            }
        }
        return score
    }

    private fun checkDiagonals(bx: Int, by: Int, rootService: RootService): Int {
        var score = 0
        val board = rootService.currentGame.board ?: return 0
        val boardWidth = board[0].size
        val boardHeight = board.size
        val diagonals = listOf(Pair(bx-1,by-1), Pair(bx+1,by-1), Pair(bx-1,by+1), Pair(bx+1,by+1))
        for (d in diagonals) {
            if (d.first in 0 until boardWidth && d.second in 0 until boardHeight) {
                if (board[d.second][d.first] == ColorType.NONE) score += 4
            }
        }
        return score
    }
    private fun checkStraightNeighbors(bx: Int, by: Int, rootService: RootService): Int {
        var score = 0
        val board = rootService.currentGame.board ?: return 0
        val colorType = rootService.gameService.getCurrentColor().colorType
        val boardWidth = board[0].size
        val boardHeight = board.size

        val neighbors = listOf(Pair(bx-1,by), Pair(bx+1,by), Pair(bx,by-1), Pair(bx,by+1))
        for (n in neighbors) {
            if (n.first in 0 until boardWidth && n.second in 0 until boardHeight) {
                val cell = board[n.second][n.first]
                if (cell != ColorType.NONE && cell != colorType) score += 2
            }
        }
        return score
    }
    /**
     * check if this move covers squares that are in opponent validity boards.
     * if we place our block on a spot where opponent could connect, we block them.
     */
    private fun scoreOpponentBlocking(block: Block, x: Int, y: Int, rootService: RootService): Int {
        var score = 0
        val game = rootService.currentGame
        val ourColor = rootService.gameService.getCurrentColor().colorType
        for (color in game.colors) {
            if (color.colorType != ourColor && color.validityBoard != null) {
                score += calculateOverlap(block, x, y, color)
            }
        }
        return score
    }
    private fun calculateOverlap(block: Block, x: Int, y: Int, color: Color): Int {
        var score = 0
        val oppValidity = color.validityBoard ?: return 0
        val height = block.blueprint.size
        val width = block.blueprint[0].size
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                if (block.blueprint[dy][dx] != 1) continue
                val bx = x + dx
                val by = y + dy
                if (by in oppValidity.indices && bx in oppValidity[0].indices && oppValidity[by][bx] == 1){
                    score += 3
                }
            }
        }
        return score
    }
    /** adjust score based on game phase: early game stay center, late game go to edges */
    private fun scorePosition(x: Int, y: Int, rootService: RootService): Int {
        val board = rootService.currentGame.board ?: return 0
        val colorType = rootService.gameService.getCurrentColor().colorType
        val boardWidth = board[0].size
        val boardHeight = board.size
        val centerX = boardWidth / 2
        val centerY = boardHeight / 2
        val distFromCenter = abs(x - centerX) + abs(y - centerY)
        val blocksLeft = 21 - countPlacedOnBoard(board, colorType, boardWidth, boardHeight)
        return if (blocksLeft > 14) {
            -distFromCenter
        } else{
            distFromCenter / 2
        }
    }
    /** count how many squares a block has */
    private fun countSquares(block: Block): Int {
        var count = 0
        for (row in block.blueprint) {
            for (cell in row) {
                if (cell == 1) count++
            }
        }
        return count
    }
    /** count how many squares of this color are already on the board */
    private fun countPlacedOnBoard(
        board: Array<Array<ColorType>>, colorType: ColorType, width: Int, height: Int): Int {
        var count = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (board[y][x] == colorType) count++
            }
        }
        return count
    }
}