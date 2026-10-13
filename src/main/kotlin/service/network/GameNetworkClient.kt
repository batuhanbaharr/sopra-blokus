package service.network

import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.InitMessage
import entity.PlayerType
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

/**
 * This class is used to receive and send messages from the game
 *
 * @param playerName the name of the local player
 * @param host is the address of the server
 * @param secret the secret that we need for authentication
 * @param networkService the param to handle incoming actions
 * @param localPlayerType is the [PlayerType] of the local player
 *
 * @property sessionID the ID of the created game
 * @property playerNames The players that are in the current online game
 *
 */
class GameNetworkClient(playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
    var localPlayerType : PlayerType
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    val playerNames : MutableList<String> = mutableListOf(playerName)

    /**
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response the response we get after CreateGame
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.run {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            when (response.status) {
                // When there is a game
                CreateGameResponseStatus.SUCCESS -> {
                    // Then update the state and save the SessionId
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                    sessionID = response.sessionID
                }// When there is no game then disconnect and send an error
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle a [JoinGameResponse] sent by the server. Guest will wait for the initMessage
     * when its status is [ConnectionState.WAITING_FOR_INIT].
     * As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response The response we get after joining a game
     *
     * @throws IllegalStateAcception if status != success or currently not waiting for a game creation response
     * */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState== ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        {"Currently in wrong state."}

        when (response.status){
            // When the join is successful
            JoinGameResponseStatus.SUCCESS ->{
                // Then update the state of the player
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
            } // When there is an error then disconnect
            else -> {
                disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server. Waiting for more players to join,
     * or the start of the hosted game.
     *
     * @param notification The information about the player that joined
     *
     * @throws IllegalStateException if no more guests are expected
     * */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUEST ||
                networkService.connectionState == ConnectionState.WAITING_FOR_INIT)
        {"Not awaiting any guests."}

        check(playerNames.size < 4) {"Maximum players already reached."}
        // Add the joined player
        playerNames.add(notification.sender)

        val connectionState = networkService.connectionState
        checkNotNull(connectionState)
        networkService.updateConnectionState(connectionState)
    }

    /**
     * Handle a [GameActionResponse] sent by the server. Waiting for the start of the hosted game.
     *
     * @throws IllegalStateException if no more guests are expected
     * */
    /*override fun onGameActionResponse(response: GameActionResponse) {
        check(networkService.connectionState == ConnectionState.WAIT_FOR_MY_TURN)
        {"Can not receive init messages as host."}
    }*/

    /**
     * Handle a [InitMessage] sent by the server.
     *
     * @param message This message contains information about the game like players, scoring and gameMode
     * @param sender Name of the player that sended the message
     *
     * @throws IllegalStateException when the connectionState is not correct
     * */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message : InitMessage, sender : String){
        check(networkService.connectionState == ConnectionState.WAITING_FOR_INIT)
        {"Can not receive init messages as host."}
        // Create a game local
        networkService.startNewJoinedGame(message)
    }

    /**
     * Handle a [InitMessage] sent by the server.
     *
     * @param recieved contains the information about the game action that was performed
     * @param sender the Player that sended the game action
     *
     * @throws IllegalStateException when the connectionState is not correct
     * */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onGameActionReceived(recieved : ActionMessage, sender: String){
        check(networkService.connectionState == ConnectionState.WAIT_FOR_MY_TURN)
        {"Currently in wrong state."}
        // Synchronise the game
        networkService.receiveAction(recieved.rotation,recieved.blockType, recieved.coords, recieved.isMirrored)

    }

    /**
     * This function disconnects the client from the server
     *
     * @param message The error message of the disconnect
     */
    private fun disconnectAndError(message: Any) {
        // Disconnect the game and send an error message
        networkService.disconnect()
        error(message)
    }

    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        playerNames.remove(notification.sender)
        val connectionState = networkService.connectionState
        checkNotNull(connectionState)
        if(networkService.connectionState!= ConnectionState.WAITING_FOR_GUEST||
            networkService.connectionState!= ConnectionState.WAITING_FOR_INIT){
            networkService.endGameEarly()
        }
        networkService.updateConnectionState(connectionState)
    }

}