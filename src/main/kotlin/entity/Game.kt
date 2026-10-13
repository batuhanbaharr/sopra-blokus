package entity

import kotlinx.serialization.Serializable

/**
 * Represent Blokus game
 *
 * Hold the current state of the game including the board, the players
 * and whose turn it is. The game support undo/redo through a linked list
 * structure using nextGame and previousGame
 * @param gameMode The mode online or local of the game.
 * @param isOnline The mode online or local of the game.
 * if online -> Network game via BGW-Net. Undo/redo and save/load are disabled.
 * if local -> Local hot-seat game on a single screen.
 * @param scoringStrategy The scoring strategy basic or advanced of the game.
 */
@Serializable
class Game(val gameMode: GameMode, val isOnline: Boolean, val scoringStrategy: ScoringStrategy) {

    /** Index of current color whose turn it is in the [colors] list */
    var currentColorIndex: Int = 0

    /** The list of the playable colors:
     * 2 colors for gameMode = TWO_PLAYER_SMALL,
     * 4 colors of customizable order for 4 players
     * and 4 colors in a defined order for others. */
    var colors: MutableList<Color> = when(gameMode) {
        GameMode.TWO_PLAYER_SMALL -> mutableListOf(
            Color(ColorType.BLUE),
            Color(ColorType.YELLOW)
        )
        GameMode.FOUR_PLAYER -> mutableListOf()
        else -> mutableListOf(
            Color(ColorType.BLUE),
            Color(ColorType.YELLOW),
            Color(ColorType.RED),
            Color(ColorType.GREEN)
        )
    }

    /**
     * The game board as 2D grid of Color values.
     * Each cell is either [ColorType.NONE] (empty) or a player color.
     * Size is 20x20 for standard mode or 14x14 for the 2-player small board variant.
     * Set to null until the game start
     */
    var board: Array<Array<ColorType>>? = null

    /**
     * Indicates the current speed of the bots.
     */
    var botSpeed: Int = 0

    /**
     * Points to the next game state. Used for redo.
     * Null if there is no state to redo.
     */
    @kotlinx.serialization.Transient
    var nextGame: Game? = null

    /**
     * Points to the previous game state. Used for undo.
     * Null if this is the first state also game start.
     */
    @kotlinx.serialization.Transient
    var previousGame: Game? = null

    /**
     * creates a copy of the [Game].
     *
     * @return copiedGame
     * */
    fun copy():Game{
        val copiedGame = Game(this.gameMode, this.isOnline, this.scoringStrategy)
        copiedGame.currentColorIndex = this.currentColorIndex
        copiedGame.colors = this.colors.map { it.copy() }.toMutableList()
        copiedGame.board = this.board?.map { it.copyOf() }?.toTypedArray()
        copiedGame.botSpeed = this.botSpeed
        copiedGame.nextGame = this.nextGame
        copiedGame.previousGame = this.previousGame

        return copiedGame
    }
}