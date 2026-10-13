package service.gameService

import entity.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import service.GameService
import service.RootService
import kotlin.test.BeforeTest
import kotlin.test.Test

/*
only uncomment when tracking time for stress test
 */
//import kotlin.time.*
//import java.io.File

/**
 * Test class for [GameService.hasValidTurnLeft]
 */
class HasValidTurnLeftTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var game : Game
    private lateinit var testColor : Color

    /**
     * Set up function for tests for [GameService.hasValidTurnLeft]
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()
        val player1 = Player("P1", PlayerType.LOCAL)
        val player2 = Player("P2", PlayerType.LOCAL)
        val bot1 = Player("B1", PlayerType.BOTEASY)
        val bot2 = Player("B2", PlayerType.BOTHARD)
        val playerList = mutableListOf(bot1, player1, bot2, player2)

        //creating the game
        rootService.currentGame = createGame(playerList)
        game = rootService.currentGame
        gameService = rootService.gameService
        testColor = game.colors[0]

        //creating an empty validity and blocking board
        testColor.validityBoard = Array(20){IntArray(20){0} }
        game.board = Array(20){Array(20){ ColorType.NONE } }
        testColor.blockingBoard = Array(20){Array(20){ ColorType.NONE } }
    }

    /**
     * Test if return is false when the validity board has no entry inside
     */
    @Test
    fun testWithEmptyValidityBoard(){
        val validityBoard = testColor.validityBoard
        checkNotNull(validityBoard)
        assertTrue(validityBoard.size==20)
        assertFalse(gameService.hasValidTurnLeft(testColor))
    }

    /**
     * Tests if return is false when function is called with color that has no blocks
     */
    @Test
    fun testWithNoBlocks(){
        val color = game.colors[1]
        color.blocks.clear()
        assertTrue(color.blocks.isEmpty())
        assertFalse(gameService.hasValidTurnLeft(color))
    }

    /**
     * Tests if return is true when validity board is only active in the corners of the board.
     * This state is the state of the start of the game.
     */
    @Test
    fun testWithCornersBoard(){
        //removes the o1 block, as it always fits instantly
        val o1Block = testColor.blocks.find {it.blockName == BlockType.O1}
        testColor.blocks.remove(o1Block)
        assertTrue(testColor.blocks.size == 20)

        //Checks top left corner
        val validityBoard = testColor.validityBoard
        checkNotNull(validityBoard)
        validityBoard[0][0]=1
        testColor.validityBoard = validityBoard
        assertTrue(gameService.hasValidTurnLeft(testColor))

        //checks bottom left corner
        validityBoard[0][0]=0
        validityBoard[19][0]=1
        testColor.validityBoard = validityBoard
        assertTrue(gameService.hasValidTurnLeft(testColor))

        //checks bottom right of the board
        validityBoard[19][0]=0
        validityBoard[19][19]=1
        testColor.validityBoard = validityBoard
        assertTrue(gameService.hasValidTurnLeft(testColor))

        //checks top right of the board
        validityBoard[19][19]=0
        validityBoard[0][19]=1
        testColor.validityBoard = validityBoard
        assertTrue(gameService.hasValidTurnLeft(testColor))
    }

    /**
     * Tests if function returns false if no valid move is possible
     */
    @Test
    fun testNoValidMove(){
        //removes the o1 block, as it always fits instantly
        val o1Block = testColor.blocks.find {it.blockName == BlockType.O1}
        testColor.blocks.remove(o1Block)
        assertTrue(testColor.blocks.size == 20)

        //surrounds validity value with blocked values on blockingBoard
        val validityBoard = testColor.validityBoard
        checkNotNull(validityBoard)
        validityBoard[9][9]=1
        val blockingBoard = Array(20){Array(20){ ColorType.NONE } }
        blockingBoard[8][9] = ColorType.YELLOW
        blockingBoard[10][9] = ColorType.YELLOW
        blockingBoard[9][8] = ColorType.YELLOW
        blockingBoard[9][10] = ColorType.YELLOW

        //sets blockingBoard and gameBoard
        testColor.blockingBoard = blockingBoard
        rootService.currentGame.board = blockingBoard

        //Should fail, as only o1 block could be placed, but it was removed before
        assertFalse(gameService.hasValidTurnLeft(testColor))
    }

    /**
     * Tests a worst case scenario, that is unachievable in game.
     * validityBoard and blockingBoard are in a chess field pattern, so that only o1 block would fit.
     * o1 block gets removed, so the function fails to find a move for 200 valid spaces with 20 blocks.
     * You can remove comments to capture the time of this function.
     */
    @Test
    fun stressTest(){
        //removes o1 block, as it fits everywhere
        val o1Block = testColor.blocks.find {it.blockName == BlockType.O1}
        testColor.blocks.remove(o1Block)
        assertTrue(testColor.blocks.size == 20)

        //Sets up blockBoard and validityBoard with inverse chess field pattern
        val l1 = Array(20){if(it%2==0) ColorType.NONE else ColorType.YELLOW }
        val l2 = Array(20){if(it%2==0) ColorType.YELLOW else ColorType.NONE }
        val v1 = IntArray(20){if(it%2==0) 1 else 0}
        val v2 = IntArray(20){if(it%2==0) 0 else 1}
        val blockBoard = Array(20){if(it%2==0) l1 else l2}
        val validityBoard = Array(20){if(it%2==0) v1 else v2}

        //Sets the boards to the color and game
        testColor.validityBoard = validityBoard
        game.board = blockBoard
        testColor.blockingBoard = blockBoard

        /*
        You can uncomment the next lines to see how much time the stress test takes.
        Time is saved in TestTime.json
         */
        //val timeSource = TimeSource.Monotonic
        //val startMark = timeSource.markNow()
        assertFalse(gameService.hasValidTurnLeft(testColor))
        //val elapsed = startMark.elapsedNow()
        //File("TestTime.json").writeText("$elapsed")
    }

    /**
     * Tests if function returns false when the current color is not active
     */
    @Test
    fun testWithColorInactive(){
        testColor.isActive = false
        val validityBoard = testColor.validityBoard
        checkNotNull(validityBoard)
        testColor.validityBoard = validityBoard
        validityBoard[0][0] = 1
        assertFalse(gameService.hasValidTurnLeft(testColor))
    }

    /**
     * tests if function returns true if only a specific block fits inside
     */
    @Test
    fun testWithOnlyOnePossibleSolution(){
        //removes o1 block and i2 block, as they would fit as well
        val o1Block = testColor.blocks.find {it.blockName == BlockType.O1}
        val i2Block = testColor.blocks.find {it.blockName == BlockType.I2}
        testColor.blocks.remove(o1Block)
        testColor.blocks.remove(i2Block)
        assertTrue(testColor.blocks.size == 19)

        //Makes valid spaces for v3 block
        val validityBoard = Array(20){IntArray(20){0} }
        validityBoard[0][18] = 1
        validityBoard[1][18] = 1
        validityBoard[1][17] = 1
        //removes blocked spaces for v3 block
        val blockedBoard = Array(20){Array(20){ ColorType.YELLOW } }
        blockedBoard[0][18] = ColorType.NONE
        blockedBoard[1][18] = ColorType.NONE
        blockedBoard[1][17] = ColorType.NONE

        //sets boards in game and testColor
        game.board = blockedBoard
        testColor.validityBoard = validityBoard
        testColor.blockingBoard = blockedBoard

        //Should return true
        assertTrue(gameService.hasValidTurnLeft(testColor))
    }



    /**
     * Help function to create a [Game] object.
     *
     * @param playerList List of players for the game.
     *
     * @return [Game] that gets created with the parameters and [GameMode.FOUR_PLAYER] and [ScoringStrategy.BASIC]
     */
    private fun createGame(playerList: MutableList<Player>): Game {
        val createdGame = Game(GameMode.FOUR_PLAYER, false, ScoringStrategy.BASIC)

        createdGame.colors.clear()

        val colorTypes = listOf(
            ColorType.BLUE,
            ColorType.YELLOW,
            ColorType.RED,
            ColorType.GREEN
        )

        for ((idx, player) in playerList.withIndex()) {
            val color = Color(colorTypes[idx])
            color.players = mutableListOf(player)
            createdGame.colors.add(color)
        }

        createdGame.currentColorIndex = 0
        return createdGame
    }
}