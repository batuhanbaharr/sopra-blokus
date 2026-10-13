package service

import entity.*



/**
 * Service to help manage operations handled by the game.
 *
 * @param rootService [RootService] for access to entity layer and other services.
 *
 * @property rootService [RootService] for access to entity layer and other services.
 */
class GameService(private val rootService: RootService): AbstractRefreshingService() {

    var gameBeforeTurn : Game? = null
    /**
     * This function starts a new game and prepares the playing field
     *
     *
     * @param players List of [Player]s in a pair that play the game.
     * @param gameMode [GameMode] in which the [Game] should be played.
     * @param scoringStrategy [scoringStrategy] which is used to determine Score for [Player]s.
     * @param isOnline this shows us if we play local or online
     *
     * Preconditions:
     * - The GameState has to be `INITIALIZED` or `ACTIVE`
     *
     * Post-conditions:
     * - Each player receives 21 blocks
     * - The order has been determined
     * - The game is currently running
     *
     * @throws IllegalStateException If the GameState is not `INITIALIZED` or *`ACTIVE`
     * @throws IllegalArgumentException If [players].size is less than 2
     * @throws IllegalArgumentException If [gameMode] is not local or online
     * @throws IllegalArgumentException If [scoringStrategy] is
     * neither [ScoringStrategy.BASIC] nor [ScoringStrategy.ADVANCED]
     *
     */
    fun startNewGame(players: MutableList<Triple<String, PlayerType, ColorType>>,
                     gameMode: GameMode,
                     scoringStrategy: ScoringStrategy,
                     isOnline: Boolean)
    {
        require(players.size in 2..4) { "The number of players is not within the valid range" }

        val distinctNames = players.distinct()
        require(players.size == distinctNames.size) { "The player names must be unique" }

        if (gameMode == GameMode.FOUR_PLAYER) {
            val distinctColors = players
                .map { it.third }
                .distinct()

            require(players.size == distinctColors.size) { "The player colors must be unique" }
        }

        val game = Game(gameMode, isOnline, scoringStrategy)

        // Initialize game board
        val gameBoardSize = when(gameMode) {
            GameMode.TWO_PLAYER_SMALL -> 14
            else -> 20
        }
        val gameBoard = Array(gameBoardSize){ Array(gameBoardSize){ ColorType.NONE } }
        game.board = gameBoard

        val createPlayer = players.map { (name, playerType) ->
            Player(name, playerType)
        }.toMutableList()

        when(gameMode){
            GameMode.TWO_PLAYER_SMALL,
            GameMode.TWO_PLAYER ->{
                for (colorIndex in 0 ..< game.colors.size) {
                    game.colors[colorIndex].players.add(createPlayer[colorIndex % 2])
                }
            }

            GameMode.THREE_PLAYER ->{
                for (player in players) {
                    val index = when (player.third) {
                        ColorType.BLUE -> 0
                        ColorType.YELLOW -> 1
                        ColorType.RED -> 2
                        ColorType.GREEN -> 3
                        else -> error("Unreachable")
                    }
                    game.colors[index].players.add(Player(player.first, player.second))
                }

                val sharedColor = game.colors.filter { it.players.isEmpty() }
                require(sharedColor.size == 1) { "Only one shared color is allowed" }

                players.forEach { sharedColor[0].players.add(Player(it.first, it.second)) }
                sharedColor[0].sharedPlayerIndex = 0
            }

            GameMode.FOUR_PLAYER ->{
                for (player in players) {
                    game.colors.add(Color(player.third))
                    game.colors.last().players.add(Player(player.first, player.second))
                }
            }
        }

        rootService.currentGame = game
        updateValidityBoards()
        gameBeforeTurn = game.copy()


        // was genau macht dieser Teil?
        val allPlayers = game.colors.flatMap { it.players }.distinct()
        allPlayers.forEach { calculateScore(it) }

        onAllRefreshables { refreshAfterGameLoaded() }
    }



