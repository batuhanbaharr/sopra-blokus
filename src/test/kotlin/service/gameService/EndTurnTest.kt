package service.gameService

import entity.*
import service.RootService
import service.TestRefreshable
import service.GameService
import kotlin.test.*

/**
 * Class to test [GameService].endTurn
 * */
class EndTurnTest {
    lateinit var rootServiceTwo : RootService
    lateinit var rootServiceTwoSmall : RootService
    lateinit var rootServiceThree : RootService
    lateinit var rootServiceFour : RootService

    /**
     * Sets up different variants of the game
     */
    @BeforeTest
    fun setUp() {
        val playersNames = mutableListOf(
            Pair("Leonardo", ColorType.GREEN),
            Pair("Nickolas", ColorType.BLUE),
            Pair("Harry", ColorType.YELLOW),
            Pair("Robot", ColorType.RED)
        )
        val playersList : MutableList<Triple<String, PlayerType, ColorType>> = mutableListOf()

        playersNames.forEach { playersList.add(Triple(it.first, PlayerType.LOCAL, it.second)) }

        rootServiceTwo = RootService()
        rootServiceTwo.gameService.startNewGame(
            playersList.slice(0..1).toMutableList(),
            GameMode.TWO_PLAYER,
            ScoringStrategy.BASIC,
            false)

        rootServiceTwoSmall = RootService()
        rootServiceTwoSmall.gameService.startNewGame(
            playersList.slice(0..1).toMutableList(),
            GameMode.TWO_PLAYER_SMALL,
            ScoringStrategy.BASIC,
            false)

        // make yellow the shared color
        val playersListThree = playersList.slice(0..1).toMutableList()
        playersListThree.add(playersList[3])
        rootServiceThree = RootService()
        rootServiceThree.gameService.startNewGame(
            playersListThree,
            GameMode.THREE_PLAYER,
            ScoringStrategy.BASIC,
            false)

        rootServiceFour = RootService()
        rootServiceFour.gameService.startNewGame(
            playersList.toMutableList(),
            GameMode.FOUR_PLAYER,
            ScoringStrategy.BASIC,
            false)
    }

    /**
     * Test if [GameService.endTurn] works for two players on normal field
     */
    @Test
    fun endTurnForTwoPlayers(){
        val game = rootServiceTwo.currentGame
        val testRefreshable = TestRefreshable()
        rootServiceTwo.addRefreshable(testRefreshable)

        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)

        rootServiceTwo.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceTwo.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[1].sharedPlayerIndex)
        assertEquals(2, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceTwo.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[2].sharedPlayerIndex)
        assertEquals(3, game.currentColorIndex)
        assertEquals(ColorType.GREEN, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceTwo.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[3].sharedPlayerIndex)
        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[1].isActive = false
        game.colors[2].isActive = false
        rootServiceTwo.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[3].sharedPlayerIndex)
        assertEquals(3, game.currentColorIndex)
        assertEquals(ColorType.GREEN, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[0].isActive = false
        game.colors[3].isActive = false
        rootServiceTwo.gameService.endTurn()
        assertFalse(testRefreshable.refreshAfterTurnEndCalled)
        assertTrue(testRefreshable.refreshAfterGameEndCalled)
        testRefreshable.reset()
    }

    /**
     * Tests if [GameService.endTurn] works for two players on a small field
     */
    @Test
    fun endTurnForTwoPlayersSmall(){
        val game = rootServiceTwoSmall.currentGame
        val testRefreshable = TestRefreshable()
        rootServiceTwoSmall.addRefreshable(testRefreshable)

        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)

        rootServiceTwoSmall.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceTwoSmall.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[1].sharedPlayerIndex)
        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceTwoSmall.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[0].isActive = false
        rootServiceTwoSmall.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[1].isActive = false
        rootServiceTwoSmall.gameService.endTurn()
        assertFalse(testRefreshable.refreshAfterTurnEndCalled)
        assertTrue(testRefreshable.refreshAfterGameEndCalled)
        testRefreshable.reset()
    }

    /**
     * Tests if [GameService.endTurn] works for three players
     */
    @Test
    fun endTurnForThreePlayers(){
        val game = rootServiceThree.currentGame
        val testRefreshable = TestRefreshable()
        rootServiceThree.addRefreshable(testRefreshable)

        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(1, game.colors[1].sharedPlayerIndex)
        assertEquals(2, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[2].sharedPlayerIndex)
        assertEquals(3, game.currentColorIndex)
        assertEquals(ColorType.GREEN, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[3].sharedPlayerIndex)
        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(1, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(2, game.colors[1].sharedPlayerIndex)
        assertEquals(2, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.currentColorIndex = 1
        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[1].sharedPlayerIndex)
        assertEquals(2, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[3].isActive = false
        game.colors[0].isActive = false
        rootServiceThree.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[2].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[1].isActive = false
        game.colors[2].isActive = false
        rootServiceThree.gameService.endTurn()
        assertFalse(testRefreshable.refreshAfterTurnEndCalled)
        assertTrue(testRefreshable.refreshAfterGameEndCalled)
        testRefreshable.reset()
    }

    /**
     * Tests if [GameService.endTurn] works for four players
     */
    @Test
    fun endTurnForFourPlayers(){
        val game = rootServiceFour.currentGame
        val testRefreshable = TestRefreshable()
        rootServiceFour.addRefreshable(testRefreshable)

        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.GREEN, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)

        rootServiceFour.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(1, game.currentColorIndex)
        assertEquals(ColorType.BLUE, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceFour.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[1].sharedPlayerIndex)
        assertEquals(2, game.currentColorIndex)
        assertEquals(ColorType.YELLOW, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceFour.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[2].sharedPlayerIndex)
        assertEquals(3, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        rootServiceFour.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[3].sharedPlayerIndex)
        assertEquals(0, game.currentColorIndex)
        assertEquals(ColorType.GREEN, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[1].isActive = false
        game.colors[2].isActive = false
        rootServiceFour.gameService.endTurn()
        assertTrue(testRefreshable.refreshAfterTurnEndCalled)
        assertEquals(0, game.colors[0].sharedPlayerIndex)
        assertEquals(3, game.currentColorIndex)
        assertEquals(ColorType.RED, game.colors[game.currentColorIndex].colorType)
        assertEquals(0, game.colors[game.currentColorIndex].sharedPlayerIndex)
        testRefreshable.reset()

        game.colors[0].isActive = false
        game.colors[3].isActive = false
        rootServiceFour.gameService.endTurn()
        assertFalse(testRefreshable.refreshAfterTurnEndCalled)
        assertTrue(testRefreshable.refreshAfterGameEndCalled)
        testRefreshable.reset()
    }
}