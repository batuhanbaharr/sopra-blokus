package service.bot
import entity.Block
import service.RootService
/**
 * interface for bot strategies. Each bot type (easy, hard)
 * implements this with its own way of picking the next move.
 */
interface BotStrategy {
    /**
     * find next move for the bot
     * @param rootService for access to game, players and move validation.
     * @return pair of  block to place and coordinates or null if no move is found.
     */
    fun findNextMove(rootService: RootService): Pair<Block, Pair<Int, Int>>?
}