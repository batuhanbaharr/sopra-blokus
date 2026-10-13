package service.playerActionService

import service.RootService
import entity.Game
import entity.GameMode
import entity.PlayerType
import entity.ScoringStrategy
import service.TestRefreshable
import kotlin.test.*

/**
 * Class to test the functions undo and redo
 */
class UndoRedoTest {
    lateinit var rootService : RootService
    lateinit var testRefreshable : TestRefreshable
    lateinit var firstTurn : Game
    lateinit var secondTurn: Game

    /**
     * Sets up two [Game] instances to test redo and undo
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()

        val playersNames = mutableListOf("Leonardo", "Nickolas", "Harry", "Robot")

        val colors = listOf(
            entity.ColorType.BLUE,
            entity.ColorType.YELLOW,
            entity.ColorType.RED,
            entity.ColorType.GREEN
        )

        val playersList : MutableList<Triple<String, PlayerType, entity.ColorType>> = mutableListOf()

        playersNames.forEachIndexed { index, name ->
            playersList.add(Triple(name, PlayerType.LOCAL, colors[index]))
        }

        rootService.gameService.startNewGame(
            playersList.slice(0..1).toMutableList(),
            GameMode.TWO_PLAYER,
            ScoringStrategy.BASIC,
            false)

        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        firstTurn = rootService.currentGame
        secondTurn = Game(GameMode.TWO_PLAYER, false, ScoringStrategy.BASIC)

        secondTurn.previousGame = firstTurn
        firstTurn.nextGame = secondTurn
    }

    /**
     * Tests the undo function for a previous game
     */
    @Test
    fun testWorkingCaseUndo(){
        rootService.playerActionService.undo()
        assertEquals(firstTurn, rootService.currentGame)
        assertTrue(testRefreshable.refreshAfterGameLoadedCalled)
        testRefreshable.reset()
    }

    /**
     * Test if redo works correct if we already used undo
     */
    @Test
    fun testWorkingCaseRedo(){
        rootService.playerActionService.undo()
        assertTrue(testRefreshable.refreshAfterGameLoadedCalled)
        testRefreshable.reset()
        rootService.playerActionService.redo()
        assertEquals(secondTurn, rootService.currentGame)
        assertTrue(testRefreshable.refreshAfterGameLoadedCalled)
        testRefreshable.reset()
    }
}