package gui

import entity.Block
import entity.Game
import entity.Player
import entity.ColorType as PlayerColor
import service.RootService
import service.Refreshable
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import kotlin.collections.indices

/**
 * Enum representing the speed of a bot actions in the GUI
 *
 * @param delayMs Delay in milliseconds before a bot action is executed
 * @param label Display label used in the UI
 */
enum class BotSpeed(val delayMs: Int, val label: String) {
    INSTANT(10, "Instant"),
    FAST(1000, "Fast"),
    SLOW(3000, "Slow");

    /**
     * cycles to the next bot speed
     *
     * @return the next [BotSpeed] value
     */
    fun next(): BotSpeed {
        return when (this) {
            INSTANT -> SLOW
            SLOW -> FAST
            FAST -> INSTANT
        }
    }
}

/**
 * The main game scene displaying the board, the player information, and the block inventory.
 * Listens to service events via [Refreshable] to update the UI dynamically.
 *
 * @param rootService The [RootService] instance to access service methods and the entity layer.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(
    width = 1920,
    height = 1080,
    background = ColorVisual(Color(0x0F0B29))
), Refreshable {

    private var boardSize = 20

    private val cellSize = 36
    private val cellSpacing = 4
    private val stepSize = cellSize + cellSpacing

    private val leftSidebarWidth = 280
    private val rightSidebarWidth = 520
    private val rightSidebarStartX = 1920 - rightSidebarWidth

    private var boardStartX = 0.0
    private var boardStartY = 0.0

    private val boardCells: MutableList<MutableList<TokenView>> = mutableListOf()
    private var selectedBlock: Block? = null

    private val hoveredCells = mutableListOf<Pair<Int, Int>>()

    private var shownPlayerIndex: Int = 0

    // Globale Bot-Geschwindigkeit für die GUI
    private var globalBotSpeed: BotSpeed = BotSpeed.FAST

    private val hintDelayMs = 0
    private var hintsActiveForCurrentTurn = false

    private var currentHoverX: Int? = null
    private var currentHoverY: Int? = null

    private var isUpdatingInventory = false
    private var inventoryUpdateQueued = false

    private val leftSidebarBg = Label(
        posX = 0,
        posY = 0,
        width = leftSidebarWidth,
        height = 1080,
        visual = ColorVisual(Color(0x1A153A))
    )

    val blokusTitle = Button(
        posX = 20, posY = 20, width = leftSidebarWidth - 40, height = 80,
        text = "BLOKUS",
        font = Font(
            size = 40,
            color = Color.WHITE,
            "Font2"
        ),
        visual = ColorVisual(Color(0x261F4C)).apply {
            style.borderRadius = BorderRadius.LARGE
        }
    )

    // Spieler 1
    private val p1NameLabel = Label(
        posX = 15, posY = 10, width = 135, height = 30,
        text = "Player 1",
        font = Font(size = 24, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        alignment = Alignment.CENTER_LEFT
    )
    private val p1ScoreLabel = Label(
        posX = 15, posY = 40, width = 200, height = 40,
        text = "Punkte: 0",
        font = Font(size = 20, color = Color.WHITE),
        alignment = Alignment.CENTER_LEFT
    )
    private val p1BotSpeedButton = createBotSpeedButton()
    private val player1Card = Pane<UIComponent>(
        posX = 20, posY = 120, width = leftSidebarWidth - 40, height = 80,
        visual = ColorVisual(Color(0x2563EB)).apply { style.borderRadius = BorderRadius.MEDIUM }
    ).apply {
        addAll(p1NameLabel, p1ScoreLabel, p1BotSpeedButton)
        isVisible = true
        //onMouseClicked = {  }
    }

    // Spieler 2
    private val p2NameLabel = Label(
        posX = 15, posY = 10, width = 135, height = 30,
        text = "Player 2",
        font = Font(size = 24, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        alignment = Alignment.CENTER_LEFT
    )
    private val p2ScoreLabel = Label(
        posX = 15, posY = 40, width = 200, height = 30,
        text = "Punkte: 0",
        font = Font(size = 20, color = Color.WHITE),
        alignment = Alignment.CENTER_LEFT
    )
    private val p2BotSpeedButton = createBotSpeedButton()
    private val player2Card = Pane<UIComponent>(
        posX = 20, posY = 220, width = leftSidebarWidth - 40, height = 80,
        visual = ColorVisual(Color(0xEAB308)).apply { style.borderRadius = BorderRadius.MEDIUM }
    ).apply {
        addAll(p2NameLabel, p2ScoreLabel, p2BotSpeedButton)
        isVisible = true
        //onMouseClicked = {  }
    }

    // Spieler 3
    private val p3NameLabel = Label(
        posX = 15, posY = 10, width = 135, height = 30,
        text = "Player 3",
        font = Font(size = 24, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        alignment = Alignment.CENTER_LEFT
    )
    private val p3ScoreLabel = Label(
        posX = 15, posY = 40, width = 200, height = 40,
        text = "Punkte: 0",
        font = Font(size = 20, color = Color.WHITE),
        alignment = Alignment.CENTER_LEFT
    )
    private val p3BotSpeedButton = createBotSpeedButton()
    private val player3Card = Pane<UIComponent>(
        posX = 20, posY = 320, width = leftSidebarWidth - 40, height = 80,
        visual = ColorVisual(Color(0xDC2626)).apply { style.borderRadius = BorderRadius.MEDIUM }
    ).apply {
        addAll(p3NameLabel, p3ScoreLabel, p3BotSpeedButton)
        isVisible = true
        //onMouseClicked = {  }
    }

    // Spieler 4
    private val p4NameLabel = Label(
        posX = 15, posY = 10, width = 135, height = 30,
        text = "Player 4",
        font = Font(size = 24, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        alignment = Alignment.CENTER_LEFT
    )
    private val p4ScoreLabel = Label(
        posX = 15, posY = 40, width = 200, height = 40,
        text = "Punkte: 0",
        font = Font(size = 20, color = Color.WHITE),
        alignment = Alignment.CENTER_LEFT
    )
    private val p4BotSpeedButton = createBotSpeedButton()
    private val player4Card = Pane<UIComponent>(
        posX = 20, posY = 420, width = leftSidebarWidth - 40, height = 80,
        visual = ColorVisual(Color(0x16A34A)).apply { style.borderRadius = BorderRadius.MEDIUM }
    ).apply {
        addAll(p4NameLabel, p4ScoreLabel, p4BotSpeedButton)
        isVisible = true
        //onMouseClicked = { }
    }

    private val playerCards = listOf(player1Card, player2Card, player3Card, player4Card)
    private val playerNameLabels = listOf(p1NameLabel, p2NameLabel, p3NameLabel, p4NameLabel)
    private val playerScoreLabels = listOf(p1ScoreLabel, p2ScoreLabel, p3ScoreLabel, p4ScoreLabel)
    private val botSpeedButtons = listOf(p1BotSpeedButton, p2BotSpeedButton, p3BotSpeedButton, p4BotSpeedButton)


    private val undoButton = Button(
        posX = 20, posY = 980, width = 110, height = 70,
        text = "↶",
        font = Font(size = 35, color = Color.WHITE),
        visual = ColorVisual(Color(0x261F4C)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    ).apply {
        onMouseClicked = {
            if (isCurrentPlayerLocal()) {
                rootService.playerActionService.undo()
            }
        }
    }

    private val redoButton = Button(
        posX = 150, posY = 980, width = 110, height = 70,
        text = "↷",
        font = Font(size = 35, color = Color.WHITE),
        visual = ColorVisual(Color(0x261F4C)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    ).apply {
        onMouseClicked = {
            if (isCurrentPlayerLocal()) {
                rootService.playerActionService.redo()
            }
        }
    }

    private val rightSidebarBg = Label(
        posX = rightSidebarStartX,
        posY = 0,
        width = rightSidebarWidth,
        height = 1080,
        visual = ColorVisual(Color(0x1A153A))
    )

    private val inventoryDashboardBg = Label(
        posX = rightSidebarStartX + 20,
        posY = 20,
        width = rightSidebarWidth - 40,
        height = 650,
        visual = ColorVisual(Color(0x261F4C)).apply {
            style.borderRadius = BorderRadius.LARGE
        }
    )

    private val inventoryPane = Pane<Pane<TokenView>>(
        posX = rightSidebarStartX + 30,
        posY = 30,
        width = rightSidebarWidth - 60,
        height = 630
    )

    private val previewDashboardBg = Label(
        posX = rightSidebarStartX + 20,
        posY = 690,
        width = rightSidebarWidth - 40,
        height = 370,
        visual = ColorVisual(Color(0x261F4C)).apply {
            style.borderRadius = BorderRadius.LARGE
        }
    )

    private val previewPane = Pane<TokenView>(
        posX = rightSidebarStartX + 135,
        posY = 710,
        width = 250,
        height = 250
    )

    //Rotate, Mirror
    private val rotateButton = Button(
        posX = rightSidebarStartX + 132, posY = 980, width = 120, height = 60,
        text = "⟳",
        font = Font(size = 35, color = Color.WHITE),
        visual = ColorVisual(Color(0x3B336A)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    ).apply { onMouseClicked ={
        if (isCurrentPlayerLocal()) {
            val block = selectedBlock
            if (block != null) {
                rootService.playerActionService.rotateBlock(block, true)
                updatePreviewWindow(block.blueprint)
                val x = currentHoverX
                val y = currentHoverY

                if (x != null && y != null) {
                    showHoverPreview(x, y)
                }
            }
        }
    }}


    private val mirrorButton = Button(
        posX = rightSidebarStartX + 265, posY = 980, width = 120, height = 60,
        text = "⇆",
        font = Font(size = 35, color = Color.WHITE),
        visual = ColorVisual(Color(0x3B336A)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    ).apply { onMouseClicked ={
        if (isCurrentPlayerLocal()) {
            val block = selectedBlock
            if (block != null) {
                rootService.playerActionService.mirrorBlock(block, true)
                updatePreviewWindow(block.blueprint)
                val x = currentHoverX
                val y = currentHoverY

                if (x != null && y != null) {
                    showHoverPreview(x, y)
                }
            }
        }
    }}

    private val endGameEarlyButton = Button(
        posX = 20, posY = 890, width = 240, height = 70,
        text = "End Game Early",
        font = Font(size = 20, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(Color(0xDC2626)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    ).apply {
        isVisible = false
        onMouseClicked = {
            if (isCurrentPlayerLocal()) {
                rootService.gameService.endGame()
            }
        }
    }


    init {

        addComponents(
            leftSidebarBg,

            player1Card,
            player2Card,
            player3Card,
            player4Card,

            undoButton,
            redoButton,
            blokusTitle,

            rightSidebarBg,
            inventoryDashboardBg,
            inventoryPane,
            previewDashboardBg,
            previewPane,

            endGameEarlyButton,
            rotateButton,
            mirrorButton,
        )

        playerCards.forEachIndexed { index, card ->
            card.onMouseClicked = {
                shownPlayerIndex = index
                createInventory()
            }
        }

        createPreviewGrid()

        this.onKeyPressed = { event ->
            if (isCurrentPlayerLocal()) {
                when (event.keyCode) {
                    tools.aqua.bgw.event.KeyCode.R -> {
                        selectedBlock?.let { block ->
                            rootService.playerActionService.rotateBlock(block, true)
                            updatePreviewWindow(block.blueprint)
                            val x = currentHoverX
                            val y = currentHoverY

                            if (x != null && y != null) {
                                showHoverPreview(x, y)
                            }
                        }
                    }
                    tools.aqua.bgw.event.KeyCode.W -> {
                        selectedBlock?.let { block ->
                            rootService.playerActionService.mirrorBlock(block, true)
                            updatePreviewWindow(block.blueprint)
                            val x = currentHoverX
                            val y = currentHoverY

                            if (x != null && y != null) {
                                showHoverPreview(x, y)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Updates Bot speed labeling
     */
    private fun cycleBotSpeed() {
        globalBotSpeed = globalBotSpeed.next()
        updatePlayerCards()
    }

    /**
     * creates Bot speed buttons
     */
    private fun createBotSpeedButton() : Button {
        return Button(
            posX = 155, posY = 10, width = 75, height = 30, text = "Fast",
            font = Font(size = 14, color = Color.WHITE, fontWeight = Font.FontWeight.BOLD),
            visual = ColorVisual(Color(0x444444)).apply { style.borderRadius = BorderRadius.SMALL }
        ).apply {
            isVisible = false
            onMouseClicked = { cycleBotSpeed() }
        }
    }

    /**
     * Updates the player cards dynamically based on the current game state.
     * Hides cards of non-participating players and highlights the active color.
     */
    private fun updatePlayerCards() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return

        for (i in 0 until 4) {
            if (i >= game.colors.size) {
                playerCards[i].isVisible = false
                continue
            }

            updateSinglePlayerCard(i, game)
        }
    }

    /**
     * updates a single player card based on the current game state
     *
     * @param i Index of the player card (0-3)
     * @param game The current game instance
     */
    private fun updateSinglePlayerCard(i: Int, game: Game) {
        val colorEntity = game.colors[i]

        playerCards[i].isVisible = true

        val player = resolvePlayer(colorEntity, game)
        updatePlayerText(i, player)
        updateCardColor(i, colorEntity)
        updateActiveHighlight(i, game)
        updateBotButton(i, player)
    }

    /**
     * checks if GameMode.THREE_PLAYER and updates visibilty for end game early button
     */
    private fun updateEndGameEarlyButtonVisibility() {
        val game = runCatching { rootService.currentGame }.getOrNull()

        if (game == null || game.gameMode != entity.GameMode.THREE_PLAYER) {
            endGameEarlyButton.isVisible = false
            return
        }

        var allPrimaryColorsFinished = true
        var sharedColorIsActive = false

        for (color in game.colors) {

            // "primary colors"
            if (color.players.size == 1) {
                if (color.isActive) {
                    allPrimaryColorsFinished = false
                }
            }
            // shared color logic
            if (color.players.size > 1) {
                if (color.isActive) {
                    sharedColorIsActive = true
                }
            }
        }

        if (allPrimaryColorsFinished && sharedColorIsActive) {
            endGameEarlyButton.isVisible = true
        } else {
            endGameEarlyButton.isVisible = false
        }
    }

    /**
     * Resolves the correct player for a given color entity
     * --> if 3 Player Mode -> Color can be shared
     *
     * @param colorEntity The color entity containing one or more players
     * @param game The current game instance
     * @return The resolved [Player] or null if none exists
     */
    private fun resolvePlayer(colorEntity: entity.Color, game: Game): Player? {
        val isShared = game.gameMode == entity.GameMode.THREE_PLAYER &&
                colorEntity.players.size > 1

        return if (isShared) {
            colorEntity.players.getOrNull(colorEntity.sharedPlayerIndex)
        } else {
            colorEntity.players.getOrNull(0)
        }
    }

    /**
     * Updates the name and score labels for a player card
     *
     * @param i Index of the player card
     * @param player The player whose data should be displayed
     */
    private fun updatePlayerText(i: Int, player: Player?) {
        playerNameLabels[i].text = player?.name ?: "Unknown"
        playerScoreLabels[i].text = "Punkte: ${player?.score ?: 0}"
    }

    /**
     * Updates the background color of a player card
     *
     * @param i Index of the player card
     * @param colorEntity The color entity defining the player's color
     */
    private fun updateCardColor(i: Int, colorEntity: entity.Color) {
        val cardColor = when (colorEntity.colorType) {
            PlayerColor.BLUE -> Color(0x2563EB)
            PlayerColor.YELLOW -> Color(0xEAB308)
            PlayerColor.RED -> Color(0xDC2626)
            PlayerColor.GREEN -> Color(0x16A34A)
            else -> Color(0x666666)
        }

        playerCards[i].visual = ColorVisual(cardColor).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    }

    /**
     * Highlights the active player's card by adjusting opacity
     *
     * @param i Index of the player card
     * @param game The current game instance
     *
     */
    private fun updateActiveHighlight(i: Int, game: Game) {
        playerCards[i].opacity = if (i == game.currentColorIndex) 1.0 else 0.4
    }

    /**
     * Updates the bot speed button visibility and label
     *
     * @param i Index of the player card
     * @param player The player associated with the card
     */
    private fun updateBotButton(i: Int, player: Player?) {
        val isBot = player?.playerType == entity.PlayerType.BOTEASY ||
                player?.playerType == entity.PlayerType.BOTHARD

        botSpeedButtons[i].isVisible = isBot

        if (isBot) {
            botSpeedButtons[i].text = globalBotSpeed.label
        }
    }

    /**
     * Initializes the game board grid with the specified size
     *
     * @param size The amount of rows and columns for the game board
     */
    private fun initializeBoard(size: Int) {
        resetBoard(size)
        calculateBoardPosition()
        createBoardCells()
    }

    /**
     * Clears the current board and prepares it for a new size
     *
     * @param size The new board size
     */
    private fun resetBoard(size: Int) {
        boardCells.flatten().forEach { removeComponents(it) }
        boardCells.clear()
        boardSize = size
    }

    /**
     * Calculates the starting position of the board so it is centered on the screen
     */
    private fun calculateBoardPosition() {
        val boardPixelSize = boardSize * stepSize - cellSpacing
        val availableCenterWidth = 1920 - leftSidebarWidth - rightSidebarWidth

        boardStartX = leftSidebarWidth + (availableCenterWidth - boardPixelSize) / 2.0
        boardStartY = (1080 - boardPixelSize) / 2.0
    }

    /**
     * Creates all board cells and adds them to the scene
     */
    private fun createBoardCells() {
        for (y in 0 until boardSize) {
            val row = mutableListOf<TokenView>()

            for (x in 0 until boardSize) {
                val cell = createSingleCell(x, y)
                row.add(cell)
                addComponents(cell)
            }

            boardCells.add(row)
        }
    }

    /**
     * Creates a single board cell including all mouse interactions
     *
     * @param x X-coordinate in the grid
     * @param y Y-coordinate in the grid
     * @return The created [TokenView]
     */
    private fun createSingleCell(x: Int, y: Int): TokenView {
        return TokenView(
            posX = boardStartX + x * stepSize,
            posY = boardStartY + y * stepSize,
            width = cellSize,
            height = cellSize,
            visual = ColorVisual(Color(0x1C173D)).apply {
                style.borderRadius = BorderRadius.SMALL
            }
        ).apply {
            onMouseEntered = { handleMouseEntered(x, y) }
            onMouseExited = { handleMouseExited() }
            onMouseClicked = { handleMouseClicked(x, y) }
        }
    }

    /**
     * Handles mouse entered events on a board cell
     *
     * @param x The x-coordinate of the clicked cell
     * @param y The y-coordinate of the clicked cell
     */
    private fun handleMouseEntered(x: Int, y: Int) {
        if (isCurrentPlayerLocal()) {
            currentHoverX = x
            currentHoverY = y
            showHoverPreview(x, y)
        }
    }

    /**
     * Handles mouse exited events on a board cell
     */
    private fun handleMouseExited() {
        currentHoverX = null
        currentHoverY = null
        clearHoverPreview()
    }

    /**
     * Handles mouse click events on a board cell
     *
     * If a block is selected, it calculates the correct placement position
     * based on the block's blueprint and places it on the board
     *
     * @param x The x-coordinate of the clicked cell
     * @param y The y-coordinate of the clicked cell
     */
    private fun handleMouseClicked(x: Int, y: Int) {
        if (!isCurrentPlayerLocal()) return

        val block = selectedBlock
        if (block == null) {
            println("Kein Block ausgewählt!")
            return
        }

        val blueprint = block.blueprint

        val (targetX, targetY) = calculatePlacementPosition(x, y, blueprint)

        rootService.playerActionService.placeBlock(block, Pair(targetX, targetY))
    }

    /**
     * Calculates the target position for placing a block on the board
     *
     * @param x The clicked x-coordinate
     * @param y The clicked y-coordinate
     * @param blueprint The block's shape matrix
     * @return A pair containing the calculated target coordinates
     */
    private fun calculatePlacementPosition(
        x: Int,
        y: Int,
        blueprint: Array<IntArray>
    ): Pair<Int, Int> {

        val centerOffsetX = blueprint[0].size / 2
        val centerOffsetY = blueprint.size / 2

        val (firstFilledX, firstFilledY) = findFirstFilledCell(blueprint)

        val targetX = x - centerOffsetX + firstFilledX
        val targetY = y - centerOffsetY + firstFilledY

        return Pair(targetX, targetY)
    }

    /**
     * Finds the top-left most filled cell (value = 1) in a blueprint
     *
     * This is used to correctly align the block when placing it
     *
     * @param blueprint The block's shape matrix
     * @return A pair containing the coordinates of the first filled cell
     */
    private fun findFirstFilledCell(blueprint: Array<IntArray>): Pair<Int, Int> {

        val filledCells = blueprint.flatMapIndexed { row, cols ->
            cols.withIndex().mapNotNull { (col, value) ->
                if (value == 1) row to col else null
            }
        }

        val firstFilledY = filledCells.minOfOrNull { it.first } ?: blueprint.size
        val firstFilledX = filledCells.minOfOrNull { it.second } ?: blueprint[0].size

        return Pair(firstFilledX, firstFilledY)
    }

    /**
     * Creates the preview grid for the currently selected block
     */
    private fun createPreviewGrid() {
        previewPane.clear()
        for (y in 0 until 5) {
            for (x in 0 until 5) {
                val cell = TokenView(
                    posX = x * 50,
                    posY = y * 50,
                    width = 46,
                    height = 46,
                    visual = ColorVisual(Color(0x1A153A)).apply {
                        style.borderRadius = BorderRadius.SMALL
                    }
                )
                previewPane.add(cell)
            }
        }
    }

    /**
     * Creates the inventory containing all available blocks for a player
     */
    private fun createInventory() {
        if (isUpdatingInventory) {
            inventoryUpdateQueued = true
            return
        }

        isUpdatingInventory = true

        try {
            val game = runCatching { rootService.currentGame }.getOrNull() ?: return
            val colorEntity = game.colors.getOrNull(shownPlayerIndex) ?: return

            inventoryPane.clear()

            val layout = calculateInventoryLayout()
            val playerColor = getColorForPlayer(shownPlayerIndex)

            colorEntity.blocks.forEachIndexed { index, block ->
                val blockPane = createBlockPane(index, layout, block, game)
                fillBlockPane(blockPane, block, playerColor)
                inventoryPane.add(blockPane)
            }

        } finally {
            isUpdatingInventory = false

            if (inventoryUpdateQueued) {
                inventoryUpdateQueued = false
                createInventory()
            }
        }
    }

    /**
     * Calculates and returns the layout configuration for the inventory grid
     *
     * @return An [InventoryLayout] containing all layout parameters
     */
    private fun calculateInventoryLayout(): InventoryLayout {
        val cols = 3
        val miniCellSize = 18
        val matrixSize = 5 * miniCellSize
        val xSpacing = 150
        val ySpacing = 90

        val startX = (inventoryPane.width - (cols * xSpacing)) / 2 + 30
        val startY = 10

        return InventoryLayout(cols, matrixSize, xSpacing, ySpacing, startX,
            startY, miniCellSize)
    }

    /**
     * Represents the layout configuration for the inventory grid.
     *
     * @property cols Number of columns in the inventory grid
     * @property matrixSize The width and height of each block container (based on a 5x5 matrix)
     * @property xSpacing Horizontal spacing between block containers
     * @property ySpacing Vertical spacing between block containers
     * @property startX The starting X-coordinate for the inventory grid inside the pane
     * @property startY The starting Y-coordinate for the inventory grid inside the pane
     * @property miniCellSize The size of a single cell within a block's mini representation
     */
    data class InventoryLayout(
        val cols: Int,
        val matrixSize: Int,
        val xSpacing: Int,
        val ySpacing: Int,
        val startX: Double,
        val startY: Int,
        val miniCellSize: Int
    )

    /**
     * Creates a container pane for a single block in the inventory
     *
     * The pane is positioned according to the given layout and index
     * and registers a click listener to select the corresponding block
     *
     * @param index The index of the block in the inventory list
     * @param layout The layout configuration used for positioning
     * @param block The block represented by this pane
     * @param game The current game instance
     *
     * @return A configured [Pane] representing the block container
     */
    private fun createBlockPane(
        index: Int,
        layout: InventoryLayout,
        block: Block,
        game: Game
    ): Pane<TokenView> {

        val col = index % layout.cols
        val row = index / layout.cols

        return Pane<TokenView>(
            posX = layout.startX + col * layout.xSpacing,
            posY = layout.startY + row * layout.ySpacing,
            width = layout.matrixSize,
            height = layout.matrixSize
        ).apply {
            onMouseClicked = {
                if (shownPlayerIndex == game.currentColorIndex && isCurrentPlayerLocal()) {
                    selectedBlock = block
                    updatePreviewWindow(block.blueprint)
                }
            }
        }
    }

    /**
     * Fills a block pane with visual cells based on the block's blueprint
     *
     * @param pane The pane to fill with visual cells
     * @param block The block providing the blueprint
     * @param playerColor The color used to render the block
     */
    private fun fillBlockPane(
        pane: Pane<TokenView>,
        block: Block,
        playerColor: Color
    ) {
        val blueprint = block.blueprint
        val size = pane.width / 5

        blueprint.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (cell == 1) {
                    pane.add(createMiniCell(x, y, size, playerColor))
                }
            }
        }
    }

    /**
     * Creates a single mini cell used to render part of a block in the inventory
     *
     * @param x The x-coordinate within the block matrix
     * @param y The y-coordinate within the block matrix
     * @param size The size of the mini cell in pixels
     * @param color The color used to render the cell
     *
     * @return A configured [TokenView] representing a mini block cell
     */
    private fun createMiniCell(
        x: Int,
        y: Int,
        size: Double,
        color: Color
    ): TokenView {
        return TokenView(
            posX = x * size,
            posY = y * size,
            width = size - 1,
            height = size - 1,
            visual = ColorVisual(color).apply {
                style.borderRadius = BorderRadius.SMALL
            }
        )
    }

    /**
     * Updates the preview based on the selected block blueprint
     *
     * @param blueprint The structural array representing the shape of the block
     */
    private fun updatePreviewWindow(blueprint: Array<IntArray>?) {
        resetPreview()

        if (blueprint == null) return

        val playerColor = getColorForPlayer(rootService.currentGame.currentColorIndex)
        applyBlueprintToPreview(blueprint, playerColor)
    }

    /**
     * Resets all preview cells to their default appearance
     */
    private fun resetPreview() {
        previewPane.components.forEach {
            it.visual = ColorVisual(Color(0x1A153A)).apply {
                style.borderRadius = BorderRadius.SMALL
            }
        }
    }

    /**
     * Applies a block blueprint to the preview grid
     *
     * @param blueprint The block shape matrix
     * @param color The color used for rendering
     */
    private fun applyBlueprintToPreview(
        blueprint: Array<IntArray>,
        color: Color
    ) {
        val center = 2

        blueprint.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (cell == 1) {
                    applyPreviewCell(x, y, blueprint, center, color)
                }
            }
        }
    }

    /**
     * Colors a single preview cell if it is within bounds
     */
    private fun applyPreviewCell(
        x: Int,
        y: Int,
        blueprint: Array<IntArray>,
        center: Int,
        color: Color
    ) {
        val targetY = center + y - (blueprint.size / 2)
        val targetX = center + x - (blueprint[y].size / 2)

        if (!isValidPreviewPosition(targetX, targetY)) return

        previewPane.components[targetY * 5 + targetX].visual =
            ColorVisual(color).apply {
                style.borderRadius = BorderRadius.SMALL
            }
    }

    /**
     * Checks whether a preview cell position is within the 5x5 preview grid
     */
    private fun isValidPreviewPosition(x: Int, y: Int): Boolean {
        return x in 0..4 && y in 0..4
    }

    /**
     * Displays a temporary hover preview of the selected block on the game board
     *
     * @param gridX The x-coordinate of the targeted cell
     * @param gridY The y-coordinate of the targeted cell
     */
    private fun showHoverPreview(gridX: Int, gridY: Int) {
        clearHoverPreview()

        val block = selectedBlock ?: return
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return
        val board = game.board ?: return

        val playerColor = getColorForPlayer(game.currentColorIndex)

        applyHoverPreview(block.blueprint, gridX, gridY, board, playerColor)
    }

    /**
     * Applies hover preview for a block on the board
     */
    private fun applyHoverPreview(
        blueprint: Array<IntArray>,
        gridX: Int,
        gridY: Int,
        board: Array<Array<PlayerColor>>,
        playerColor: Color
    ) {
        val offsetX = blueprint[0].size / 2
        val offsetY = blueprint.size / 2

        blueprint.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (cell == 1) {
                    applyHoverCell(
                        CellPos(x, y),
                        CellPos(gridX, gridY),
                        Offset(0, offsetX, offsetY),
                        HoverContext(board, playerColor)
                    )
                }
            }
        }
    }

    /**
     * Represents a position of a cell within a grid or blueprint
     *
     * @property x The horizontal coordinate
     * @property y The vertical coordinate
     */
    data class CellPos(val x: Int, val y: Int)

    /**
     * Represents the offset used to align a block relative to a grid position
     *
     * @property x The horizontal coordinate
     * @property xOffset The horizontal offset
     * @property yOffset The vertical offset
     */
    data class Offset(val x: Int, val xOffset: Int, val yOffset: Int)

    /**
     * Bundles all contextual information required to render a hover preview
     *
     * @property board The current game board state
     * @property playerColor The color used for rendering the hover preview
     */
    data class HoverContext(
        val board: Array<Array<PlayerColor>>,
        val playerColor: Color
    )

    /**
     * Applies hover styling to a single cell
     */
    private fun applyHoverCell(
        cell: CellPos,
        grid: CellPos,
        offset: Offset,
        context: HoverContext
    ) {
        val targetX = grid.x + cell.x - offset.xOffset
        val targetY = grid.y + cell.y - offset.yOffset

        if (!isInBounds(targetX, targetY)) return

        val cellView = boardCells[targetY][targetX]

        if (context.board[targetY][targetX] != PlayerColor.NONE) {
            applyInvalidHoverStyle(cellView)
        } else {
            applyValidHoverStyle(cellView, context.playerColor)
        }

        cellView.opacity = 0.5
        hoveredCells.add(targetX to targetY)
    }

    /**
     * Applies the visual styling for an invalid hover cell
     *
     * @param cell The [TokenView] representing the board cell to update
     */
    private fun applyInvalidHoverStyle(cell: TokenView) {
        cell.visual = ColorVisual(Color.GRAY).apply {
            style.borderRadius = BorderRadius.SMALL
        }
    }

    /**
     * Applies the visual styling for a valid hover cell
     *
     * @param cell The [TokenView] representing the board cell to update
     * @param color The color used to render the hover preview
     */
    private fun applyValidHoverStyle(cell: TokenView, color: Color) {
        cell.visual = ColorVisual(color).apply {
            style.borderRadius = BorderRadius.SMALL
        }
    }

    /**
     * Clears the currently displayed hover preview from the game board
     */
    private fun clearHoverPreview() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return
        val board = game.board ?: return
        val validityBoard = game.colors
            .getOrNull(game.currentColorIndex)?.validityBoard

        for ((x, y) in hoveredCells) {
            if (!isInBounds(x, y)) continue

            val cellValue = board[y][x]
            val cellView = boardCells[y][x]

            if (shouldShowHint(cellValue, validityBoard, x, y)) {
                applyHintStyle(cellView, game)
            } else {
                applyDefaultStyle(cellView, cellValue)
            }
        }

        hoveredCells.clear()
    }

    /**
     * Determines whether a hint should be displayed for a specific board cell.
     *
     * @param cellValue The current value of the cell on the board
     * @param validityBoard A matrix indicating valid placement positions (1 = valid)
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return True if a hint should be displayed for this cell, false otherwise
     */
    private fun shouldShowHint(
        cellValue: PlayerColor,
        validityBoard: Array<IntArray>?,
        x: Int,
        y: Int
    ): Boolean {
        return hintsActiveForCurrentTurn &&
                cellValue == PlayerColor.NONE &&
                validityBoard?.get(y)?.get(x) == 1
    }

    /**
     * Checks whether the given coordinates are within the bounds of the game board.
     *
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @return True if the coordinates are inside the board, false otherwise
     */
    private fun isInBounds(x: Int, y: Int): Boolean {
        return x in 0 until boardSize && y in 0 until boardSize
    }

    /**
     * Applies the visual styling for a hint cell.
     *
     * @param cellView The [TokenView] to update
     * @param game The current game instance
     */
    private fun applyHintStyle(cellView: TokenView, game: Game) {
        cellView.visual = ColorVisual(getColorForPlayer(game.currentColorIndex)).apply {
            style.borderRadius = BorderRadius.SMALL
        }
        cellView.opacity = 0.2
    }

    /**
     * Applies the default visual styling to a board cell.
     *
     * @param cellView The [TokenView] to update
     * @param cellValue The value representing the occupying player color
     */
    private fun applyDefaultStyle(cellView: TokenView, cellValue: PlayerColor) {
        val color = getColorForCell(cellValue)

        cellView.visual = ColorVisual(color).apply {
            style.borderRadius = BorderRadius.SMALL
        }
        cellView.opacity = 1.0
    }

    /**
     * Returns the display color for a given cell value.
     *
     * Maps the internal player color representation to a corresponding UI color.
     *
     * @param cellValue The player color stored in the board cell
     * @return The corresponding [Color] for rendering
     */
    private fun getColorForCell(cellValue: PlayerColor): Color {
        return when (cellValue) {
            PlayerColor.BLUE -> Color(0x2563EB)
            PlayerColor.YELLOW -> Color(0xEAB308)
            PlayerColor.RED -> Color(0xDC2626)
            PlayerColor.GREEN -> Color(0x16A34A)
            else -> Color(0x1C173D)
        }
    }

    /**
     * Gets the Colors of each player for the pieces
     */
    private fun getColorForPlayer(index: Int): Color {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return Color(0x3B82F6)
        if (index >= game.colors.size) return Color(0x3B82F6)

        return when (game.colors[index].colorType) {
            PlayerColor.BLUE -> Color(0x2563EB)
            PlayerColor.YELLOW -> Color(0xEAB308)
            PlayerColor.RED -> Color(0xDC2626)
            PlayerColor.GREEN -> Color(0x16A34A)
            else -> Color(0x3B82F6)
        }
    }

    /**
     * Each cell of the internal board matrix is mapped to its corresponding [TokenView]
     * and colored according to the player who occupies it
     */
    private fun drawBoard() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return

        val board = game.board ?: return

        for (y in board.indices) {
            for (x in board[y].indices) {

                val cellValue = board[y][x]
                val cellView = boardCells[y][x]

                val color = when (cellValue) {
                    PlayerColor.BLUE -> Color(0x2563EB)
                    PlayerColor.YELLOW -> Color(0xEAB308)
                    PlayerColor.RED -> Color(0xDC2626)
                    PlayerColor.GREEN -> Color(0x16A34A)
                    else -> Color(0x1C173D)
                }

                cellView.visual = ColorVisual(color).apply {
                    style.borderRadius = BorderRadius.SMALL
                }

                // Allow hover events even on placed blocks
                cellView.isDisabled = false
                cellView.opacity = 1.0
            }
        }
    }

    /**
     * Draws hints on the board based on the active player's validityBoard
     */
    private fun drawHints() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return

        val board = game.board ?: return
        val validityBoard = game.colors
            .getOrNull(game.currentColorIndex)?.validityBoard ?: return

        val playerColor = getColorForPlayer(game.currentColorIndex)

        applyHints(board, validityBoard, playerColor)
    }

    /**
     * Iterates over the board and applies hint styling where valid
     */
    private fun applyHints(
        board: Array<Array<PlayerColor>>,
        validityBoard: Array<IntArray>,
        playerColor: Color
    ) {
        board.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (shouldApplyHint(cell, validityBoard, x, y)) {
                    applyHintToCell(x, y, playerColor)
                }
            }
        }
    }

    /**
     * Checks whether a hint should be applied to a specific cell
     */
    private fun shouldApplyHint(
        cell: PlayerColor,
        validityBoard: Array<IntArray>,
        x: Int,
        y: Int
    ): Boolean {
        return cell == PlayerColor.NONE && validityBoard[y][x] == 1
    }

    /**
     * Applies hint styling to a specific board cell
     */
    private fun applyHintToCell(
        x: Int,
        y: Int,
        playerColor: Color
    ) {
        val cellView = boardCells[y][x]

        cellView.visual = ColorVisual(playerColor).apply {
            style.borderRadius = BorderRadius.SMALL
        }
        cellView.opacity = 0.2
    }


    /**
     * Triggers the display of hints after a delay.
     */
    private fun triggerHints() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return

        val expectedIndex = game.currentColorIndex

        // Beim Start des Zugs sind die Hints gesperrt
        hintsActiveForCurrentTurn = false

        if (hintDelayMs > 0) {
            playAnimation(
                tools.aqua.bgw.animation.DelayAnimation(duration = hintDelayMs).apply {
                    onFinished = {
                        val currentGame = runCatching { rootService.currentGame }.getOrNull()
                        if (currentGame?.currentColorIndex == expectedIndex) {
                            hintsActiveForCurrentTurn = true
                            drawHints()
                        }
                    }
                }
            )
        } else {
            hintsActiveForCurrentTurn = true
            drawHints()
        }
    }

    /**
     * Determines whether the current player is a local (human) player
     *
     * @return True if the current player is of type LOCAL, false otherwise
     */
    private fun isCurrentPlayerLocal(): Boolean {
        return runCatching {
            rootService.gameService.getCurrentPlayer().playerType == entity.PlayerType.LOCAL
        }.getOrDefault(false)
    }

    /**
     * Delays the actions inside the refreshables if the player is a bot, depending on the bot speed
     */
    private fun executeWithBotDelay(action: () -> Unit) {
        val game = runCatching { rootService.currentGame }.getOrNull()

        if (game != null && globalBotSpeed.delayMs > 0) {

            val prevIndex = (game.currentColorIndex - 1 + game.colors.size) % game.colors.size
            val prevColor = game.colors[prevIndex]
            val prevPlayer = prevColor.players.getOrNull(prevColor.sharedPlayerIndex)
            val isPrevBot = prevPlayer?.playerType == entity.PlayerType.BOTEASY ||
                    prevPlayer?.playerType == entity.PlayerType.BOTHARD

            val currColor = game.colors[game.currentColorIndex]
            val currPlayer = currColor.players.getOrNull(currColor.sharedPlayerIndex)
            val isCurrBot = currPlayer?.playerType == entity.PlayerType.BOTEASY ||
                    currPlayer?.playerType == entity.PlayerType.BOTHARD

            if (isPrevBot || isCurrBot) {
                playAnimation(
                    tools.aqua.bgw.animation.DelayAnimation(duration =
                        globalBotSpeed.delayMs).apply {
                        onFinished = { action() }
                    }
                )
                return
            }
        }

        action()
    }

    /**
     * Checks whether the current player is a bot and, if so, triggers its turn.
     *
     * If the active player is of type BOT (easy or hard), the bot action is executed
     * after a delay defined by the currently selected [BotSpeed]. The delay is handled
     */
    private fun triggerBotIfNeeded() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return


        val currentColor = game.colors[game.currentColorIndex]
        val player = currentColor.players.getOrNull(currentColor.sharedPlayerIndex)

        val isBot = player?.playerType == entity.PlayerType.BOTEASY ||
                player?.playerType == entity.PlayerType.BOTHARD

        if (isBot) {
            // GUI-Delay nutzen (dein bestehendes System)
            playAnimation(
                tools.aqua.bgw.animation.DelayAnimation(duration =
                    globalBotSpeed.delayMs).apply {
                    onFinished = {
                        rootService.botService.calculateBotAction()
                    }
                }
            )
        }
    }

    // --- Refreshables ---

    override fun refreshAfterGameLoaded() {
        val game = runCatching { rootService.currentGame }
            .getOrNull() ?: return


        if (game.isOnline) {
            undoButton.isDisabled = true
            undoButton.opacity = 0.5
            redoButton.isDisabled = true
            redoButton.opacity = 0.5
        } else {
            undoButton.isDisabled = false
            undoButton.opacity = 1.0
            redoButton.isDisabled = false
            redoButton.opacity = 1.0
        }
        shownPlayerIndex = game.currentColorIndex
        boardSize = game.board?.size ?: 20
        initializeBoard(boardSize)
        updatePlayerCards()
        drawBoard()
        createInventory()
        triggerHints()
        triggerBotIfNeeded()
        updateEndGameEarlyButtonVisibility()
    }

    override fun refreshAfterBlockPlaced() {
        executeWithBotDelay {
            selectedBlock = null
            updatePreviewWindow(null)
            updatePlayerCards()
            createInventory()
            drawBoard()
        }

    }

    override fun refreshSingleBlock(block: Block) {
        executeWithBotDelay {
            updatePlayerCards()
            createInventory()
        }
    }

    override fun refreshAfterGameEnd(ranking : MutableList<Pair<String,Int>>) {
        executeWithBotDelay {
            updatePlayerCards()
        }
    }

    override fun refreshAfterTurnEnd() {
        executeWithBotDelay {
            val game = runCatching { rootService.currentGame }
                .getOrNull() ?: return@executeWithBotDelay


            shownPlayerIndex = game.currentColorIndex
            selectedBlock = null
            updatePreviewWindow(null)
            updatePlayerCards()
            createInventory()
            drawBoard()
            triggerHints()
            triggerBotIfNeeded()
            updateEndGameEarlyButtonVisibility()
        }
    }
}