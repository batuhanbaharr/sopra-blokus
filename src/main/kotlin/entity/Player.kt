package entity

import kotlinx.serialization.Serializable

/**
 * Represent a player in the Blokus game.
 *
 * Each player has a unique name, a playerType that tell if they are
 * human or bot. Score is calculated at the end of the game.
 * Validity board tracks which board positions are valid for this player
 *
 * @param name The name of the player.
 * @param playerType The type of the player, see [PlayerType].
 */
@Serializable
class Player(val name: String, val playerType: PlayerType) {
    /** The score of this player. */
    var score: Int = 0

    /**
     * creates a copy of the [Player].
     *
     * @return copiedPlayer
     * */
    fun copy():Player{
        val copiedPlayer = Player(this.name, this.playerType)
        copiedPlayer.score = this.score
        return copiedPlayer
    }
}