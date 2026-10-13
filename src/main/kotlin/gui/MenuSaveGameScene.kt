package gui


import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*

/**
 * Overlay menu for saving or exiting the current game
 *
 * appears on top of the game scene
 *
 * service methods and entity layer
 */
class MenuSaveGameScene : MenuScene(1920, 1080, blurRadius = 0.0) {

    private val colorBackground = Label(
        posX = 400,
        posY = 250,
        width = 1120,
        height = 580,
        visual = ImageVisual(
            path = "breaks.png",
        )
    )

    val saveButton = Button(
        posX = 615,
        posY = 590,
        width = 330,
        height = 130,
        text = "Save Game",
        font = Font(60, Color(0x82efff), "Font2"),
        visual = ColorVisual(Color(0x0b6c8a)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    )

    val exitButton = Button(
        posX = 980,
        posY = 590,
        width = 330,
        height = 130,
        text = "Exit Game",
        font = Font(60, Color(0xffa6a1), "Font2"),
        visual = ColorVisual(Color(0x0b6c8a)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }

    )

    val returnButton = Button(
        posX = 1265,
        posY = 262,
        width = 200,
        height = 80,
        text = " return",
        font = Font(50, Color(0xc8ffb4), "Font2"),
        visual = ColorVisual(Color(0x0b6c8a)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    )

    init {
        addComponents(
            colorBackground,
            saveButton,
            exitButton,
            returnButton,
        )
    }

    /**
     * Schaltet den Save-Button aus, wenn ein Online-Spiel läuft.
     */
    fun setOnlineMode(isOnline: Boolean) {
        saveButton.isVisible = !isOnline
        saveButton.isDisabled = isOnline
    }
}