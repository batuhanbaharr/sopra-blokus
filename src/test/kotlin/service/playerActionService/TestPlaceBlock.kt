package service.playerActionService

import entity.*
import service.RootService
import service.TestRefreshable
import kotlin.test.*

/**
 * Class to test the function [service.PlayerActionService.placeBlock]
 */
class TestPlaceBlock {
    lateinit var game: Game
    lateinit var rootService: RootService
    lateinit var testRefreshable: TestRefreshable

    /**
     * Sets up a [Game] and [RootService] to test the operations on.
     */
    @BeforeTest
    fun setUp() {

        rootService = RootService()

        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        val emptyBoard = Array(14) { Array(14) { ColorType.NONE } }

        //setting up the game and saving it in RootService
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.BASIC)
        rootService.currentGame = game


        game.board = emptyBoard
    }

    /**
     * Check if we can place the block [BlockType.O1]
     */
    @Test
    fun testSuccessfulPlacementOneBlock() {
        val currentColor = rootService.gameService.getCurrentColor()
        currentColor.blockingBoard = Array(14) { Array(14) { ColorType.NONE } }
        currentColor.validityBoard = Array(14) { IntArray(14) { 0 } }


        currentColor.players.add(Player("Alice", PlayerType.LOCAL))
        currentColor.validityBoard!![5][5] = 1

        val block = currentColor.blocks.find { it.blockName == BlockType.O1 }
        val coordinates = Pair(5, 5)

        assertNotNull(block){"Block should not be null"}

        rootService.playerActionService.placeBlock(block, coordinates)

        val gameBoard = game.board
        checkNotNull(gameBoard)

        assertEquals(currentColor.colorType, gameBoard[5][5])
        assertEquals(ColorType.NONE, gameBoard[5][6])
        assertTrue(testRefreshable.refreshAfterBlockPlacedCalled)
        testRefreshable.reset()
    }

    /**
     * Check if we can place the block [BlockType.O4]
     */
    @Test
    fun testSuccessfulPlacementSquareBlock() {
        rootService.gameService.getCurrentColor().blockingBoard = Array(14) {Array(14) { ColorType.NONE }}
        rootService.gameService.getCurrentColor().validityBoard = Array(14) { IntArray(14) { 1 } }


        val currentColor = rootService.gameService.getCurrentColor()
        currentColor.players.add(Player("Alice", PlayerType.LOCAL))



        val block = currentColor.blocks.find { it.blockName == BlockType.O4 }
        val coordinates = Pair(2, 2)



        assertNotNull(block){"Block should not be null"}

        rootService.playerActionService.placeBlock(block, coordinates)



        val gameBoard = game.board
        checkNotNull(gameBoard)

        assertEquals(currentColor.colorType, gameBoard[2][2])
        assertEquals(currentColor.colorType, gameBoard[2][3])
        assertEquals(currentColor.colorType, gameBoard[3][2])
        assertEquals(currentColor.colorType, gameBoard[3][3])


        assertEquals(ColorType.NONE, gameBoard[4][4])
        assertTrue(testRefreshable.refreshAfterBlockPlacedCalled)
        testRefreshable.reset()
    }

    /**
     * Check if we can place a block if it is not allowed
     */
    @Test
    fun testFailurePlacementThrowsException() {
        val currentColor = rootService.gameService.getCurrentColor()
        currentColor.blockingBoard = Array(14) { Array(14) { ColorType.NONE }}
        currentColor.validityBoard = Array(14) { IntArray(14) { 0 } }

        currentColor.players.add(Player("Alice", PlayerType.LOCAL))
        val block = currentColor.blocks.find { it.blockName == BlockType.O1 }
        val coordinates = Pair(0, 0)

        assertNotNull(block){"Block should not be null"}
        rootService.playerActionService.placeBlock(block, coordinates)

        val gameBoard = game.board
        checkNotNull(gameBoard)
        assertEquals(ColorType.NONE, gameBoard[0][0])
        assertFalse(testRefreshable.refreshAfterBlockPlacedCalled)
    }
}