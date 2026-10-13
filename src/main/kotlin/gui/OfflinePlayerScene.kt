package gui

import entity.ColorType
import entity.GameMode
import entity.PlayerType
import entity.ScoringStrategy
import service.RootService

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
class OfflinePlayerScene(private val rootService: RootService) :
    BoardGameScene(1920, 1080, background = ImageVisual("OfflinePlayer.png")){

    private val playerVariants = mutableListOf(
        PlayerType.LOCAL, PlayerType.LOCAL,
        PlayerType.LOCAL, PlayerType.LOCAL
    )

    private val gameVariants = listOf(
        GameMode.TWO_PLAYER.toString(),
        GameMode.THREE_PLAYER.toString(),
        GameMode.FOUR_PLAYER.toString())


    private var currentVariantIndex = 2

    private val playerName1 = TextField(
        posX = 540, posY = 330, width = 600, height = 90,
        text = "",
        prompt = "Name 1: ",
        font = Font(60, Color(0xffffff), "Font2"),
        visual = ColorVisual(Color(0x61daff)).apply {
            transparency = 0.0
        }
    )

    private val playerName2 = TextField(
        posX = 540, posY = 460, width = 600, height = 90,
        text = "",
        prompt = "Name 2: ",
        font = Font(60, Color(0xffffff), "Font2"),
        visual = ColorVisual(Color(0x61daff)).apply {
            transparency = 0.0
        }
    )

    private val playerName3 = TextField(
        posX = 540, posY = 575, width = 600, height = 90,
        text = "",
        prompt = "Name 3: ",
        font = Font(60, Color(0xffffff), "Font2"),
        visual = ColorVisual(Color(0x61daff)).apply {
            transparency = 0.0
        }
    )

    private val playerName4 = TextField(
        posX = 540, posY = 700, width = 600, height = 90,
        text = "",
        prompt = "Name 4: ",
        font = Font(60, Color(0xffffff), "Font2"),
        visual = ColorVisual(Color(0x61daff)).apply {
            transparency = 0.0
        }
    )

    val playerNames = mutableListOf(
        playerName1, playerName2,
        playerName3, playerName4
    )

    private val bot1 = Button(
        posX = 1290, posY = 330, width = 110, height = 95,
        text = "AI", font = Font(50, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER, isWrapText = false,
        visual = ColorVisual(Color(0x0d106b)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.8
        }
    ).apply {
        onMouseClicked = {
            updatePlayerType(0)
        }
    }

    private val bot2 = Button(
        posX = 1290, posY = 453, width = 110, height = 95,
        text = "AI", font = Font(50, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER, isWrapText = false,
        visual = ColorVisual(Color(0x0d106b)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.8
        }
    ).apply {
        onMouseClicked = {
            updatePlayerType(1)
        }
    }

    private val bot3 = Button(
        posX = 1290, posY = 575, width = 110, height = 95,
        text = "AI", font = Font(50, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER, isWrapText = false,
        visual = ColorVisual(Color(0x0d106b)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.8
        }
    ).apply {
        onMouseClicked = {
            updatePlayerType(2)
        }
    }

    private val bot4 = Button(
        posX = 1290, posY = 699, width = 110, height = 95,
        text = "AI", font = Font(50, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER, isWrapText = false,
        visual = ColorVisual(Color(0x0d106b)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.8
        }
    ).apply {
        onMouseClicked = {
            updatePlayerType(3)
        }
    }

    var botButtons = mutableListOf(bot1, bot2, bot3, bot4)

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
            currentVariantIndex = (currentVariantIndex - 1 + gameVariants.size) % gameVariants.size
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
        posX = 430, posY = 330, width = 80, height = 90,
        text = "1",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    ).apply {
        onMouseClicked = {
            cycleOrder(0)
        }
    }

    private val orderButton2 = Button(
        posX = 430, posY = 460, width = 80, height = 90,
        text = "2",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    ).apply {
        onMouseClicked = {
            cycleOrder(1)
        }
    }

    private val orderButton3 = Button(
        posX = 430, posY = 575, width = 80, height = 90,
        text = "3",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    ).apply {
        onMouseClicked = {
            cycleOrder(2)
        }
    }

    private val orderButton4 = Button(
        posX = 430, posY = 700, width = 80, height = 90,
        text = "4",
        font = Font(28, Color(0xFFFFFF), "Font2"),
        visual = ColorVisual(Color(0x162ba0)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    ).apply {
        onMouseClicked = {
            cycleOrder(3)
        }
    }


    init {
        addComponents(
            playerName1, playerName2, playerName3, playerName4,
            bot1, bot2, bot3, bot4,
            smallBoardCheckbox, advancedScoringCheckbox,
            startButton, changeGameLeft, changeGameRight, shufflePlayerNames,
            orderButton1, orderButton2, orderButton3, orderButton4, backButton
        )

        playerNames.forEach {
            it.onKeyReleased =  { updateStartButton() }
        }

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
        if (!playerNamesTexts.any {it.isNotBlank()}) return
        val playerVariantsEnums = playerVariants
            .map {it}
            .slice(0..< playerCount)
            .toMutableList()
        val unshuffledList = playerNamesTexts zip playerVariantsEnums
        val shuffledList = unshuffledList
            .shuffled()
            .map{it}
            .toMutableList()
        while (unshuffledList == shuffledList) {
            shuffledList.shuffle()
        }
        for (index in 0..< playerCount){
            var newType = PlayerType.LOCAL
            if (shuffledList[index].first.isNotBlank()) {
                newType = shuffledList[index].second
            }
            updatePlayer(index, shuffledList[index].first, newType)
        }
    }

    private fun updatePlayer(index: Int, newName: String, newPlayerType : PlayerType) {
        playerNames[index].text = newName
        playerVariants[index] = newPlayerType
        botButtons[index].visual = matchPlayerTypeToColor(playerVariants[index])
    }

    private fun matchPlayerTypeToColor(playerType : PlayerType) : ColorVisual {
        return when (playerType) {
            PlayerType.LOCAL -> ColorVisual(Color(0x0d106b))
            PlayerType.BOTEASY -> ColorVisual(Color(0xFFAF47))
            PlayerType.BOTHARD -> ColorVisual(Color(0xFF4747))
            PlayerType.ONLINE -> error("Online is unreachable in OfflinePlayerScene")
        }
    }
    /**
     * Updates player type if clicked on bot1/bot2/bot3/bot4 button.
     */
    private fun updatePlayerType(buttonNumber : Int) {
        val standardBotNames : MutableList<String> = when (buttonNumber) {
            0 -> {
                mutableListOf("Pasha Biceps", "Sabrina Carpenter")
            }
            1 -> {
                mutableListOf("KennyS", "Rihanna")
            }
            2 -> {
                mutableListOf("S1mple", "Lady Gaga")
            }
            3 -> {
                mutableListOf("Karrigan", "Billie Eilish")
            }
            else -> error("Unreachable bot button number")
        }
        botButtons[buttonNumber].apply {
            when (playerVariants[buttonNumber]) {
                PlayerType.LOCAL ->
                    {
                        updatePlayer(buttonNumber, standardBotNames[0], PlayerType.BOTEASY)
                    }
                PlayerType.BOTEASY ->
                    {
                        updatePlayer(buttonNumber, standardBotNames[1], PlayerType.BOTHARD)
                    }
                PlayerType.BOTHARD ->
                    {
                        updatePlayer(buttonNumber, "", PlayerType.LOCAL)
                    }
                else -> error("Unreachable in playerVariants[buttonNumber]")
            }
        }
        updateStartButton()
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
            playerNames
                .slice(2..3)
                .forEach { it
                    .apply {
                    text = ""
                    isVisible = false
                    }
                }
            botButtons
                .slice(2..3)
                .forEach { it.isVisible = false }

            for (index in 2 .. 3) {
                playerVariants[index] = PlayerType.BOTHARD
                updatePlayerType(index)
            }
        }

        if (playerCount >= 3) {
            smallBoardCheckbox.isChecked = false
            smallBoardCheckbox.isDisabled = true
            smallBoardCheckbox.isVisible = false
            playerNames
                .slice(2..3)
                .forEach { it.isVisible = true }
            botButtons
                .slice(2..3)
                .forEach { it.isVisible = true }
        }

        // Reihenfolge nur bei 4 Spielern
        val orderButtons = listOf(orderButton1, orderButton2, orderButton3, orderButton4)
        orderButtons.forEach { it.isVisible = (playerCount == 4) }

        if (playerCount != 4) {
            // Reset Reihenfolge
            for (i in 0 until 4) playerOrder[i] = i
            updateOrderButtons()
        }

        updateStartButton()
    }

    /**
     * Validates input fields based on the currently selected variant and enables/disables the start button.
     */
    private fun updateStartButton() {
        var isValid = true
        val playerCount = currentVariantIndex + 2
        val playerNameIsNotBlankList = playerNames.map { it.text.isNotBlank() }
        val playerNamesWithoutBlanksList = playerNames.map { it.text }.filter { it.isNotBlank() }
        val playerNamesUniqueList = playerNamesWithoutBlanksList.distinct()

        val countTrue = playerNameIsNotBlankList.filter { it }.size
        val  bool1 = countTrue != playerCount
                || playerNamesWithoutBlanksList.size != playerNamesUniqueList.size

        if (bool1 || (playerCount == 4 && !isOrderValid())) {
            isValid = false
        }

        startButton.isDisabled = !isValid
    }

    /**
     * Constructs the player list based on the variant and starts the game via the GameService.
     */
    fun startGame() {
        val colorsdefault = listOf(
            ColorType.BLUE,
            ColorType.YELLOW,
            ColorType.RED,
            ColorType.GREEN
        )

        val playerCount = currentVariantIndex + 2
        val players = mutableListOf<Triple<String, PlayerType, ColorType>>()

        playerNames.forEachIndexed { index, playerName ->
            if (playerName.text.isNotBlank()) {
                players.add(Triple(playerName.text, playerVariants[index], colorsdefault[index]))
            }
        }

        if (players.map{it.first}.distinct().size != players.size) {
            println("Fehler: Alle Spielernamen müssen eindeutig sein!")
            return
        }

        val names = players.map { it.first }

        if (names.size != names.toSet().size) {
            println("Fehler: Alle Spielernamen müssen eindeutig sein!")
            return
        }

        val gameMode = when (playerCount) {
            2 -> if (smallBoardCheckbox.isChecked) GameMode.TWO_PLAYER_SMALL else GameMode.TWO_PLAYER
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

        rootService.gameService.startNewGame(
            orderedPlayers,
            gameMode,
            scoringStrategy,
            isOnline = false
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