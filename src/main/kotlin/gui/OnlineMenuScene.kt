package gui

import entity.PlayerType
import service.RootService

import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*

//background = ColorVisual(Color(0x10101A)
/**
 * Online Menu Scene to either host a new lobby or join an existing game.
 */
class OnlineMenuScene(private val rootService: RootService) : BoardGameScene(
    width = 1920, height = 1080,
    background = ImageVisual("OnlineBlokus.png")) {

    private val networkSecret = "blocksAgain"

    private var currentPlayerType = PlayerType.LOCAL

    private val standardRadius = BorderRadius.MEDIUM
    private val inputVisual = ColorVisual(Color(0x261F4C)).apply{
        transparency = 0.5; style.borderRadius = standardRadius }
    private val primaryButtonVisual = ColorVisual(Color(0x162ba0)).apply { style.borderRadius = standardRadius }
    private val dangerButtonVisual = ColorVisual(Color(0xDC2626)).apply { style.borderRadius = standardRadius }

    private val menuTitle = Label(
        posX = 560, posY = 150, width = 800, height = 80,
        text = "Online Multiplayer",
        font = Font(60, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER
    )

    private val playerNameInput = TextField(
        posX = 660, posY = 300, width = 450, height = 80,
        prompt = "Enter Player Name",
        font = Font(40, Color.WHITE, "Font2"),
        visual = inputVisual
    )

    private val botButton = Button(
        posX = 1140, posY = 300, width = 120, height = 80,
        text = "AI", font = Font(40, Color.WHITE, "Font2"),
        visual = ColorVisual(Color(0x0d106b)).apply {
            style.borderRadius = standardRadius
            transparency = 0.8
        }
    ).apply {
        onMouseClicked = { cycleBotType() }
    }

    private val sessionIdInput = TextField(
        posX = 660, posY = 450, width = 600, height = 80,
        prompt = "Session ID (Required for Join)",
        font = Font(40, Color.WHITE, "Font2"),
        visual = inputVisual
    )

    private val createLobbyButton = Button(
        posX = 660, posY = 600, width = 280, height = 80,
        text = "Create Lobby",
        font = Font(35, Color.WHITE, "Font2"),
        visual = primaryButtonVisual
    ).apply {
        onMouseClicked = { performHost() }
    }

    private val joinGameButton = Button(
        posX = 980, posY = 600, width = 280, height = 80,
        text = "Join Game",
        font = Font(40, Color.WHITE, "Font2"),
        visual = primaryButtonVisual
    ).apply {
        onMouseClicked = { performJoin() }
    }

    val backButton = Button(
        posX = 820, posY = 800, width = 280, height = 80,
        text = "Back",
        font = Font(40, Color.WHITE, "Font2"),
        visual = dangerButtonVisual
    )

    private val statusLabel = Label(
        posX = 560, posY = 720, width = 800, height = 50,
        text = "",
        font = Font(30, Color(0xFFD700), "Font2"),
        alignment = Alignment.CENTER
    )


    private val loadingOverlay = Pane<UIComponent>(
        posX = 0, posY = 0, width = 1920, height = 1080,
        visual = ColorVisual(Color(0, 0, 0, 200))
    ).apply { isVisible = false }

    private val loadingLabel = Label(
        posX = 0, posY = 400, width = 1920, height = 100,
        text = "Please Wait...",
        font = Font(70, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER
    )

    private val cancelLoadingButton = Button(
        posX = 820, posY = 550, width = 280, height = 80,
        text = "Cancel",
        font = Font(40, Color.WHITE, "Font2"),
        visual = dangerButtonVisual
    ).apply {
        onMouseClicked = {
            rootService.networkService.disconnect()
            loadingOverlay.isVisible = false
            statusLabel.text = "Connection cancelled."
        }
    }


    init {
        loadingOverlay.addAll(loadingLabel, cancelLoadingButton)

        addComponents(
            menuTitle,
            playerNameInput,
            botButton,
            sessionIdInput,
            createLobbyButton,
            joinGameButton,
            backButton,
            statusLabel,
            loadingOverlay
        )
    }

    /**
     * Setzt die Szene zurück. Sollte aufgerufen werden, bevor die Szene wieder angezeigt wird.
     */
    fun resetScene() {
        loadingOverlay.isVisible = false
        statusLabel.text = ""

    }

    /**
     * Cycles through Human, BotEasy, and BotHard.
     */
    private fun cycleBotType() {
        when (currentPlayerType) {
            PlayerType.LOCAL -> {
                currentPlayerType = PlayerType.BOTEASY
                botButton.visual = ColorVisual(Color(0xFFAF47)).apply {
                    style.borderRadius = standardRadius; transparency = 0.8
                }
            }
            PlayerType.BOTEASY -> {
                currentPlayerType = PlayerType.BOTHARD
                botButton.visual = ColorVisual(Color(0xFF4747)).apply {
                    style.borderRadius = standardRadius; transparency = 0.8
                }
            }
            PlayerType.BOTHARD -> {
                currentPlayerType = PlayerType.LOCAL
                botButton.visual = ColorVisual(Color(0x0d106b)).apply {
                    style.borderRadius = standardRadius; transparency = 0.8
                }
            }
            else -> {}
        }
    }


    /**
     * Calls JoinGame from network
     */
    private fun performJoin() {
        if (playerNameInput.text.isBlank() || sessionIdInput.text.isBlank()) {
            statusLabel.text = "Error: Player Name and Session ID required!"
            return
        }

        loadingOverlay.isVisible = true
        statusLabel.text = ""

        rootService.networkService.playerType = currentPlayerType
        rootService.networkService.joinGame(
            secret = networkSecret,
            name = playerNameInput.text,
            sessionID = sessionIdInput.text
        )
    }

    /**
     * Calls Hostgame from network.
     */
    private fun performHost() {
        if (playerNameInput.text.isBlank()) {
            statusLabel.text = "Error: Player Name required to host!"
            return
        }
        statusLabel.text = "Creating lobby..."

        rootService.networkService.playerType = currentPlayerType

        rootService.networkService.hostGame(
            secret = networkSecret,
            name = playerNameInput.text,
            sessionID = sessionIdInput.text.ifBlank { null }
        )
    }
}