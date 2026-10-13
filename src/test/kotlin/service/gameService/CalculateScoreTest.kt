package service.gameService

import entity.*
import service.RootService
import kotlin.test.*

/**
 * test class for [service.GameService.calculateScore]
 */
class CalculateScoreTest {
    private lateinit var rootService: RootService
    private lateinit var game: Game

    /**
     * set up a game with two players on a small board for testing
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.BASIC)
        val player1 = Player("P1", PlayerType.LOCAL)
        val player2 = Player("P2", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player1)
        game.colors[1].players = mutableListOf(player2)

        rootService.currentGame = game
    }
    /**
     * test basic scoring when no blocks have been placed
     * all 21 blocks are still in the list, so score should be 89 (total squares)
     */
    @Test
    fun testBasicScoringNoBlocksPlaced() {
        val player = game.colors[0].players[0]
        rootService.gameService.calculateScore(player)
        // 21 blocks = 89 squares total
        assertEquals(89, player.score)
    }
    /**
     * test basic scoring when some blocks have been placed
     * we remove a block from the list to simulate placing it
     */
    @Test
    fun testBasicScoringSomeBlocksPlaced() {
        val player = game.colors[0].players[0]
        val color = game.colors[0]
        // remove the O1 block (1 square) to simulate placing it
        val o1Block = color.blocks.find { it.blockName == BlockType.O1 }
        checkNotNull(o1Block)
        color.blocks.remove(o1Block)
        color.discardedBlocks.add(o1Block)
        rootService.gameService.calculateScore(player)

        // 89 - 1 = 88 remaining squares
        assertEquals(88, player.score)
    }

    /**
     * test basic scoring when all blocks have been placed
     */
    @Test
    fun testBasicScoringAllBlocksPlaced() {
        val player = game.colors[0].players[0]
        val color = game.colors[0]
        // move all blocks to discarded
        color.discardedBlocks.addAll(color.blocks)
        color.blocks.clear()
        rootService.gameService.calculateScore(player)

        assertEquals(0, player.score)
    }
    /**
     * test advanced scoring when no blocks have been placed
     * score should be -89
     */
    @Test
    fun testAdvancedScoringNoBlocksPlaced() {
        // switch to advanced scoring
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.ADVANCED)
        val player = Player("P1", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player)
        rootService.currentGame = game
        rootService.gameService.calculateScore(player)

        assertEquals(-89, player.score)
    }
    /**
     * test advanced scoring when all blocks have been placed
     * should get +15 bonus
     */
    @Test
    fun testAdvancedScoringAllBlocksPlaced() {
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.ADVANCED)
        val player = Player("P1", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player)
        rootService.currentGame = game
        val color = game.colors[0]
        // move all blocks to discarded
        color.discardedBlocks.addAll(color.blocks)
        color.blocks.clear()

        rootService.gameService.calculateScore(player)

        // 0 remaining + 15 bonus = 15
        assertEquals(15, player.score)
    }
    /**
     * test advanced scoring with onesquare bonus
     * if all blocks placed and last one was O1, should get +15 and +5
     */
    @Test
    fun testAdvancedScoringOneSquareBonus() {
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.ADVANCED)
        val player = Player("P1", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player)
        rootService.currentGame = game

        val color = game.colors[0]

        // move all blocks to discarded, put O1 last
        val o1Block = color.blocks.find { it.blockName == BlockType.O1 }
        checkNotNull(o1Block)
        color.blocks.remove(o1Block)
        color.discardedBlocks.addAll(color.blocks)
        color.blocks.clear()
        // add O1 as the last discarded block
        color.discardedBlocks.add(o1Block)
        rootService.gameService.calculateScore(player)

        // 0 remaining + 15 bonus + 5 one square bonus = 20
        assertEquals(20, player.score)
    }
    /**
     * test advanced scoring when all blocks placed but last was not O1
     * should get +15 but not +5
     */
    @Test
    fun testAdvancedScoringNoOneSquareBonus() {
        game = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.ADVANCED)
        val player = Player("P1", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player)
        rootService.currentGame = game
        val color = game.colors[0]

        // move all blocks to discarded und put I2 last
        val i2Block = color.blocks.find { it.blockName == BlockType.I2 }
        checkNotNull(i2Block)
        color.blocks.remove(i2Block)
        color.discardedBlocks.addAll(color.blocks)
        color.blocks.clear()
        color.discardedBlocks.add(i2Block)
        rootService.gameService.calculateScore(player)

        // 0 remaining + 15 bonus = 15 (no one square bonus)
        assertEquals(15, player.score)
    }
    /**
     * test scoring for two player mode where one player controls two colors
     * scores from both colors should be added together
     */
    @Test
    fun testTwoPlayerModeTwoColors() {
        game = Game(GameMode.TWO_PLAYER, false, ScoringStrategy.BASIC)
        val player = Player("P1", PlayerType.LOCAL)
        game.colors[0].players = mutableListOf(player)
        game.colors[2].players = mutableListOf(player)
        rootService.currentGame = game
        // remove O1 from first color (1 square)
        val color1 = game.colors[0]
        val o1Block = color1.blocks.find { it.blockName == BlockType.O1 }
        checkNotNull(o1Block)
        color1.blocks.remove(o1Block)
        color1.discardedBlocks.add(o1Block)

        rootService.gameService.calculateScore(player)

        // color 0: 89-1 = 88, color 2: 89, total = 177
        assertEquals(177, player.score)
    }
}