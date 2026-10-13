package service.networkService

import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.Color
import edu.udo.cs.sopra.ntf.InitMessage
import edu.udo.cs.sopra.ntf.Rotation
import entity.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import service.RootService
import service.network.ConnectionState
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import kotlin.test.*

/**
 * Class that provides test for the network service and the network client.
 * It connects to the SoPra BGW-Net server.
 * */
class TetsNetworkService {
    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService

    /**
     * initialize the connection to the server and choose a random Session Id
     * */
    private fun initConnections() {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        val randomSessionId = (0..1000).random()
        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "Test Host", randomSessionId.toString())
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, "Test Guest", randomSessionId.toString())
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * starts a two player game with advanced scoring.
     * */
    private fun startTwoPlayerGame( scoringStrategy: ScoringStrategy ) {
        val p1 = Triple("Test Host", PlayerType.LOCAL, ColorType.YELLOW)
        val p2 = Triple("Test Guest", PlayerType.ONLINE, ColorType.BLUE)

        rootServiceHost.networkService.startNewHostedGame(
            mutableListOf(p1,p2),
            GameMode.TWO_PLAYER_SMALL,
            scoringStrategy
        )
    }

    /**
     * tests that all players are in the playerNames list in the right order.
     * */
    @Test
    fun testHostAndJoin(){
        initConnections()
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        val hostClient = rootServiceHost.networkService.client
        val guestClient = rootServiceGuest.networkService.client

        checkNotNull(hostClient)
        checkNotNull(guestClient)

        //names in right order
        assertTrue(hostClient.playerNames.size==2)
        assertTrue(hostClient.playerNames.first()=="Test Host")
        assertTrue(hostClient.playerNames.last()=="Test Guest")
        assertTrue(hostClient.localPlayerType== PlayerType.LOCAL)

        //test with bot
        hostClient.localPlayerType = PlayerType.BOTHARD
        assertTrue(hostClient.localPlayerType== PlayerType.BOTHARD)
    }

    /**
     * tests if the game starts properly with all players that joined.
     * */
    @Test
    fun testStartGame(){
        initConnections()
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        val p1 = Triple("Test Host", PlayerType.LOCAL, ColorType.BLUE)
        val p2 = Triple("Test Guest", PlayerType.ONLINE, ColorType.YELLOW)
        rootServiceHost.networkService.startNewHostedGame(
            mutableListOf(p1,p2),
            GameMode.TWO_PLAYER_SMALL,
            ScoringStrategy.BASIC
        )

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAIT_FOR_MY_TURN)
        assertTrue(rootServiceHost.gameService.getCurrentPlayer().name=="Test Host")
        assertTrue(rootServiceGuest.gameService.getCurrentPlayer().name=="Test Host")
    }

    /**
     * rotates, mirrors and places the block, to test if the messages the player sends and receives from the server
     * are correct.
     * */
    @Test
    fun testPlaceBlock(){
        initConnections()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        val hostPAS = rootServiceHost.playerActionService
        val guestPAS = rootServiceGuest.playerActionService

        val p1 = Triple("Test Host", PlayerType.LOCAL, ColorType.BLUE)
        val p2 = Triple("Test Guest", PlayerType.ONLINE, ColorType.YELLOW)

        rootServiceHost.networkService.startNewHostedGame(
            mutableListOf(p1,p2),
            GameMode.TWO_PLAYER_SMALL,
            ScoringStrategy.BASIC
        )

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAIT_FOR_MY_TURN)

        assertTrue(rootServiceHost.currentGame.isOnline)
        assertTrue(rootServiceGuest.currentGame.isOnline)

        //finds a block with the name BlockType.O1
        val block = rootServiceHost.gameService.getCurrentColor().blocks.find{it.blockName == BlockType.O1 }
        checkNotNull(block)

        assertTrue(rootServiceHost.networkService.connectionState== ConnectionState.PLAYING_MY_TURN)

        hostPAS.placeBlock(block, Pair(0,0))

        rootServiceHost.waitForState(ConnectionState.WAIT_FOR_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        // compares if the two boards are equal so far
        var hostBoard = rootServiceHost.currentGame.board
        var guestBoard = rootServiceHost.currentGame.board
        checkNotNull(hostBoard)
        checkNotNull(guestBoard)
        assertTrue(boardEquals(hostBoard,guestBoard))
        assertTrue(rootServiceGuest.gameService.getCurrentPlayer().playerType== PlayerType.LOCAL)

        //places a second block
        val block2 = rootServiceGuest.gameService.getCurrentColor().blocks.find{it.blockName == BlockType.V3 }
        checkNotNull(block2)
        guestPAS.rotateBlock(block2, false)
        guestPAS.rotateBlock(block2, false)
        guestPAS.mirrorBlock(block2, false)
        guestPAS.placeBlock(block2, Pair(0,12))

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAIT_FOR_MY_TURN)

        hostBoard = rootServiceHost.currentGame.board
        guestBoard = rootServiceHost.currentGame.board
        checkNotNull(hostBoard)
        checkNotNull(guestBoard)
        assertTrue(boardEquals(hostBoard,guestBoard))
    }

    /**
     * Test if we can send a wrong rotation of a block
     */
    @Test
    fun testWrongRotation(){
        initConnections()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        startTwoPlayerGame(ScoringStrategy.BASIC)

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAIT_FOR_MY_TURN)

        val block = rootServiceHost.gameService.getCurrentColor().blocks.find{it.blockName == BlockType.O1 }
        checkNotNull(block)

        val fakeBlueprint = arrayOf(
            intArrayOf(0,1,1,1,1,1)
        )
        assertThrows<IllegalStateException>
        {rootServiceHost.networkService.sendAction(fakeBlueprint,
            BlockType.O1,
            false,
            Pair(0,0))}
    }

    /**
     * connection failed message when host or guest cant connect to server
     * */
    @Test
    fun connectionFailed(){
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        assertThrows<IllegalStateException>{
            rootServiceHost.networkService.hostGame("wrong secret", "Host", "fakeID")
        }
        assertThrows<IllegalStateException>{
            rootServiceGuest.networkService.joinGame("wrong secret", "Guest", "fakeID")
        }
    }


    /**
     * the state of the guest must be [WAITING_FOT_INIT], the test uses [PLAYING_MY_TURN].
     * */
    @Test
    fun testNewJoinedGameWrongState(){
        initConnections()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        rootServiceGuest.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        val p1 = Pair("Test Host", ColorType.BLUE)
    val p2 = Pair("Test Guest", ColorType.YELLOW)

        val initMessage= rootServiceGuest.networkService.createInitMessage(
            mutableListOf(p1,p2),
            GameMode.TWO_PLAYER_SMALL,
            true)

        assertThrows<IllegalStateException> { rootServiceGuest.networkService.startNewJoinedGame(initMessage) }
    }

    /**
     * a game should not be able to start with less than the required number of players.
     * */
    @Test
    fun testNotEnoughPlayersJoined(){
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        val randomSessionId = (0..1000).random()
        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "Test Host", randomSessionId.toString())
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, "Test Guest", "wrong session ID")
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)

        rootServiceHost.networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)

        assertThrows<IllegalStateException>{startTwoPlayerGame( ScoringStrategy.BASIC)}
    }

    /**
     * The same player should not be able to join a second time.
     * Session Id is set automatically to Blokus if nothing is entered.
     * */
    @Test
    fun testConnectionFail(){
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "Test Host", null)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, "Test Guest", "Blokus")
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)

        assertThrows<IllegalArgumentException> {
            rootServiceGuest.networkService.joinGame(NETWORK_SECRET, "Test Guest", "Blokus")
        }
    }

    /**
     * only the current player is able to place a block.
     * */
    @Test
    fun testNotPlayersTurn(){
        initConnections()
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        startTwoPlayerGame(ScoringStrategy.ADVANCED)

        val block = rootServiceHost.gameService.getCurrentColor().blocks.find{it.blockName == BlockType.O1 }
        assertNotNull(block)

        rootServiceHost.networkService.updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        rootServiceHost.waitForState(ConnectionState.WAIT_FOR_MY_TURN)
        assertThrows<IllegalStateException> {
            rootServiceHost.networkService.sendAction(
                block.blueprint, BlockType.O1,false,Pair(0,0))
        }
    }

    /**
     * Tests the [ConnectionState] and if the client for every player is null after leaving the game
     * */
    @Test
    fun testDisconnect(){
       initConnections()

        rootServiceHost.networkService.disconnect()
        rootServiceGuest.networkService.disconnect()

        assertEquals(ConnectionState.DISCONNECTED, rootServiceHost.networkService.connectionState)
        assertEquals(ConnectionState.DISCONNECTED, rootServiceGuest.networkService.connectionState)

        assertNull(rootServiceHost.networkService.client)
        assertNull(rootServiceGuest.networkService.client)

        initConnections()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)

        assertEquals(ConnectionState.WAITING_FOR_GUEST, rootServiceHost.networkService.connectionState)
        assertEquals(ConnectionState.WAITING_FOR_INIT, rootServiceGuest.networkService.connectionState)

        assertDoesNotThrow{rootServiceHost.networkService.disconnect()}
    }


    /**
     * busy waiting for the game represented by this [RootService] to reach the desired network [state].
     * Polls the desired state every 100 ms until the [timeout] is reached.
     *
     * This is a simplification hack for testing purposes, so that tests can be linearized on
     * a single thread.
     *
     * @param state the desired network state to reach
     * @param timeout maximum milliseconds to wait (default: 5000)
     *
     * @throws IllegalStateException if desired state is not reached within the [timeout]
     */
    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 35000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connectionState == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }

    /**
     * This function tests check if it is false
     */
    @Test
    fun testOnCreateGameResponseInvalidState(){
        initConnections()

        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.DISCONNECTED)

        val response = CreateGameResponse(CreateGameResponseStatus.SERVER_ERROR, null)

        assertThrows<IllegalStateException> { client.onCreateGameResponse(response) }
    }

    /**
     * Try to host two games with the same sessionID to trigger the error
     */
    @Test
    fun testOnCreateGameResponseDisconnect(){
        rootServiceHost = RootService()
        val crashHost = RootService()
        val sessionID = "Crash"

        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "Test Host1", sessionID)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)

        crashHost.networkService.hostGame(NETWORK_SECRET, "Test Host2", sessionID)

        crashHost.waitForState(ConnectionState.DISCONNECTED)

        assertEquals(ConnectionState.DISCONNECTED, crashHost.networkService.connectionState)
    }

    /**
     * Tests if onJoinGameResponse works when the host is in the false state
     */
    @Test
    fun testOnJoinGameResponseInvalidState(){
        initConnections()

        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.DISCONNECTED)

        val response = JoinGameResponse(JoinGameResponseStatus.SUCCESS, null, listOf("Frechdachs"), "tmm")

        assertThrows<IllegalStateException> { client.onJoinGameResponse(response) }
    }

    /**
     * Tests onPlayerJoined works when the host is in the false state
     */
    @Test
    fun testOnPlayerJoinedInvalidState(){
        initConnections()
        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.CONNECTED)

        val response = PlayerJoinedNotification("Joined Player", "MessiTheGoat")

        assertThrows<IllegalStateException> { client.onPlayerJoined(response) }
    }

    /**
     * Tests if onPlayerJoined works when we have too much player but the right state
     */
    @Test
    fun testOnPlayerJoinedTooMuchPlayer(){
        initConnections()
        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
        client.playerNames.add("Ronaldo")
        client.playerNames.add("Reus")
        client.playerNames.add("Leon")

        val response = PlayerJoinedNotification("Joined Player", "MessiTheGoat")

        assertThrows<IllegalStateException> { client.onPlayerJoined(response) }

    }

    /**
     * Tests if onPlayerJoined is in the right state
     */
    @Test
    fun testOnPlayerJoinedCorrectState(){
        initConnections()
        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.WAITING_FOR_INIT)

        val response = PlayerJoinedNotification("Joined Player", "MessiTheGoat")

        val playersSize = client.playerNames.size
        client.onPlayerJoined(response)

        assertEquals(ConnectionState.WAITING_FOR_INIT, client.networkService.connectionState)
        assertTrue { playersSize + 1 == client.playerNames.size }
    }

    /**
     * Tests if onInitReceived works when the host is in the false state
     */
    @Test
    fun testOnInitReceivedInvalidState(){
        initConnections()
        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.CONNECTED)

        val message = InitMessage(listOf(Pair("R9", Color.BLUE)),
            edu.udo.cs.sopra.ntf.GameMode.TWO_PLAYER_SMALL,
            false)

        assertThrows<IllegalStateException> { client.onInitReceived(message, "Host") }
    }

    /**
     * Tests if onGameActionReceived works when the host is in the false state
     */
    @Test
    fun testOnGameActionReceivedInvalidState(){
        initConnections()
        val host = rootServiceHost.networkService
        val client = host.client
        checkNotNull(client)

        host.updateConnectionState(ConnectionState.CONNECTED)

        val received = ActionMessage(false,
            coords = Pair(0,0),
            Rotation.NONE,
            edu.udo.cs.sopra.ntf.BlockType.V3)

        assertThrows<IllegalStateException> { client.onGameActionReceived(received, "Host") }
        assertEquals(false, received.isMirrored)
        assertEquals(Pair(0,0), received.coords)
        assertEquals(Rotation.NONE , received.rotation)
        assertEquals(edu.udo.cs.sopra.ntf.BlockType.V3, received.blockType)
    }


    /**
     * compares two boards.
     * */
    private fun boardEquals(b1 : Array<Array<ColorType>>, b2:Array<Array<ColorType>>):Boolean{
        if(b2.size!=b1.size){return false}
        for((lineIndex, line) in b1.withIndex()){
            for((columnIndex, column) in line.withIndex()){
                if(column!=b2[lineIndex][columnIndex]){
                    return false
                }
            }
        }
        return true
    }

    /**set the network secret*/
    companion object {
        const val NETWORK_SECRET = "blocksAgain"
    }
}