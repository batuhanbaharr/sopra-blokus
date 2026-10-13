package service.gameService
import entity.ColorType
import entity.GameMode
import entity.PlayerType
import entity.ScoringStrategy
import org.junit.jupiter.api.assertThrows
import service.RootService
import kotlin.test.*

/**
 * This class tests the function StartNewGame
 */
class StartNewGameTest {
    /**
     * The [RootService] will be initialized before each test
     */
    private lateinit var rootService: RootService


    /**
     * This function Sets up some players
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()


        val player1 = Triple("P1", PlayerType.LOCAL, ColorType.RED)
        val player2 = Triple("P2", PlayerType.LOCAL, ColorType.YELLOW)
        val player3 = Triple("P3", PlayerType.LOCAL, ColorType.GREEN)
        val player4 = Triple("P4", PlayerType.LOCAL, ColorType.BLUE)

        val players = mutableListOf(player1, player2, player3, player4)

        rootService.gameService.startNewGame(players, GameMode.FOUR_PLAYER, ScoringStrategy.BASIC, false)
    }


    /**
     * This function tests the game with 4 players
     */
    @Test
    fun testStartNewGame(){
        val game = rootService.currentGame

        assertEquals(1, game.colors[0].players.size)
        assertEquals(1, game.colors[1].players.size)
        assertEquals(1, game.colors[2].players.size)
        assertEquals(1, game.colors[3].players.size)
        assertEquals(4, game.colors.size)
        assertEquals(GameMode.FOUR_PLAYER, game.gameMode)
        assertEquals(ScoringStrategy.BASIC, game.scoringStrategy)

        val gameBoard = game.board
        checkNotNull(gameBoard)
        assertEquals(20, gameBoard.size)
        assertEquals(20, gameBoard[0].size)

        for(row in gameBoard){
            for(cell in row){
                assertEquals(ColorType.NONE, cell)
            }
        }
    }

    /**
     * This function checks if we have an error when we do not have enough players
     */
    @Test
    fun testNotEnoughPlayers(){
       val testPlayer = Triple("Leon", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayers = mutableListOf(testPlayer)
        assertThrows<IllegalArgumentException> {
            rootService.gameService.startNewGame(testPlayers, GameMode.FOUR_PLAYER,
            ScoringStrategy.BASIC, false) }
    }

    /**
     * This function checks if we can start a game with too many players
     */
    @Test
    fun testTooManyPlayers(){
        val testPlayer1 = Triple("TP1", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayer2 = Triple("TP2", PlayerType.LOCAL, ColorType.YELLOW)
        val testPlayer3 = Triple("TP3", PlayerType.LOCAL, ColorType.GREEN)
        val testPlayer4 = Triple("TP4", PlayerType.LOCAL, ColorType.RED)
        val testPlayer5 = Triple("TP5", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayers = mutableListOf(testPlayer1, testPlayer2, testPlayer3, testPlayer4, testPlayer5)
        assertThrows<IllegalArgumentException> { rootService.gameService.startNewGame(testPlayers,
            GameMode.FOUR_PLAYER,
            ScoringStrategy.BASIC,
            false) }
    }

    /**
     * This function checks if we can start a game with 4 players, at least two of which have the same color.
     */
    @Test
    fun testFourPlayersNotUniqueColors(){
        val testPlayer1 = Triple("TP1", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayer2 = Triple("TP2", PlayerType.LOCAL, ColorType.YELLOW)
        val testPlayer3 = Triple("TP3", PlayerType.LOCAL, ColorType.GREEN)
        val testPlayer4 = Triple("TP4", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayers = mutableListOf(testPlayer1, testPlayer2, testPlayer3, testPlayer4)
        assertThrows<IllegalArgumentException> { rootService.gameService.startNewGame(testPlayers,
            GameMode.FOUR_PLAYER,
            ScoringStrategy.BASIC,
            false) }
    }

    /**
     * This function checks if we can set up a game with three players
     */
    @Test
    fun testThreePlayerMode(){
        val testPlayer1 = Triple("TP1", PlayerType.LOCAL, ColorType.GREEN)
        val testPlayer2 = Triple("TP2", PlayerType.LOCAL, ColorType.RED)
        val testPlayer3 = Triple("TP3", PlayerType.LOCAL, ColorType.YELLOW)
        val testPlayers = mutableListOf(testPlayer1, testPlayer2, testPlayer3)
        rootService.gameService.startNewGame(testPlayers, GameMode.THREE_PLAYER, ScoringStrategy.BASIC, false
        )
        val game = rootService.currentGame

        assertEquals(3, game.colors[0].players.size)
        assertEquals(1, game.colors[1].players.size)
        assertEquals(1, game.colors[2].players.size)
        assertEquals(1, game.colors[3].players.size)
        assertEquals(GameMode.THREE_PLAYER, game.gameMode)
    }

    /**
     * This function checks if we can set up a game with two players on  a normal field
     */
    @Test
    fun testTwoPlayerMode(){
        val testPlayer1 = Triple("TP1", PlayerType.LOCAL, ColorType.BLUE)
        val testPlayer2 = Triple("TP2", PlayerType.LOCAL, ColorType.YELLOW)
        val testPlayers = mutableListOf(testPlayer1, testPlayer2)
        rootService.gameService.startNewGame(testPlayers, GameMode.TWO_PLAYER, ScoringStrategy.BASIC,
            false)

        val game = rootService.currentGame
        assertEquals(GameMode.TWO_PLAYER, game.gameMode)
        assertEquals(1, game.colors[0].players.size)
        assertEquals(1, game.colors[1].players.size)
        assertEquals(1, game.colors[2].players.size)
        assertEquals(1, game.colors[3].players.size)
        assertEquals(game.colors[0].players[0], game.colors[2].players[0])
        assertEquals(game.colors[1].players[0], game.colors[3].players[0])
    }

    /**
     * This function checks if we can set up a game with two players with a small field
     */
    @Test
    fun testTwoPlayersSmallField(){
        val testPlayer1 = Triple("TP1", PlayerType.LOCAL, ColorType.NONE)
        val testPlayer2 = Triple("TP2", PlayerType.LOCAL, ColorType.NONE)
        val testPlayers = mutableListOf(testPlayer1, testPlayer2)
        rootService.gameService.startNewGame(testPlayers, GameMode.TWO_PLAYER_SMALL,
            ScoringStrategy.BASIC, false)

        val game = rootService.currentGame
        assertEquals(1, game.colors[0].players.size)
        assertEquals(1, game.colors[1].players.size)
        assertNotEquals(game.colors[0].players[0],
            game.colors[1].players[0])
        val gameBoard = game.board
        checkNotNull(gameBoard)
        assertEquals(14, gameBoard.size)
        assertEquals(14, gameBoard[0].size)
    }
}