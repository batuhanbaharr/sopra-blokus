package gui

import tools.aqua.bgw.components.uicomponents.*

import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font

import tools.aqua.bgw.visual.*


/**
 * Shows two possible Game States --> Online or Offline Game
 * The player can choose between those two or can quit the Game
 */
class GameMenuScene : BoardGameScene(
    1920, 1080,
    background = ImageVisual("MainMenuBackground.png"))
{

    val onlineButton = Button(
        posX = 760,
        posY = 500,
        width = 400,
        height = 90,
        text = "Online",
        font = Font(42, Color(0x055c37), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x85ffcc)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    )

    val offlineButton = Button(
        posX = 760,
        posY = 610,
        width = 400,
        height = 90,
        text = "Offline",
        font = Font(42, Color(0x0b6c8a), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0x61daff)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    )

    val quitButton = Button(
        posX = 880,
        posY = 978,
        width = 200,
        height = 50,
        text = "Quit",
        font = Font(24, Color(0xFFFFFFF), "Font2"),
        alignment = Alignment.CENTER,
        isWrapText = false,
        visual = ColorVisual(Color(0xFF6459)).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    )

    init {
        addComponents(
            onlineButton,
            offlineButton,
            quitButton
        )
    }
}
