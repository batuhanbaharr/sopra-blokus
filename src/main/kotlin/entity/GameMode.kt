package entity

import edu.udo.cs.sopra.ntf.GameMode
/**
 * Enum to distinguish between the four possible game modes in Blokus.
 */
enum class GameMode {
    /** Four player game */
    FOUR_PLAYER,
    /** Three player game */
    THREE_PLAYER,
    /** Two player game with 20x20 board */
    TWO_PLAYER,
    /** Two player game with 14x14 board */
    TWO_PLAYER_SMALL;
    /**
     * Provides a string representation of this game mode.
     * @return One of: "Four player game", "Three player game",
     * "Two player game with 20x20 board" or "Two player game with 14x14 board".
     */
    override fun toString() = when(this) {
        FOUR_PLAYER -> "4 Players"
        THREE_PLAYER -> "3 Players"
        TWO_PLAYER -> "2 Players"
        TWO_PLAYER_SMALL -> "2 Players with 14x14 board"
    }


}