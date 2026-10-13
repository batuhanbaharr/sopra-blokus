package service.network

import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.InitMessage
import edu.udo.cs.sopra.ntf.Rotation
import entity.*
import entity.Extensions.toBlockType
import entity.Extensions.toColorType
import entity.Extensions.toGameMode
import entity.Extensions.toNTFBlockType
import entity.Extensions.toNTFColor
import entity.Extensions.toNTFGameMode
import service.AbstractRefreshingService
import service.RootService


/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games. Bridges between the [GameNetworkClient] and the other services.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {
    /** Network client. Nullable for offline games. */
    var client: GameNetworkClient? = null
        private set

    var playerType = PlayerType.LOCAL

    /**
     * current state of the connection in a network game.
     */
    var connectionState: ConnectionState? = ConnectionState.DISCONNECTED
    private set

    /**
     * Connects to server and creates a new game session.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(secret: String, name: String, sessionID: String?) {
        // Establish a connection
        if(!connect(secret,name)) {
            error("connection failed")
        }
        // Set the correct state
        updateConnectionState(ConnectionState.CONNECTED)

        // Check if there is already a sessionID
        if(sessionID.isNullOrBlank()){
            // If there is not one, create a new game
            client?.createGame(GAME_ID, "Welcome to Blokus!")
        }else{
            // if there is one, create the game with this sessionID
            client?.createGame(GAME_ID, sessionID,"Welcome to Blokus!")
        }
        // update the state
        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * connects to server.
     * @param secret server secret. for our project its: "blocksAgain"
     * @param name joined player name.
     * @param sessionID identifier of the hosted session
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     * */
    fun joinGame(secret: String, name: String, sessionID: String) {
        // Establish a connection
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        // update the state
        updateConnectionState(ConnectionState.CONNECTED)
        // Join the game
        client?.joinGame(sessionID, "Hello!")
        // update the state
        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * Set up the game with [service.GameService.startNewGame] and send the Init message to all
     * guest players. This function should be called from [?] when enough players joined.
     *
     * @param players List of players to start the game with.
     * @param gameMode GameMode of the game that is getting initialized.
     * @param scoringStrategy ScoringStrategy which is used to calculate the score of the game.
     *
     * */
    fun startNewHostedGame(players : MutableList<Triple<String, PlayerType, ColorType>>,
                           gameMode: GameMode,
                           scoringStrategy: ScoringStrategy)
    {
        //Checks if we are currently in the correct connection state
        check(connectionState == ConnectionState.WAITING_FOR_GUEST)
        { "currently not prepared to start a new hosted game." }

        //Creates playerList for creation of init message
        val initList : MutableList<Pair<String, ColorType>> = mutableListOf()
        players.forEach { player ->
            initList.add(Pair(player.first, player.third))
        }

        val playerList : MutableList<Triple<String, PlayerType, ColorType>> = mutableListOf()

        players.forEach { player ->
            if(player.first==client?.playerName){
                val localType = client?.localPlayerType
                checkNotNull(localType)
                playerList.add(Triple(player.first, localType, player.third))
            }
            else{
                playerList.add(Triple(player.first, PlayerType.ONLINE, player.third))
            }
        }



        //sets boolean for isAdvancedScoring for creation of init message
        var isAdvancedScoring = false
        if(scoringStrategy == ScoringStrategy.ADVANCED){isAdvancedScoring = true}

        //creates InitMessage
        val initMessage = createInitMessage(initList,gameMode, isAdvancedScoring)

        //Starts the game
        rootService.gameService.startNewGame(playerList, gameMode, scoringStrategy, true)

        //Checks if local player is currently playing and updates the ConnectionState
        if(rootService.gameService.getCurrentPlayer().playerType == PlayerType.ONLINE){
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }
        else{
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }

        onAllRefreshables { refreshAfterGameLoaded() }

        //Sends initMessage to other clients.
        client?.sendGameActionMessage(initMessage)
    }

    /**
     * This function sets up a local game with the InitMessage
     *
     * @param message The message we got from the client
     *
     * @throws IllegalStateException when the connection state is not correct
     */
    fun startNewJoinedGame(message: InitMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not waiting for game init message. " }
        val playerList : MutableList<Triple<String, PlayerType, ColorType>> = mutableListOf()
        // Take the players from the InitMessage and add them to the local playerList
        message.players.forEach { player ->
            // Check whether we need to add a local or an online player
            if(player.first == client?.playerName) {
                val playerName = client?.playerName
                val playerType = client?.localPlayerType
                checkNotNull(playerName)
                checkNotNull(playerType)
                playerList.add(Triple(playerName,playerType, player.second.toColorType()))
            }
            // In case it is not the local player
            else{playerList.add(Triple(player.first, PlayerType.ONLINE, player.second.toColorType()))}
        }
        // set the right scoringStrategy based on the message
        var scoringStrategy = ScoringStrategy.BASIC
        if(message.isAdvancedScoring){scoringStrategy = ScoringStrategy.ADVANCED}
        // Start a local game based on the information in the message from the host
        rootService.gameService.startNewGame(playerList, message.gameMode.toGameMode(),
            scoringStrategy, true)
        // check if it is the local player's turn and set the correct state
        if(rootService.gameService.getCurrentPlayer().playerType == PlayerType.ONLINE){
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }
        else{
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
        onAllRefreshables { refreshAfterGameLoaded() }
    }



    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param secret Network secret. Must not be blank (i.e. empty or only whitespaces)
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(secret: String, name: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(name.isNotBlank()) { "player name must be given" }

        // Initialize the client
        val newClient =
            GameNetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this,
                playerType
            )

        // Try to connect the client with the server
        return if (newClient.connect()) {
            // If it works save the client and return true
            this.client = newClient
            true
        } else {
            false
        }

    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     * */
    fun disconnect(){
        client?.apply{
            // If there is a client and a game, then exit from the game
            if(sessionID!=null)leaveGame("you left the game.")
            // If the connection is still alive, then disconnect
            if(isOpen) disconnect()
        }
        // Set the client on null and update the state
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshables via [Refreshable.refreshConnectionState]
     * @param newState the connectionState we want to set
     */
    fun updateConnectionState(newState: ConnectionState) {
        // update the current state
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    /**
     * Creates an [InitMessage] with the correct parameters to start a game.
     *
     * @param players List of Players that start the game.
     * @param gameMode the chosen mode.
     * @param isAdvancedScoring to select the correct scoring calculation method.
     *
     * */
    fun createInitMessage(players: MutableList<Pair<String, ColorType>>,
                 gameMode : GameMode,
                 isAdvancedScoring: Boolean): InitMessage{
        //creates list of Pairs with player name and NtfColor for the init message
        val playersToSend : MutableList<Pair<String, edu.udo.cs.sopra.ntf.Color>> = mutableListOf()
        players.forEach { player ->
            val color = player.second.toNTFColor()
            playersToSend.add(Pair(player.first, color))
        }

        //casts the GameMode to NTFGameMode
        val gameModeToSend = gameMode.toNTFGameMode()

        //Creates InitMessage
        val initMessage = InitMessage(playersToSend,gameModeToSend,isAdvancedScoring)
        return initMessage
    }

    /**
     * Sends an Action message if the player ends the move
     *
     * @param blueprint the blueprint of the block
     * @param blockType The block we want to mirror, rotate
     * @param coords the coordinations where block placed
     * @param isMirrored detects if block was mirrored
     * */
    fun sendAction(blueprint: Array<IntArray>, blockType: BlockType, isMirrored: Boolean, coords: Pair<Int, Int>) {
        check(connectionState == ConnectionState.PLAYING_MY_TURN)
        {"Not your turn right now."}
        // Temporary block to find the right position of the block
        val blockTypeTemplate = blockType
        val blockTemplate = Block(blockTypeTemplate)
        var blockMirrored = isMirrored
        // Check if the block is mirrored
        if(blockMirrored){
            rootService.playerActionService.mirrorBlock(blockTemplate, false)
        }
        // Now we want to find the rotation of the block
        var rotationCount = -1
        // Check for the current rotation and if false then we try the other rotations
        if(blueprintEquals(blueprint, blockTemplate.blueprint)){
            rotationCount=0
        }
        rootService.playerActionService.rotateBlock(blockTemplate, false)
        if(blueprintEquals(blueprint, blockTemplate.blueprint)){
            rotationCount=1
        }
        rootService.playerActionService.rotateBlock(blockTemplate, false)
        if(blueprintEquals(blueprint, blockTemplate.blueprint)){
            rotationCount=2
        }
        rootService.playerActionService.rotateBlock(blockTemplate, false)
        if(blueprintEquals(blueprint, blockTemplate.blueprint)){
            rotationCount=3
        }
        //compares the block with blueprints and returns the amount of rotations
        if(rotationCount==-1){
            blockMirrored = !blockMirrored
            rootService.playerActionService.rotateBlock(blockTemplate, false)
            rootService.playerActionService.mirrorBlock(blockTemplate, false)
            if(blueprintEquals(blueprint, blockTemplate.blueprint)){
                rotationCount=0
            }
            rootService.playerActionService.rotateBlock(blockTemplate, false)
            if(blueprintEquals(blueprint, blockTemplate.blueprint)){
                rotationCount=1
            }
            rootService.playerActionService.rotateBlock(blockTemplate, false)
            if(blueprintEquals(blueprint, blockTemplate.blueprint)){
                rotationCount=2
            }
            rootService.playerActionService.rotateBlock(blockTemplate, false)
            if(blueprintEquals(blueprint, blockTemplate.blueprint)){
                rotationCount=3
            }
        }
        check(rotationCount!=-1){"Rotation did not get calculated correctly"}
        // Now we create the actionMessage
        val actionMessage = ActionMessage(
            blockMirrored,
            coords,
            Rotation.entries[rotationCount],
            blockType.toNTFBlockType()
        )
        // check if it is the local player's turn and set the correct state
        if(rootService.gameService.getCurrentPlayer().playerType == PlayerType.ONLINE){
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }
        else{
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
        // Send the created actionMessage to the clients
        client?.sendGameActionMessage(actionMessage)
    }

    /**
     * receive the action from other players
     *
     * @param rotation rotation of block
     * @param blocktype type of played block
     * @param isMirrored detects if block was mirrored
     * @param coords the coordinations where block placed
     * */
    fun receiveAction(rotation : edu.udo.cs.sopra.ntf.Rotation,
                      blocktype : edu.udo.cs.sopra.ntf.BlockType,
                      coords : Pair<Int,Int>,
                      isMirrored : Boolean) {
        // Search for the current color
        val color = rootService.gameService.getCurrentColor()
        // Casts the NTFBlocktype to BlockType
        val blockType : BlockType = blocktype.toBlockType()
        // Find the right block from the player
        val block = color.blocks.find { it.blockName==blockType }
        checkNotNull(block) {"Block not avaiable"}
        val blockCopy = Block(block.blockName)
        block.blueprint=blockCopy.blueprint
        block.isMirrored=blockCopy.isMirrored
        block.rotation=blockCopy.rotation
        // Now set the block in the right position
        if(isMirrored){
            rootService.playerActionService.mirrorBlock(block,true)
        }
        repeat(rotation.ordinal){
            rootService.playerActionService.rotateBlock(block, true)
        }
        // Place the block on the right position
        rootService.playerActionService.placeBlock(block, coords)
        // check if it is the local player's turn and set the correct state
        if(rootService.gameService.getCurrentPlayer().playerType == PlayerType.ONLINE){
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }
        else{
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
    }


    /**
     * Checks if two blueprints are equal
     *
     * @param b1 the blueprint we want to check
     * @param b2 teh second blueprint we want to check
     */
    private fun blueprintEquals(b1 : Array<IntArray>, b2:Array<IntArray>):Boolean{
        // Check if the size is equal if not return false
        if(b2.size!=b1.size){return false}
        // Now check if the cells are equal
        for((lineIndex, line) in b1.withIndex()){
            for((columnIndex, column) in line.withIndex()){
                // If there is a difference then return false
                if(column!=b2[lineIndex][columnIndex]){
                    return false
                }
            }
        }
        return true
    }

    /**
     * ends the game when called.
     * */
    fun endGameEarly(){
        rootService.gameService.endGame()
    }

    /**companion object for the server we connect to*/
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Blokus"

        const val NETWORK_SECRET = "blocksAgain"
    }
}