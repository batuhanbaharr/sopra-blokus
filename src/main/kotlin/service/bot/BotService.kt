package service.bot

import entity.PlayerType
import service.AbstractRefreshingService
import service.RootService
/**
 * service that manage bot actions. Pick the right strategy
 * based on the player type and executes the move
 * @param rootService for access to game state and other services.
 */
class BotService(private val rootService: RootService): AbstractRefreshingService() {
    /**
     * set the thinking delay for bot moves
     * @param speed delay between moves
     */
    fun setBotSpeed(speed: Int) {
        val game = rootService.currentGame
        game.botSpeed = speed
    }

    /**
     * pick right strategy based on current player type
     * calculate a move and places block on the board
     */
    fun executeBotTurn() {
        val player = rootService.gameService.getCurrentPlayer()
        val color = rootService.gameService.getCurrentColor()

        if (!rootService.gameService.hasValidTurnLeft(color)) {
            return
        }
        // pick right strategy
        val strategy: BotStrategy = when (player.playerType) {
            PlayerType.BOTEASY -> BotEasy()
            PlayerType.BOTHARD -> BotHard()
            else -> return
        }
        /*
        // wait for bot speed delay
        if (game.botSpeed > 0) {
            Thread.sleep(game.botSpeed.toLong())
        }
        */
        // find move
        val move = strategy.findNextMove(rootService) ?: return
        // place the block
        val block = move.first
        val coordinates = move.second
        if(rootService.currentGame.isOnline) {
            Thread.sleep(500)
        }
        rootService.playerActionService.placeBlock(block, coordinates)
    }
    /**
     * calculate and execute next bot move
     */
    fun calculateBotAction() {
        executeBotTurn()
    }
}