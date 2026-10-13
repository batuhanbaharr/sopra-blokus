package service.playerActionService

import entity.*
import org.junit.jupiter.api.Test
import service.RootService
import service.TestRefreshable
import kotlin.test.*

/**
 * Class to test the function [service.PlayerActionService.rotateBlock]
 */
class RotateBlockTest {
    lateinit var rootService : RootService
    lateinit var testRefreshable : TestRefreshable

    /**
     * Sets up a [Game] and [RootService] to test the operations on.
     */
    @BeforeTest
    fun setUp(){
        //Creating RootService
        rootService = RootService()
        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
    }

    /**
     * We test the trivial case where we rotate the [BlockType.O1]
     */
    @Test
    fun rotateOneBlockTest() {
        val block = Block(BlockType.O1)
        val expected = arrayOf(intArrayOf(1))

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }


    /**
     * We test the case where we rotate the [BlockType.I2]
     */
    @Test
    fun rotateTwoBlockTest(){
        val i2Block = Block(BlockType.I2)

        rootService.playerActionService.rotateBlock(i2Block, false)
        val twoBlock90Degrees = arrayOf(
            intArrayOf(1),
            intArrayOf(1)
        )
        assertTrue(twoBlock90Degrees.contentDeepEquals(i2Block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(i2Block, true)
        val twoBlock180Degrees = arrayOf(
            intArrayOf(1,1)
        )
        assertTrue(twoBlock180Degrees.contentDeepEquals(i2Block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(i2Block, false)
        val twoBlock270Degrees = arrayOf(
            intArrayOf(1),
            intArrayOf(1)
        )
        assertTrue(twoBlock270Degrees.contentDeepEquals(i2Block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.I3]
     */
    @Test
    fun rotateThreeBlockTest() {
        val block = Block(BlockType.I3)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1))
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(intArrayOf(1, 1, 1))
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.I4]
     */
    @Test
    fun rotateShortIBlockTest() {
        val block = Block(BlockType.I4)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1))
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(intArrayOf(1, 1, 1, 1))
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }


    /**
     * We test the case where we rotate [BlockType.T4]
     */
    @Test
    fun rotateShortTBlockTest() {
        val block = Block(BlockType.T4)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.Z4]
     */
    @Test
    fun rotateShortZBlockTest() {
        val block = Block(BlockType.Z4)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.V3]
     */
    @Test
    fun rotateCrookedThreeBlockTest() {
        val block = Block(BlockType.V3)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.I5]
     */
    @Test
    fun rotateIBlockTest() {
        val block = Block(BlockType.I5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1),
                              intArrayOf(1))
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(intArrayOf(1, 1, 1, 1, 1))
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.L5]
     */
    @Test
    fun rotateLBlockTest() {
        val block = Block(BlockType.L5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(0, 1),
            intArrayOf(0, 1),
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 1),
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.U5]
     */
    @Test
    fun rotateUBlockTest() {
        val block = Block(BlockType.U5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 0, 1),
            intArrayOf(1, 1, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.Z5]
     */
    @Test
    fun rotateZBlockTest() {
        val block = Block(BlockType.Z5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.X5]
     */
    @Test
    fun rotateXBlockTest() {
        val block = Block(BlockType.X5)
        val expected = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        )

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        assertTrue(expected.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.W5]
     */
    @Test
    fun rotateWBlockTest() {
        val block = Block(BlockType.W5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0),
            intArrayOf(1, 0, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1),
            intArrayOf(0, 0, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.V5]
     */
    @Test
    fun rotateVBlockTest() {
        val block = Block(BlockType.V5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1),
            intArrayOf(0, 0, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.F5]
     */
    @Test
    fun rotateFBlockTest() {
        val block = Block(BlockType.F5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1),
            intArrayOf(0, 1, 0)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.P5]
     */
    @Test
    fun rotatePBlockTest() {
        val block = Block(BlockType.P5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 1)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(1, 1, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.Y5]
     */
    @Test
    fun rotateYBlockTest() {
        val block = Block(BlockType.Y5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(0, 1)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(0, 0, 1, 0),
            intArrayOf(1, 1, 1, 1)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }

    /**
     * We test the case where we rotate [BlockType.N5]
     */
    @Test
    fun rotateNBlockTest() {
        val block = Block(BlockType.N5)

        rootService.playerActionService.rotateBlock(block, false)
        val block90 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(0, 1)
        )
        assertTrue(block90.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)

        rootService.playerActionService.rotateBlock(block, true)
        val block180 = arrayOf(
            intArrayOf(0, 0, 1, 1),
            intArrayOf(1, 1, 1, 0)
        )
        assertTrue(block180.contentDeepEquals(block.blueprint))
        assertTrue(testRefreshable.refreshSingleBlockCalled)
        testRefreshable.reset()

        rootService.playerActionService.rotateBlock(block, false)
        val block270 = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        )
        assertTrue(block270.contentDeepEquals(block.blueprint))
        assertFalse(testRefreshable.refreshSingleBlockCalled)
    }
}