package service.gameService

import entity.*
import entity.Game
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import service.RootService
import service.TestRefreshable
import kotlin.test.*

/**
 * Class to test [GameService.endGame]
 * */
class EndGameTest {
    lateinit var game : Game
    lateinit var rootService : RootService
    lateinit var testRefreshable: TestRefreshable

    /**
     * Set up function to test if [service.GameService.endGame] works correctly.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        testRefreshable = TestRefreshable()
        rootService.addRefreshables(testRefreshable)

        val player1 = mutableListOf(Player("John", PlayerType.LOCAL))
        val player2 = mutableListOf(Player("Bohn", PlayerType.LOCAL))

        game = Game(GameMode.TWO_PLAYER, false, ScoringStrategy.BASIC)
        game.colors[0].players = player1
        game.colors[1].players = player2
        game.colors[2].players = player1
        game.colors[3].players = player2
        rootService.currentGame = game
    }


    /**
     * Tests if advanced mode calculates the right score
     */
    @Test
    fun advancedModeTest(){
        val player1 = mutableListOf(Player("John", PlayerType.LOCAL))
        val player2 = mutableListOf(Player("Bohn", PlayerType.LOCAL))

        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.ADVANCED)
        game.colors[0].players = player1
        game.colors[1].players = player2
        rootService.currentGame = game

        val winner = Pair("John", 15)
        testRefreshable.rankingList.add(winner)

        val looser = Pair("Bohn", 5)
        testRefreshable.rankingList.add(looser)

        assertDoesNotThrow { rootService.gameService.endGame() }
        assertEquals(winner.first, testRefreshable.rankingList[0].first)
        assertEquals(looser.first, testRefreshable.rankingList[1].first)

    }

    /**
     * Tests if nomal mode calculates the right scores
     * */
    @Test
    fun normalModeTest(){
        game = rootService.currentGame

        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.BASIC)

        val winner = Pair("John", 5)
        testRefreshable.rankingList.add(winner)

        val looser = Pair("Bohn", 15)
        testRefreshable.rankingList.add(looser)

        assertDoesNotThrow { rootService.gameService.endGame() }
        assertEquals(winner.first, testRefreshable.rankingList[0].first)
        assertEquals(looser.first, testRefreshable.rankingList[1].first)

    }

    /**
     * Tests if game gets set to null after endGame
     */
    @Test
    fun isGameActiveTest(){
        assertDoesNotThrow{rootService.gameService.endGame()}
    }

    /**
     * Tests if refresh gets called correctly and the ranking size is correct
     */
    @Test
    fun refreshableCalledTest(){
        assertFalse(testRefreshable.refreshAfterGameEndCalled)
        assertDoesNotThrow{rootService.gameService.endGame()}
        assertTrue(testRefreshable.refreshAfterGameEndCalled)
        assertTrue(testRefreshable.rankingList.size==2)
    }
}