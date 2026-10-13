package service

import entity.Block
import entity.ColorType
import entity.Game
import entity.PlayerType
import entity.Rotation

/**
 * Service to help manage player actions.
 *
 * @param rootService [RootService] for access to entity layer and other services.
 *
 * @property rootService [RootService] for access to entity layer and other services.
 */
class PlayerActionService(private val rootService: RootService): AbstractRefreshingService() {

    /**
     * Uses [SaveService] to set the game state to the last state before the current state,
     * where a player is active.
     *
     * Preconditions:
     * - [Game.previousGame] muss not be null
     *
     * Post-conditions:
     * - [rootService.currentGame] should be set to [Game.previousGame]
     */
    fun undo(){
        // we get the previous game
        val previousGame = rootService.saveService.findPreviousPlayerTurn()

        // we set the game to the previous game
        rootService.currentGame = previousGame
        rootService.gameService.gameBeforeTurn = rootService.currentGame.copy()

        onAllRefreshables { refreshAfterGameLoaded() }
    }

    /**
     * Uses [SaveService] to get back to the most recently available turn.
     *
     * Preconditions:
     * - [Game.nextGame] muss not be null
     *
     * Post-conditions:
     * - [rootService.currentGame] should be set to [Game.nextGame]
     */
    fun redo(){
        // we get the most recent game with player
        val recentGame = rootService.saveService.findMostRecentPlayerTurn()

        // we set the game to the previous game
        rootService.currentGame = recentGame
        rootService.gameService.gameBeforeTurn = rootService.currentGame.copy()

        onAllRefreshables { refreshAfterGameLoaded() }
    }

    /**
     * Places a [Block] at the given coordinates on the [Game.board].
     *
     * @param block [Block] that is getting placed.
     * @param coordinates Location where the [Block] is getting placed.
     *
     * Preconditions:
     * - The cells where we want to place the block should be empty
     *
     * Post-conditions:
     * - The cells where we place the block should be filled
     *
     * @throws IllegalStateException when there is a invalid block placement
     *
     */
    fun placeBlock(block: Block, coordinates: Pair<Int, Int>){
        if (!isValidBlockPlacement(block, coordinates)) return
        val color = rootService.gameService.getCurrentColor()

        val game = rootService.currentGame

        val x = coordinates.first
        val y = coordinates.second

        val heightOfBlock = block.blueprint.size
        val widthOfBlock = block.blueprint[0].size

        val board = game.board
        checkNotNull(board){"Board should not be null"}

        for (currentY in 0 until heightOfBlock) {
            for (currentX in 0 until widthOfBlock) {
                if (block.blueprint[currentY][currentX] == 1) {
                    board[y + currentY][x + currentX] = color.colorType
                }
            }
        }
        game.board = board

        // Add block to the discard blocks
        color.blocks.removeIf { it.blockName == block.blockName }
        color.discardedBlocks.add(block)
        color.lastPlacedByPlayer = rootService.gameService.getCurrentPlayer()

        val allPlayers = game.colors.flatMap { it.players }.distinct()
        allPlayers.forEach { player ->
            rootService.gameService.calculateScore(player)
        }
        val currentPlayerType = rootService.gameService.getCurrentPlayer().playerType
        onAllRefreshables { refreshAfterBlockPlaced() }
        rootService.gameService.endTurn()
        if(rootService.currentGame.isOnline && currentPlayerType!= PlayerType.ONLINE){
            rootService.networkService.sendAction(block.blueprint, block.blockName, block.isMirrored, coordinates)
        }
        rootService.currentGame.nextGame=null
        rootService.gameService.gameBeforeTurn?.nextGame=null
    }

    /**
     * Rotates a [Block] by 90 degrees to the right.
     * @param currentBlock [Block] that gets rotated.
     * Preconditons:
     * - Valid selected block
     *
     * Post-conditons:
     * - Block should be rotated by 90 degrees.
     */
    fun rotateBlock(block: Block, callRefreshable: Boolean) : Block{
        val currentBluePrint = block.blueprint

        val height = currentBluePrint.size
        val width = currentBluePrint[0].size


        // we create a transposed array of the block
        val rotatedBluePrint = Array(width){IntArray(height)}

        for (y in 0 until height){
            for (x in 0 until width){
                rotatedBluePrint[x][height - 1 - y] = currentBluePrint[y][x]
            }
        }

        block.blueprint = rotatedBluePrint

        block.rotation = Rotation.entries[(block.rotation.ordinal + 1) % Rotation.entries.size]

        if (callRefreshable) onAllRefreshables { refreshSingleBlock(block) }

        return block
    }

