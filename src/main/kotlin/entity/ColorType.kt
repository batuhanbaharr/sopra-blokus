package entity

import edu.udo.cs.sopra.ntf.Color

/**
 * Enum to distinguish between the four possible colors
 * and their absence for the objects in Blokus
 *
 * The four colors are Blue, Yellow, Red and Green.
 * None is used when no color was assigned to an object yet.
 */
enum class ColorType {
    /** No color assigned to this player. */
    NONE,
    /** Blue player color. */
    BLUE,
    /** Yellow player color. */
    YELLOW,
    /** Red player color. */
    RED,
    /** Green player color. */
    GREEN;
    /**
     * Provides a string representation of this color.
     *
     * @return One of: "No Color Assigned",
     * "Blue", "Yellow", "Red", "Green".
     */
    override fun toString() =
        when(this) {
            NONE -> "No Color Assigned"
            BLUE -> "Blue"
            YELLOW -> "Yellow"
            RED -> "Red"
            GREEN -> "Green"
        }

}