    /**
     * Ends a [Player]s turn and changes the active [Player] to the next [Player] in list.
     *
     * Preconditions:
     * - The GameState has to be `ACTIVE`
     * - The player must have placed his game piece or the player does not have a fitting game
     * piece
     * - The player must be on an active turn
     *
     * Post-conditions:
     * - The turn of the current player is over
     * - The next player is up
     * - If none of the players can make any move, [endGame] is called
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE
     */
    fun endTurn() {
        // we update the validity boards when we end a turn
        updateValidityBoards()

        // Implemented function to end a players turn and call endGame() when the game is over.
        val game = rootService.currentGame
        // Define the default next color index.
        val nextColorIndex = (game.currentColorIndex + 1) % game.colors.size
        // If it's the tree-player game and the color player was shared.
        if (game.gameMode == GameMode.THREE_PLAYER && getCurrentColor().players.size > 1) {
            // Make another player play this color in the next round.
            getCurrentColor()
                .sharedPlayerIndex = (getCurrentColor().sharedPlayerIndex + 1) % getCurrentColor().players.size
        }
        // Create a list to check the following colors for their playability, up till the current color.
        val colorsToCheckList: MutableList<Color> =
            (game.colors.slice(nextColorIndex..<game.colors.size)
                    + game.colors.slice(0..<nextColorIndex)).toMutableList()
        // Iterate through that list
        var anotherTurnPossible = false
        for (colorToCheck in colorsToCheckList) {
            // The first color in that ring to have a valid turn left
            // is assigned to be the next current color
            // and the function returns.
            if (hasValidTurnLeft(colorToCheck)) {
                game.currentColorIndex = game.colors.indexOf(colorToCheck)
                anotherTurnPossible = true
                break
            }
        }
        // If no playable color was found -> end the game.
        if (anotherTurnPossible) {
            game.previousGame?.nextGame = gameBeforeTurn
            gameBeforeTurn?.previousGame = game.previousGame

            game.previousGame = gameBeforeTurn
            gameBeforeTurn?.nextGame = game

            gameBeforeTurn = game.copy()

            onAllRefreshables { refreshAfterTurnEnd() }
        } else {
            endGame()
        }
    }


    /**
     * This function ends the game when no player can make a move
     *
     * Preconditions:
     * - The GameState has to be `ACTIVE`
     * - No player can place a block
     *
     * Post-conditions:
     * - The scores of the players are calculated
     * - The GameState is now 'ENDED'
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE`
     *
     *
     */
    fun endGame(){
        val game = rootService.currentGame

        // find all unique players from all colors
        val allPlayers = mutableListOf<Player>()
        for (color in game.colors) {
            for (player in color.players) {
                if (player !in allPlayers) {
                    allPlayers.add(player)
                }
            }
        }

        //calculate score for all players
        val rankingList = mutableListOf<Pair<String, Int>>()
        for(color in  game.colors){
            if (game.gameMode == GameMode.THREE_PLAYER && color.players.size > 1) continue
            calculateScore(color.players.first())
            val playerRank = Pair(color.players.first().name, color.players.first().score)
            if(!rankingList.contains(playerRank)){
                rankingList.add(playerRank)
            }
        }

        if(game.scoringStrategy == ScoringStrategy.ADVANCED){
            rankingList.sortByDescending { it.second }
        }else {
            //ascending, least squares -> highest rank
            rankingList.sortBy { it.second }
        }

        //refresh after game end
        onAllRefreshables {
            refreshAfterGameEnd(rankingList)
        }
    }

