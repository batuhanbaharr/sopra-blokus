package gui

import service.RootService
import service.Refreshable
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import entity.ColorType

/**
 * Scene that displays teh final ranking after a game has ended
 *
 * it shows up to four players sorted by their final score
 *
 * @param rootService The [RootService] instance used to access the current game state and other services
 */
class GameFinishedScene(private val rootService: RootService) :
    BoardGameScene(1920, 1080, background = ImageVisual("GameFinishedRanking.png")),
    Refreshable {

    //Player
    private val place1 = createPlayerLabel(500)
    private val place2 = createPlayerLabel(600)
    private val place3 = createPlayerLabel(700)
    private val place4 = createPlayerLabel(800)

    //score
    private val score1 = createScoreLabel(500)
    private val score2 = createScoreLabel(600)
    private val score3 = createScoreLabel(700)
    private val score4 = createScoreLabel(800)

    private val playerLabels = listOf(place1, place2, place3, place4)
    private val scoreLabels = listOf(score1, score2, score3, score4)

    val exitButton = Button(
        posX = 850,
        posY = 950,
        width = 270,
        height = 60,
        text = "Exit",
        font = Font(40, Color.WHITE, "Font2"),
        visual = ColorVisual(Color(0x85ffcc)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    )

    init {
        addComponents(
            place1, place2, place3, place4,
            score1, score2, score3, score4,
            exitButton
        )
    }

    /**
     * creates a label for displaying a player's name at a given position
     *
     * @param y the y-coordinate of a label
     * @return A configured [Label] instance
     */
    private fun createPlayerLabel(y: Int) = Label(
        posX = 600,
        posY = y,
        width = 600,
        height = 60,
        text = "",
        font = Font(45, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER_LEFT,
        visual = ColorVisual(Color(0x000000)).apply {
            transparency = 0.5
            style.borderRadius = BorderRadius.MEDIUM
        }
    )

    /**
     * creates a label for displaying a player's score at a given position
     *
     * @param y the y-coordinate of a label
     * @return A configured [Label] instance
     */
    private fun createScoreLabel(y: Int) = Label(
        posX = 1250,
        posY = y,
        width = 200,
        height = 60,
        text = "",
        font = Font(40, Color.WHITE, "Font2"),
        alignment = Alignment.CENTER
    )

    /**
     * return the background image path corresponding to a player color
     *
     * @param color The player's [ColorType].
     * @return The image path as a string.
     */
    private fun getImageForColor(color: ColorType): String {
        return when (color) {
            ColorType.YELLOW -> "yellowPlayers.jpg"
            ColorType.BLUE -> "bluePlayers.jpg"
            ColorType.RED -> "redPlayers.jpg"
            ColorType.GREEN -> "greenPlayers.jpg"
            else -> "yellowPlayers.jpg"
        }
    }

    /**
     * displays the ranking of a players in the UI
     *
     * updates player names, score, colors, and highlights the winner
     *
     * @param ranking A list of pairs containing player names and scores
     */
    fun showRanking(ranking: List<Pair<String, Int>>) {
        val game = rootService.currentGame

        // alles verstecken
        for (i in 0 until 4) {
            playerLabels[i].isVisible = false
            scoreLabels[i].isVisible = false
        }

        var currentRank = 1

        for (i in ranking.indices) {
            val (name, score) = ranking[i]

            if (i > 0) {
                val prevScore = ranking[i - 1].second
                if (score != prevScore) {
                    currentRank = i + 1
                }
            }
            playerLabels[i].isVisible = true
            scoreLabels[i].isVisible = true

            playerLabels[i].text = " $currentRank. $name"
            scoreLabels[i].text = "$score PTS"

            val color = game.colors.find { it.players.any { p -> p.name == name } }?.colorType

            //Hintergrund bei den Spielernamen
            val imagePath = getImageForColor(color ?: ColorType.YELLOW)

            playerLabels[i].visual = ImageVisual(imagePath).apply {
                style.borderRadius = BorderRadius.MEDIUM
            }

            // Gewinner highlight
            if (i == 0) {
                playerLabels[i].font = Font(50, Color(0xFFD700), "Font2")
            } else {
                playerLabels[i].font = Font(45, Color.WHITE, "Font2")
            }
        }
    }
}