    /**
     * Mirrors a [Block] in the center of the matrix.
     *
     * @param block [Block] that gets mirrored.
     *
     * Preconditions:
     * - A valid block should be selected
     *
     * Post-conditions:
     * - The block should be mirrored
     */
    fun mirrorBlock(block: Block, callRefreshable: Boolean): Block{
        //Implemented function that mirrors the matrix of a block in the middle.
        val currentBlock = block.blueprint
        val width = currentBlock[0].size
        val height = currentBlock.size
        // Only mirror if width is greater than 1.
        if (width > 1) {
            // For each line of the matrix:
            for (lineIndex in 0..<height) {
                // Reverse the order of its elements.
                currentBlock[lineIndex].reverse()
            }
        }

        block.isMirrored = !(block.isMirrored)

        if (callRefreshable) onAllRefreshables { refreshSingleBlock(block) }

        return block
    }

    /**
     * Checks if [Block] can be placed at given coordinates.
     *
     * @param block [Block] that gets tested.
     * @param coordinates Coordinates where the test is happening.
     *
     * @return true if [Block] can be placed. false if [Block] can not be placed.
     */
    fun isValidBlockPlacement(block: Block, coordinates: Pair<Int, Int>, customColor: ColorType? = null): Boolean{
        val colorTypeToCheck = customColor ?: rootService.gameService.getCurrentColor().colorType

        val game = rootService.currentGame
        val colorObject = game.colors.find { it.colorType == colorTypeToCheck } ?: return false

        val blockingBoardOfColor = colorObject.blockingBoard
        checkNotNull(blockingBoardOfColor){"Block board should not be null"}

        val validityBoardOfColor = colorObject.validityBoard
        checkNotNull(validityBoardOfColor){"Validity board should not be null"}

        // we retrieve the size of the board
        val heightOfBoard = blockingBoardOfColor.size
        val widthOfBoard = blockingBoardOfColor[0].size

        // we retrieve the size of the block to be placed
        val heightOfBlock = block.blueprint.size
        val widthOfBlock = block.blueprint[0].size

        // retrieve the coordinates of where the block will be placed
        val x = coordinates.first
        val y = coordinates.second

        // check if we are not outside the board
        if (widthOfBlock + x > widthOfBoard || heightOfBlock + y > heightOfBoard) return false
        if (x < 0 || y < 0) return false

        // we slice a part of the blocking board to compare it with the block we want to place
        val slicedBlockingBoard = blockingBoardOfColor
            .sliceArray(y until (y + heightOfBlock))
            .map { row -> row.sliceArray(x until (x + widthOfBlock)) }
            .toTypedArray()

        // we slice a part of the validity board to compare it with the block we want to place
        val slicedValidityBoard = validityBoardOfColor
            .sliceArray(y until (y + heightOfBlock))
            .map { row -> row.sliceArray(x until (x + widthOfBlock)) }
            .toTypedArray()

        // boolean variable to check if the block to be placed touches the corner of another block with
        // the same color
        var touchesCorner = false

        // we compare and see if all the space occupied by the block are available
        // and also if a corner is touched
        for (currentY in 0 until heightOfBlock) {
            for (currentX in 0 until widthOfBlock) {

                // we select a cell from the blocking board
                val cellBlockingBoard = slicedBlockingBoard[currentY][currentX]

                // we select a cell from the validity board
                val cellValidityBoard = slicedValidityBoard[currentY][currentX]

                // we select a cell from the block
                val cellBlock = block.blueprint[currentY][currentX]

                // if the block cell to be placed is empty we skip this cell
                if (cellBlock == 0) continue

                // if the current cell of the validity board overlaps with the block we return false
                if (cellBlockingBoard != ColorType.NONE) return false

                // if we fill the cell of a corner we switch the boolean variable touchesCorner to true
                if (cellValidityBoard == 1) touchesCorner = true

            }
        }

        return touchesCorner
    }
}