    /**
     * Calculate score of a [Player] with [ScoringStrategy] from game
     * we find all colors that belong to this player and count
     * remaining squares for each color separately
     * in 2 player mode each player control 2 colors so we add up both scores
     * @param player [Player] of which the [Player.score] should be calculated
     */
    fun calculateScore(player: Player) {
        val game = rootService.currentGame
        var totalScore = 0
        // find all colors that belong to this player
        // in 3 player mode ignore the shared color
        val playerColors = if (game.gameMode == GameMode.THREE_PLAYER) {
            game.colors.filter { player in it.players && it.players.size == 1 }
        } else {
            game.colors.filter { player in it.players }
        }

        // calculate score for each color via helper function to avoid nested depth
        for (color in playerColors) {
            totalScore += calculateSingleColorScore(color, game.scoringStrategy)
        }
        player.score = totalScore
    }
    private fun calculateSingleColorScore(color: Color, strategy: ScoringStrategy): Int {
        val remainingSquares = countRemainingSquares(color)
        if (strategy == ScoringStrategy.BASIC) {
            return remainingSquares
        }
        var score = -remainingSquares
        // advanced scoring logic
        if (remainingSquares == 0) {
            score += 15
            val lastDiscarded = color.discardedBlocks.lastOrNull()
            if (lastDiscarded != null && lastDiscarded.blockName == BlockType.O1) {
                score += 5
            }
        }
        return score
    }
    /** count total remaining squares from unplaced blocks of a color */
    private fun countRemainingSquares(color: Color): Int {
        var count = 0
        for (block in color.blocks) {
            count += countSquaresInBlock(block)
        }
        return count
    }
    private fun countSquaresInBlock(block: Block): Int {
        var count = 0
        for (row in block.blueprint) {
            for (cell in row) {
                if (cell == 1) count++
            }
        }
        return count
    }

    /**
     * Checks if [Color] has the possibility to make a move.
     *
     * @param color [Color] of which the possibilities get tested.
     *
     * Preconditions:
     * - The GameState has to be `ACTIVE`
     *
     * Post-conditions:
     * - We know whether the color is still playable.
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE`
     *
     * @return true if color has a possible move left and false if not.
     */
    fun hasValidTurnLeft(color: Color): Boolean {
        //early abort: color is not active anymore
        if(!color.isActive){return false}

        //early abort: Player has no blocks left
        if(color.blocks.isEmpty()) {return false}

        //Setting up values for later use
        val playerAS = rootService.playerActionService
        val validityBoard = color.validityBoard
        checkNotNull(validityBoard)

        //Find the coordinates of the validityBoard
        val validCoord = findBlockOffsets(validityBoard)

        //early abort: validityBoard is empty
        if(validCoord.isEmpty()) {return false}

        //get blocks sorted by color
        val sortedBlocks = sortedBlocks(color.blocks)

        for(block in sortedBlocks){
            val blockOrientation = Pair(block.isMirrored, block.rotation)
            //Check for both mirror orientations
            repeat(2){
                //Check for all 4 rotations
                repeat(4) {
                    //Find the offsets from the block blueprints
                    val blockOffsets = findBlockOffsets(block.blueprint)

                    //Check if any offset is a valid placement.
                    for (offset in blockOffsets) {
                        //tests each coordinate from the validityBoard with the current offset
                        validCoord.forEach { validCoord ->
                            //calculates the coordinate where the block should be placed by subtracting the offset.
                            val coord = Pair(validCoord.first - offset.first, validCoord.second - offset.second)
                            if (isValidPosition(coord)
                                && playerAS.isValidBlockPlacement(block, coord, color.colorType)) {
                                //early abort: block placement found
                                block.rotation = blockOrientation.second
                                block.isMirrored = blockOrientation.first
                                return true
                            }
                        }
                    }
                    //rotates the block by 90 degrees
                    playerAS.rotateBlock(block, false)
                }
                //mirrors the block
                playerAS.mirrorBlock(block, false)
            }
        }
        //no valid block placement found
        color.isActive = false
        return false
    }

    /**
     * Returns true if coordinates are inside the gameBoard
     *
     * @param coord Coordinates which get checked for validity
     *
     * @return true when coordinates are valid. false when coordinates are invalid.
     */
    private fun isValidPosition(coord : Pair<Int, Int>) : Boolean{
        //sets up values for later use
        val game = rootService.currentGame
        val board = game.board
        checkNotNull(board)

        //checks if coordinate is in the game board
        if(coord.first<0 || coord.second<0){ return false}
        if(coord.first>=board.size || coord.second>=board.size) {return false}

        //coordinate is in game board
        return true
    }

