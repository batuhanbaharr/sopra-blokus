package entity
/**
 * Enum to distinguish between the four possible player types in Blokus:
 * local player, online player, easy bot and hard bot.
 *
 * Local and Online are human players. BotEasy play random valid moves,
 * while BotHard use strategy algorithm.
 */
enum class PlayerType {
    LOCAL,
    ONLINE,
    BOTEASY,
    BOTHARD, ;
    /**
     * Provide a string representation of this player type.
     *
     * @return One of: "Local Player" / "Online Player" / "Easy Bot" / "Hard Bot".
     */
    override fun toString() =
        when(this) {
            LOCAL -> "Local Player"
            ONLINE -> "Online Player"
            BOTEASY -> "Easy Bot"
            BOTHARD -> "Hard Bot"
        }
}