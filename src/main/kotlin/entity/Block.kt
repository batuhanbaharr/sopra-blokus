package entity

import kotlinx.serialization.Serializable

/**
 * Represent a block (game piece) in the Blokus game.
 *
 * Each block has a blockName that defines its shape. The shape is stored
 * in blueprint as a list of relative coordinates, where each pair
 * represents one square of the block.
 * It is also tracked if the block has been mirrored/rotated.
 *
 * @param blockName The block type that define the shape of this block.
 */
@Serializable
class Block(val blockName: BlockType) {
    /**
     * The shape of this block as a list of relative coordinates.
     * Each Pair is one square of the block relative to the origin point.
     * The list size equals the number of squares in the block.
     * Set based on blockName.
     */
    var blueprint :Array<IntArray> = when (blockName) {
        BlockType.I5 -> arrayOf(
            intArrayOf(1, 1, 1, 1, 1)
        )
        BlockType.L5 -> arrayOf(
            intArrayOf(1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0)
        )
        BlockType.U5 -> arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 1)
        )
        BlockType.Z5 -> arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1)
        )
        BlockType.T5 -> arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1)
        )
        BlockType.X5 -> arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        )
        BlockType.W5 -> arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1)
        )
        BlockType.V5 -> arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1)
        )
        BlockType.F5 -> arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0)
        )
        BlockType.P5 -> arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        )
        BlockType.Y5 -> arrayOf(
            intArrayOf(1, 1, 1, 1),
            intArrayOf(0, 1, 0, 0)
        )
        BlockType.N5 -> arrayOf(
            intArrayOf(0, 1, 1, 1),
            intArrayOf(1, 1, 0, 0)
        )
        BlockType.O1 -> arrayOf(
            intArrayOf(1)
        )
        BlockType.I2 -> arrayOf(
            intArrayOf(1, 1)
        )
        BlockType.I3 -> arrayOf(
            intArrayOf(1, 1, 1)
        )
        BlockType.I4 -> arrayOf(
            intArrayOf(1, 1, 1, 1)
        )
        BlockType.T4 -> arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1)
        )
        BlockType.L4 -> arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1)
        )
        BlockType.Z4 -> arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        )
        BlockType.O4 -> arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        )
        BlockType.V3 -> arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        )
    }

    /**
     * Signals if the block is in its "mirrored" state.
     */
    var isMirrored : Boolean = false

    /**
     * Shows if and how the block has been rotated.
     */
    var rotation : Rotation = Rotation.NONE

    /**
     * creates a copy of the [Block].
     *
     * @return copiedBlock
     * */
    fun copy():Block{
        val copiedBlock = Block(this.blockName)
        copiedBlock.blueprint = this.blueprint.map { it.copyOf() }.toTypedArray()
        copiedBlock.isMirrored = this.isMirrored
        copiedBlock.rotation = this.rotation

        return copiedBlock
    }
}