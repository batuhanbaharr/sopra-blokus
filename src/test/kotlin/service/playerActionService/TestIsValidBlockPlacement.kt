package service.playerActionService

import entity.*
import service.RootService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class to test the function [service.GameService.isValidPosition]
 */
class TestIsValidBlockPlacement {
    lateinit var rootService : RootService

    /**
     * Sets up a [Game] and [RootService] to test the operations on.
     */
    @BeforeTest
    fun setUp(){
        //Creating RootService
        rootService = RootService()

        //setting up the game and saving it in RootService
        val game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.BASIC)
        rootService.currentGame = game
    }

    /**
     * Check if we can place a block even though the board is full
     */
    @Test
    fun testFailureCaseFullBoard(){
        // we set everything to 1 in the block board
        rootService.gameService.getCurrentColor().blockingBoard = Array(14) { Array(14) { ColorType.BLUE } }
        rootService.gameService.getCurrentColor().validityBoard = Array(14) { IntArray(14) { 1 } }

        assertFalse { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(0,0)) }
        assertFalse { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(13,13)) }

        assertFalse { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(0,13)) }
        assertFalse { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(13,0)) }
    }

    /**
     * check whether we can place a block if the corners are free
     */
    @Test
    fun testWorkingCaseOneBlockCorners(){
        // we set everything to 1 in the validity board
        rootService.gameService.getCurrentColor().blockingBoard = Array(14) { Array(14) { ColorType.NONE }}
        rootService.gameService.getCurrentColor().validityBoard = Array(14) { IntArray(14) { 0 } }

        rootService.gameService.getCurrentColor().validityBoard!![0][0] = 1
        rootService.gameService.getCurrentColor().validityBoard!![13][13] = 1
        rootService.gameService.getCurrentColor().validityBoard!![0][13] = 1
        rootService.gameService.getCurrentColor().validityBoard!![13][0] = 1

        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(0,0)) }
        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(13,13)) }
        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(0,13)) }
        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.O1),
            Pair(13,0)) }
    }

    /**
     *  Check if [BlockType.I2] could be placed in the corners
     */
    @Test
    fun testWorkingCaseTwoBlockCorners(){
        // we set everything to 1 in the validity board
        rootService.gameService.getCurrentColor().blockingBoard = Array(14) { Array(14) { ColorType.NONE }}
        rootService.gameService.getCurrentColor().validityBoard = Array(14) { IntArray(14) { 0 } }

        rootService.gameService.getCurrentColor().validityBoard!![0][0] = 1
        rootService.gameService.getCurrentColor().validityBoard!![13][13] = 1
        rootService.gameService.getCurrentColor().validityBoard!![0][13] = 1
        rootService.gameService.getCurrentColor().validityBoard!![13][0] = 1

        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.I2),
            Pair(0,0)) }
        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.I2),
            Pair(12,0))}

        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.I2),
            Pair(12,13)) }
        assertTrue { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.I2),
            Pair(0,13))}
    }

    /**
     * Check whether it is valid for a block to go outside the board if it is placed inside it
     */
    @Test
    fun testFailureCaseOutOfRange(){
        // we set everything to 1 in the validity board
        rootService.gameService.getCurrentColor().blockingBoard = Array(14) { Array(14) { ColorType.NONE}}
        rootService.gameService.getCurrentColor().validityBoard = Array(14) { IntArray(14) { 1 } }

        assertFalse { rootService.playerActionService.isValidBlockPlacement(Block(BlockType.I2),
            Pair(13,13)) }

    }
}