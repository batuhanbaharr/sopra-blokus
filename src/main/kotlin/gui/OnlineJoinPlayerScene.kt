package gui

import entity.ColorType
import entity.GameMode
import entity.PlayerType
import entity.ScoringStrategy
import service.RootService
import service.Refreshable
import service.network.ConnectionState

import tools.aqua.bgw.components.uicomponents.*

import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font

import tools.aqua.bgw.visual.*


/**
 * Scene for configuring and starting an offline multiplayer game
 *
 * User configures a local game with 2, 3, or 4 players
 * Players can enter their names, choose between Human and Bot, choose if they want
 * to play normal or advanced and if they want a smaller board with only 2 players
 *
 * It is also possible to shuffle the player names
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class OnlineJoinPlayerScene(private val rootService: RootService) :
    BoardGameScene(1920, 1080,
        background = ImageVisual("OfflinePlayer.png")), Refreshable {

    private val gameVariants = listOf(
        GameMode.TWO_PLAYER.toString(),
        GameMode.THREE_PLAYER.toString(),
        GameMode.FOUR_PLAYER.toString())

    private var currentVariantIndex = 2

    private val playerName1 = Label(
        posX = 540, posY = 330, width = 600, height = 90,
        text = "Waiting...", font = Font(60, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_LEFT,
        visual = ColorVisual(Color(0x61daff)).apply { transparency = 0.0 }
    )

    private val playerName2 = Label(
        posX = 540, posY = 460, width = 600, height = 90,
        text = "Waiting...", font = Font(60, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_LEFT,
        visual = ColorVisual(Color(0x61daff)).apply { transparency = 0.0 }
    )

    private val playerName3 = Label(
        posX = 540, posY = 575, width = 600, height = 90,
        text = "Waiting...", font = Font(60, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_LEFT,
        visual = ColorVisual(Color(0x61daff)).apply { transparency = 0.0 }
    )

    private val playerName4 = Label(
        posX = 540, posY = 700, width = 600, height = 90,
        text = "Waiting...", font = Font(60, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_LEFT,
        visual = ColorVisual(Color(0x61daff)).apply { transparency = 0.0 }
    )

    val playerNames = listOf(
        playerName1, playerName2,
        playerName3, playerName4
    )

    private val sessionIDLabel = Label(
        posX = 1400, posY = 960, width = 450, height = 50,
        text = "Session ID: Loading...",
        font = Font(35, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_RIGHT
    )

    val startButton = Button(
        posX = 700, posY = 890, width = 500, height = 90,
        text = gameVariants[currentVariantIndex],
        font = Font(55, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.8 }
    ).apply {
        isDisabled = true
        onMouseClicked = { startGame() }
    }

    private val smallBoardCheckbox = CheckBox(
        posX = 700, posY = 830, width = 300, height = 50,
        text = "Small Board",
        alignment = Alignment.CENTER_LEFT,
        font = Font(28, Color(0xFFFFFF), "Font2")
    )

    private val advancedScoringCheckbox = CheckBox(
        posX = 960, posY = 830, width = 300, height = 50,
        text = "Advanced Scoring",
        alignment = Alignment.CENTER_LEFT,
        font = Font(28, Color(0xFFFFFF), "Font2")
    )

    private val changeGameLeft = Button(
        posX = 592, posY = 900, width = 90, height = 90,
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply {
        onMouseClicked = {
            currentVariantIndex = (currentVariantIndex - 1 +
                    gameVariants.size) % gameVariants.size
            updateVariantDisplay()
        }
    }

    private val changeGameRight = Button(
        posX = 1223, posY = 900, width = 95, height = 90,
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply {
        onMouseClicked = {
            currentVariantIndex = (currentVariantIndex + 1) % gameVariants.size
            updateVariantDisplay()
        }
    }

    private val shufflePlayerNames = Button(
        posX = 140,
        posY = 908,
        width = 200,
        height = 85,
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    ).apply {
        onMouseClicked = {
            shufflePlayers()
        }
    }

    val backButton = Button(
        posX = 50,
        posY = 50,
        width = 100,
        height = 100,
        text = "←",
        font = Font(70, Color(0xFFFFFF), "Font2"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.2
        }
    )

    private val playerOrder = mutableListOf(0, 1, 2, 3)

    private val orderButton1 = Button(
        posX = 430, posY = 330, width = 80, height = 90, text = "1",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply
        { style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply { onMouseClicked = { cycleOrder(0) } }

    private val orderButton2 = Button(
        posX = 430, posY = 460, width = 80, height = 90, text = "2",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply
        { style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply { onMouseClicked = { cycleOrder(1) } }

    private val orderButton3 = Button(
        posX = 430, posY = 575, width = 80, height = 90, text = "3",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply
        { style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply { onMouseClicked = { cycleOrder(2) } }

    private val orderButton4 = Button(
        posX = 430, posY = 700, width = 80, height = 90, text = "4",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply
        { style.borderRadius = BorderRadius.MEDIUM; transparency = 0.0 }
    ).apply { onMouseClicked = { cycleOrder(3) } }


    init {
        addComponents(
            playerName1, playerName2, playerName3, playerName4,
            smallBoardCheckbox, advancedScoringCheckbox,
            startButton, changeGameLeft, changeGameRight, shufflePlayerNames,
            orderButton1, orderButton2, orderButton3, orderButton4, backButton,
            sessionIDLabel
        )

        updateVariantDisplay()
    }

    /**
     * Shuffles player names if clicked on shufflePlayerNames Button.
     */
    private fun shufflePlayers() {
        var playerCount = currentVariantIndex + 2
        if (startButton.text == GameMode.THREE_PLAYER.toString()) playerCount++
        val playerNamesTexts = playerNames
            .map {it.text}
            .slice(0..< playerCount)
            .toMutableList()
        if (!playerNamesTexts.any {it.isNotBlank() && !it.contains("Waiting...")}) return
        val unshuffledList = playerNamesTexts.toList()
        val shuffledList = unshuffledList
            .shuffled()
            .map{it}
            .toMutableList()
        while (unshuffledList == shuffledList) {
            shuffledList.shuffle()
        }
        for (index in 0..< playerCount){
            playerNames[index].text = shuffledList[index]
        }
    }

    /**
     * Updates UI visibility based on whether it is a 2, 3, or 4 player game.
     */
    private fun updateVariantDisplay() {
        startButton.text = gameVariants[currentVariantIndex]
        val playerCount = currentVariantIndex + 2

        if (playerCount == 2) {
            smallBoardCheckbox.isDisabled = false
            smallBoardCheckbox.isVisible = true
        } else {
            smallBoardCheckbox.isChecked = false
            smallBoardCheckbox.isDisabled = true
            smallBoardCheckbox.isVisible = false
        }

        // Reihenfolge nur bei 4 Spielern
        playerName3.isVisible = (playerCount >= 3)
        playerName4.isVisible = (playerCount >= 3)

        val orderButtons = listOf(orderButton1, orderButton2, orderButton3, orderButton4)
        orderButtons.forEach { it.isVisible = (playerCount == 4) }

        if (playerCount != 4) {
            for (i in 0 until 4) playerOrder[i] = i
            updateOrderButtons()

            val names = rootService.networkService.client?.playerNames ?: emptyList()
            for (i in 0 until 4) {
                playerNames[i].text =
                    if (i < names.size) names[i] else "Waiting..."
            }
        }

        updateStartButton()
    }

    /**
     * Validates input fields based on the currently selected variant and enables/disables the start button.
     */
    private fun updateStartButton() {
        val playerCount = currentVariantIndex + 2
        val namesCount = rootService.networkService.client?.playerNames?.size ?: 0

        val isValid = namesCount == playerCount

        // Reihenfolge nur prüfen bei 4 Spielern
        if (playerCount == 4 && !isOrderValid()) {
            startButton.isDisabled = true
            return
        }

        startButton.isDisabled = !isValid
    }

    /**
     * Triggered by Refreshable when the network state changes (e.g. a guest joins).
     */
    override fun refreshConnectionState(newState: ConnectionState) {
        if (newState == ConnectionState.WAITING_FOR_GUEST ||
            newState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
            val client = rootService.networkService.client
            val names = client?.playerNames ?: emptyList()

            for (i in 0 until 4) {
                if (i < names.size) {
                    playerNames[i].text = names[i]
                } else {
                    playerNames[i].text = "Waiting..."
                }
            }

            if (client?.sessionID != null) {
                sessionIDLabel.text = "Session ID: ${client.sessionID}"
            }

            updateStartButton()
        }
    }

    /**
     * Constructs the player list based on the variant and starts the game via the GameService.
     */
    fun startGame() {
        val networkNames = rootService.networkService.client?.playerNames ?: return
        val names = playerNames.map { it.text }.filter { it != "Waiting..." && it.isNotBlank() }
        val playerCount = currentVariantIndex + 2

        if (names.size != playerCount) return

        val colorsDefault = listOf(
            ColorType.BLUE,
            ColorType.YELLOW,
            ColorType.RED,
            ColorType.GREEN
        )

        val players = mutableListOf<Triple<String, PlayerType, ColorType>>()

        for (i in 0 until 4) {
            val labelText = playerNames[i].text.trim()
            if (labelText != "Waiting..." && labelText.isNotBlank()) {
                val isHost = (labelText == networkNames.firstOrNull())
                val pType = if (isHost) rootService.networkService.playerType else PlayerType.ONLINE

                players.add(Triple(labelText, pType, colorsDefault[i]))
            }
        }

        val gameMode = when (playerCount) {
            2 -> if (smallBoardCheckbox.isChecked)
                GameMode.TWO_PLAYER_SMALL else GameMode.TWO_PLAYER
            3 -> GameMode.THREE_PLAYER
            else -> GameMode.FOUR_PLAYER
        }

        val scoringStrategy =
            if (advancedScoringCheckbox.isChecked) ScoringStrategy.ADVANCED
            else ScoringStrategy.BASIC

        val orderedPlayers = players
            .mapIndexed { index, player -> Pair(playerOrder[index], player) }
            .sortedBy { it.first }
            .map { it.second }
            .toMutableList()

        rootService.networkService.startNewHostedGame(
            orderedPlayers,
            gameMode,
            scoringStrategy
        )
    }

    private fun cycleOrder(playerIndex: Int) {
        playerOrder[playerIndex] = (playerOrder[playerIndex] + 1) % 4
        updateOrderButtons()
        updateStartButton()
    }

    private fun updateOrderButtons() {
        val buttons = listOf(orderButton1, orderButton2, orderButton3, orderButton4)
        for (i in buttons.indices) {
            buttons[i].text = (playerOrder[i] + 1).toString()
        }
    }

    private fun isOrderValid(): Boolean {
        val used = playerOrder.sorted()
        return used == listOf(0, 1, 2, 3)
    }
}