package gui

import service.Refreshable
import tools.aqua.bgw.core.BoardGameApplication
import service.RootService
import service.network.ConnectionState
import tools.aqua.bgw.util.Font
import kotlin.system.exitProcess

/**
 * Represents the main application for the Blokus board game.
 * The application initializes the [RootService] and displays the scenes.
 */
class GameApplication : BoardGameApplication("SoPra Game"), Refreshable {

    /**
     * The root service instance. This is used to call service methods and access the entity layer.
     */
    val rootService: RootService = RootService()

    private val offlineMenuScene : OfflineMenuScene = OfflineMenuScene().apply {
        createButton.onMouseClicked = {this@GameApplication.showGameScene(offlinePlayerScene)}
        backButton.onMouseClicked = {this@GameApplication.showGameScene(gameMenuScene)}
        loadButton.onMouseClicked = {
            rootService.saveService.loadGameState(1)
        }
    }

    private val onlineMenuScene : OnlineMenuScene = OnlineMenuScene(rootService).apply {
        backButton.onMouseClicked = {
            this@GameApplication.showGameScene(gameMenuScene)
            rootService.networkService.disconnect()
        }
    }

    private val gameScene = GameScene(rootService).apply {
        blokusTitle.onMouseClicked = {
            val isOnline = rootService.currentGame.isOnline
            menuSaveGameScene.setOnlineMode(isOnline)
            this@GameApplication.showMenuScene(menuSaveGameScene)
        }
    }

    private val offlinePlayerScene = OfflinePlayerScene(rootService).apply {
        startButton.onMouseClicked = {
            startGame()
        }

        backButton.onMouseClicked = {
            this@GameApplication.showGameScene(gameMenuScene)
        }
    }

    private val gameMenuScene = GameMenuScene().apply {

        offlineButton.onMouseClicked = {
            this@GameApplication.showGameScene(offlineMenuScene)
        }
        onlineButton.onMouseClicked = {
            onlineMenuScene.resetScene()
            this@GameApplication.showGameScene(onlineMenuScene)
            if(rootService.networkService.client!=null){
                rootService.networkService.disconnect()
            }
        }
        quitButton.onMouseClicked = {
            exitProcess(0)
        }
    }
    private val onlineJoinPlayerScene = OnlineJoinPlayerScene(rootService).apply {
        backButton.onMouseClicked = {
            this@GameApplication.showGameScene(gameMenuScene)
            rootService.networkService.disconnect()
        }
    }

    private val gameFinishedScene : GameFinishedScene = GameFinishedScene(rootService).apply {
        exitButton.onMouseClicked = {
            this@GameApplication.showGameScene(gameMenuScene)
        }
    }

    private val menuSaveGameScene : MenuSaveGameScene= MenuSaveGameScene ().apply {
        exitButton.onMouseClicked = {
            rootService.saveService.saveGameState(1)
            this@GameApplication.showGameScene(gameMenuScene)
            if(rootService.currentGame.isOnline) {
                rootService.networkService.disconnect()
            }
            hideMenuScene()
        }
        saveButton.onMouseClicked = {
            rootService.saveService.saveGameState(1)
            hideMenuScene()
        }
        returnButton.onMouseClicked = {
            hideMenuScene()
        }
    }

    init {
        loadFont("Font1.ttf", "Font1", Font.FontWeight.NORMAL)
        loadFont("Font2.ttf", "Font2", Font.FontWeight.NORMAL)

        rootService.addRefreshables(
            this,
            gameScene,
            onlineJoinPlayerScene
        )

        this.showGameScene(gameMenuScene)
    }

    /**
     * Switches scenes based on network state changes.
     */
    override fun refreshConnectionState(newState: ConnectionState) {
        // As soon as the host successfully creates a lobby, switch to the lobby scene
        if (newState == ConnectionState.WAITING_FOR_GUEST) {
            showGameScene(onlineJoinPlayerScene)
        }
    }

    /**
     * called after a game state has been successfully loaded
     */
    override fun refreshAfterGameLoaded() {
        showGameScene(gameScene)
    }

    /**
     * called when the game has ended
     *
     * @param ranking a list of a pairs containing player
     * names and their scores, sorted in descending order
     */
    override fun refreshAfterGameEnd(ranking : MutableList<Pair<String,Int>>) {
        gameFinishedScene.showRanking(ranking)
        showGameScene(gameFinishedScene)
        /*if(rootService.networkService.client!=null){
            Thread.sleep(1000)
            rootService.networkService.client?.disconnect()
        }*/
    }

    /**
     * called when an error occurs in the service layer
     *
     * @param message the error message describing the issue
     */
    override fun refreshAfterError(message: String) {
        println("Error: $message")
    }
}