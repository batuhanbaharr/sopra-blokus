package entity
/**
 * Enum to distinguish between the two possible scoring strategies in Blokus.
 * Basic scoring simply count remaining squares, the player
 * with the fewest remaining squares wins.
 * Advanced scoring use a point system with bonus points
 * for placing all pieces.
 */
enum class ScoringStrategy {
    /**
     * Basic scoring: players count the number of squares
     * in their remaining pieces. The player with the lowest
     * number of remaining squares wins.
     */
    BASIC,
    /**
     * Advanced scoring: each remaining square counts as -1 point.
     * A player earns +15 bonus points if all 21 pieces have been placed,
     * plus an additional +5 bonus points if the last placed piece
     * was the 1-square piece. The highest score win.
     */
    ADVANCED, ;
    /**
     * Provides a string representation of this scoring strategy.
     *
     * @return One of: "Basic Scoring Strategy" / "Advanced Scoring Strategy".
     */
    override fun toString() = when(this) {
        BASIC -> "Basic Scoring Strategy"
        ADVANCED -> "Advanced Scoring Strategy"
    }
}