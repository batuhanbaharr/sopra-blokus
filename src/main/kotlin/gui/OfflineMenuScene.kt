package gui

import tools.aqua.bgw.components.uicomponents.*

import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*


/**
 * Shows 2 possible Game Options, Load a previous Game or create a new Game
 * Or you can go back to the previous scene --> GameMenuScene
 */
class OfflineMenuScene :
    BoardGameScene(1920, 1080,
        background = ImageVisual("MainMenuOfflineScene.png")){

    val createButton = Button(
        posX = 605,
        posY = 430,
        width = 700,
        height = 105,
        text = "Create Game",
        font = Font(70, Color(0x0cfe00), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x85ffcc)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    )

    val loadButton = Button(
        posX = 605,
        posY = 570,
        width = 700,
        height = 105,
        text = "Load Game",
        font = Font(70, Color(0x00f7fe), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x61daff)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.0
        }
    )

    val backButton = Button(
        posX = 675,
        posY = 905,
        width = 550,
        height = 100,
        text = "back",
        font = Font(60, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x0025fe)).apply {
            style.borderRadius = BorderRadius.MEDIUM
            transparency = 0.3
        }
    )

    init {
        addComponents(
            loadButton,
            createButton,
            backButton
        )
    }
}
