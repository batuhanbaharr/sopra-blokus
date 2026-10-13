package service

import entity.Game
import service.bot.BotService
import service.network.NetworkService


/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService{
    // backing property for the currentGame object
    private var _currentGame: Game? = null

    var currentGame: Game
        get() {
            val curGame = _currentGame
            checkNotNull(curGame) { "There is no current running game" }
            return curGame}
        set(value) { _currentGame = value}

    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    val botService = BotService(this)
    val saveService = SaveService(this)
    val networkService = NetworkService(this)

    /**
     * This function adds refreshable to the list of refreshables that needs to be called
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        botService.addRefreshable(newRefreshable)
        saveService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }

    /**
     * Help function to set the current Game null
     */
    fun setCurrentGameToNull(){
        _currentGame = null
    }
}