    /**
     * Finds and returns the offsets from (0,0) of each element in matrix with value 1
     *
     * @param matrix matrix of which the offsets get calculated
     *
     * @return MutableList of coordinates as Pairs, which show the offset from (0,0)
     */
    private fun findBlockOffsets(matrix: Array<IntArray>):MutableList<Pair<Int,Int>>{
        val returnList : MutableList<Pair<Int,Int>> = mutableListOf()

        //goes through each entry in matrix and checks if the value is 1. Adds those positions to the return list.
        for((y, line) in matrix.withIndex()){
            for((x, value) in line.withIndex()){
                //checks if current value equals 1
                if(value==1){
                    //adds the coordinates to the list
                    returnList.add(Pair(x, y))
                }
            }
        }
        return returnList
    }

    /**
     * Sorts a list of [Block]s by the area of their matrix.
     *
     * @param blocks Blocks which get sorted
     *
     * @return MutableList of [Block]s that are sorted in order by area size.
     */
    private fun sortedBlocks(blocks : MutableList<Block>) : MutableList<Block>{
        //creates a copy of the blocks used
        val blocksCopy = blocks.toMutableList()
        val returnList : MutableList<Block> = mutableListOf()

        //checks if there are blocks left in list that have to be added to returnList
        while(blocksCopy.isNotEmpty()){
            //keeps track of the currently smallest block in the list
            var smallestBlock = blocksCopy[0]
            var smallestBlockSize = smallestBlock.blueprint.size*smallestBlock.blueprint[0].size

            //checks if there is a smaller block in the list
            for(block in blocksCopy){
                val blockSize = block.blueprint.size*block.blueprint[0].size

                //checks if the current block is smaller than the comparison
                if(blockSize<smallestBlockSize){
                    //block is smaller and smallestBlock gets updated
                    smallestBlock = block
                    smallestBlockSize = blockSize
                }
            }
            //add the smallest block to the returnList and remove the block from blocksCopy
            returnList.add(smallestBlock)
            blocksCopy.remove(smallestBlock)
        }

        //List sorted by size of matrix
        return returnList
    }


    /**
     * This function returns the current [Player] object in the [Game]
     * using [Game.currentColorIndex]
     *
     * @return the current player
     */
    fun getCurrentPlayer(): Player {
        val game = rootService.currentGame

        val color = game.colors[game.currentColorIndex]

        check(color.players.isNotEmpty()) { "Diese Farbe hat keinen zugewiesenen Spieler!" }

        val safeIndex = color.sharedPlayerIndex % color.players.size

        return color.players[safeIndex]
    }

    /**
     * This function returns the current [Color] in the [Game] which should be played
     */
    fun getCurrentColor(): Color{
        val game = rootService.currentGame
        return game.colors[game.currentColorIndex]
    }

    /**
     * This function returns true if a given [Color] has already been played
     * in this game, false otherwise.
     */
    private fun colorHasBeenPlayed(color: Color): Boolean{
        val game = rootService.currentGame
        val board = game.board

        checkNotNull(board){"Board should not be null"}
        return board.any{ row ->
            row.any{ cell -> cell == color.colorType}
        }
    }

    /**
     * This function fills the corners of the block with 1 in the validity board of a given color,
     * if certain conditions are met.
     */
    private fun fillCorners(downLeftCorner : Pair<Int,Int>, downRightCorner : Pair<Int,Int>,
                            topLeftCorner : Pair<Int,Int>, topRightCorner : Pair<Int,Int>,
                            newValidityBoard : Array<IntArray>) {
        val game = rootService.currentGame
        val board = game.board
        checkNotNull(board) { "Game board should not be null" }

        val boardHeight = board.size
        val boardWidth = board[0].size

        if(downLeftCorner.first >= 0
            && downLeftCorner.second >= 0
            && board[downLeftCorner.second][downLeftCorner.first] == ColorType.NONE){
            newValidityBoard[downLeftCorner.second][downLeftCorner.first] = 1
        }

        if(downRightCorner.first < boardWidth
            && downRightCorner.second >= 0
            && board[downRightCorner.second][downRightCorner.first] == ColorType.NONE){
            newValidityBoard[downRightCorner.second][downRightCorner.first] = 1
        }

        if(topLeftCorner.first >= 0
            && topLeftCorner.second < boardHeight
            && board[topLeftCorner.second][topLeftCorner.first] == ColorType.NONE){
            newValidityBoard[topLeftCorner.second][topLeftCorner.first] = 1
        }

        if(topRightCorner.first < boardWidth
            && topRightCorner.second < boardHeight
            && board[topRightCorner.second][topRightCorner.first] == ColorType.NONE){
            newValidityBoard[topRightCorner.second][topRightCorner.first] = 1
        }
    }

