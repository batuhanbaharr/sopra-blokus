package entity

import kotlinx.serialization.Serializable

/**
 * Represent a color in the Blokus game.
 *
 * Each color has:
 * 1) a unique color type
 * 2) a players list, indicating the players who play this color
 * 3) an isActive-property signaling if the color is still playable
 * 4) a sharedPlayerIndex, that shows the index of the player,
 * who is to play this color.
 * 5) blocks-List to store the playable blocks of this color.
 * 6) discardedBlocks-List to store the discarded blocks of this color.
 * 7) lastPlacedByPlayer to store the player who last placed a block of this color.
 * 8) blockBoard and validityBoard to store the next possible
 * placements of the playable blocks of this color.
 *
 * @param colorType The type of the color, see [ColorType].
 */

@Serializable
class Color(val colorType : ColorType) {
    /**
     * Index of the player currently playing the given color
     * in the 3 player (THREE_PLAYER) / 2 player big board (TWO_PLAYER) variant.
     * In THREE_PLAYER game mode -> Takes Int values between 0 and 2.
     * In TWO_PLAYER game mode -> Takes Int values between 0 and 1.
     * In other game mode -> Takes only Int value 0.
     */
    var sharedPlayerIndex: Int = 0

    /**
     * Signaling if the color is still playable.
     */
    var isActive : Boolean = true

    /**
     * List indicating the players who play this color.
     * Consists at least 1 and at most 3 players.
     */
    var players : MutableList<Player> = mutableListOf()

    /**
     * Value storing the player who last placed a block of this color.
     */
    var lastPlacedByPlayer : Player? = null

    /**
     * List with playable blocks of given color.
     * Initialised with 21 blocks.
     */
    var blocks : MutableList<Block> = BlockType.entries.map{Block(it)}.toMutableList()

    /**
     * List with discarded blocks of given color.
     * Initialised empty.
     */
    var discardedBlocks : MutableList<Block> = mutableListOf()

    /**
     * Board that shows the corners in the board,
     * where the playable blocks of this color can be put.
     */
    var validityBoard : Array<IntArray>? = null

    /**
     * Board that shows the neighboring forbidden cells for
     * the discarded blocks of this color.
     */
    var blockingBoard : Array<Array<ColorType>>? = null

    /**
     * creates a copy of the [Color].
     *
     * @return copiedColor
     * */
    fun copy():Color{
        val copiedColor = Color(this.colorType)
        copiedColor.sharedPlayerIndex = this.sharedPlayerIndex
        copiedColor.isActive = this.isActive
        copiedColor.players = this.players.map { it.copy() }.toMutableList()
        copiedColor.lastPlacedByPlayer = this.lastPlacedByPlayer
        copiedColor.blocks = this.blocks.map { it.copy() }.toMutableList()
        copiedColor.discardedBlocks = this.discardedBlocks.map { it.copy() }.toMutableList()
        copiedColor.validityBoard = this.validityBoard?.map { it.copyOf() }?.toTypedArray()
        copiedColor.blockingBoard = this.blockingBoard?.map { it.copyOf() }?.toTypedArray()

        return copiedColor
    }
}