    /**
     * Calculates and updates the [Player].validityBoard of each [Player].
     */
    private fun updateValidityBoards() {
        val game = rootService.currentGame
        val board = game.board
        checkNotNull(board) { "Game board should not be null" }

        // we iterate through all the players
        for (color in game.colors){
            updateBoardForColor(color, board)
        }
    }

    /**
     * This functions updates the validity and blocking board of a color
     */
    private fun updateBoardForColor(color: Color, board: Array<Array<ColorType>>){
        val boardHeight = board.size
        val boardWidth = board[0].size
        // we create an empty blocking and validity board
        val newBlockingBoard = Array(boardHeight) { Array(boardWidth) { ColorType.NONE} }

        val newValidityBoard = Array(boardHeight) { IntArray(boardWidth) { 0 } }

        val colorHasBeenPlayed = colorHasBeenPlayed(color)
        if (!colorHasBeenPlayed){
            val downLeftCorner = Pair(0, boardHeight - 1)
            val downRightCorner = Pair(boardWidth-1, boardHeight - 1)

            val topLeftCorner = Pair(0, 0)
            val topRightCorner = Pair(boardWidth-1, 0)

            fillCorners(downLeftCorner,downRightCorner,topLeftCorner,topRightCorner,newValidityBoard)

        }

        fillBlockingAndValidity(board, color, colorHasBeenPlayed, newBlockingBoard, newValidityBoard)
        finalizeValidityBoard(newBlockingBoard, newValidityBoard, color.colorType)

        color.blockingBoard = newBlockingBoard
        // I suppose this part was missing.
        color.validityBoard = newValidityBoard
    }

    /**
     * This functions fills the blocking and validity board of a color
     */
    private fun fillBlockingAndValidity(
        board: Array<Array<ColorType>>,
        color: Color,
        hasBeenPlayed: Boolean,
        newBlockingBoard: Array<Array<ColorType>>,
        newValidityBoard: Array<IntArray>
    ) {
        val boardHeight = board.size
        val boardWidth = board[0].size

        for (y in 0 until boardHeight) {
            for (x in 0 until boardWidth) {
                val currentCell = board[y][x]
                if (currentCell == ColorType.NONE) continue

                newBlockingBoard[y][x] = currentCell

                if (currentCell != color.colorType) continue

                // Block neighboring cells
                if ((x - 1) >= 0) newBlockingBoard[y][x - 1] = color.colorType
                if ((x + 1) < boardWidth) newBlockingBoard[y][x + 1] = color.colorType
                if ((y - 1) >= 0) newBlockingBoard[y - 1][x] = color.colorType
                if ((y + 1) < boardHeight) newBlockingBoard[y + 1][x] = color.colorType

                // Fill valid corners
                if (hasBeenPlayed) {
                    fillCorners(
                        Pair(x - 1, y - 1), Pair(x + 1, y - 1),
                        Pair(x - 1, y + 1), Pair(x + 1, y + 1),
                        newValidityBoard
                    )
                }
            }
        }
    }


    /**
     * This functions finalizes the validity board by comparing it to the blocking board
     */
    private fun finalizeValidityBoard(
        newBlockingBoard: Array<Array<ColorType>>,
        newValidityBoard: Array<IntArray>,
        colorType: ColorType
    ) {
        for (y in newBlockingBoard.indices) {
            for (x in newBlockingBoard[y].indices) {
                if (newBlockingBoard[y][x] == colorType) {
                    newValidityBoard[y][x] = 0
                }
            }
        }
    }

}
