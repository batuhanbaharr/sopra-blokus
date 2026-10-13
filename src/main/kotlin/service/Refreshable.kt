package service

import entity.Block
import service.network.ConnectionState

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable{

    /**
     *This function refreshes after we load a saved game or after we started a new game so that
     *we can move to the next scene
     *
     *Preconditions:
     * -The game must be `ACTIVE` or there must be a saved game available
     *
     *Post-conditions:
     * -We switch to the GameScene
     *

     */
    fun refreshAfterGameLoaded(){}

    /**
     *This function refreshes after we placed a block so that we can see the placed block in the
     *gui
     *
     *
     *Preconditions:
     * -The current player must have a valid move and the player placed the block
     *
     *Post-conditions:
     * -We see the placed block in the gui
     *
     */
    fun refreshAfterBlockPlaced(){}

    /**
     *This function refreshes after we rotate or mirror a block
     *
     *
     *Preconditions:
     *-The game must be `ACTIVE`
     *-The player must have rotated or mirrored the block
     *
     *Postconditions:
     *-The player sees the block rotated or mirrored
     *
     *@param block this is the block we want to mirror or rotate
     */
    fun refreshSingleBlock(block: Block){}

    /**
    *This function ensures that we enter the GameFinishedMenuScene
     *
    *Preconditions:
    * -The game must be `ACTIVE`
    * -No player has a valid move
    *
    *Post-conditions:
    * -We are in the GameFinishedMenuScene
    *
     * @param ranking Ranking of the Players.
    */
    fun refreshAfterGameEnd(ranking : MutableList<Pair<String,Int>>){}

    /**
     *This function ensures that we switch between the players in the gui
     *
     *Preconditions:
     * -A player must have placed a block or have no valid moves left
     *
     *Post-conditions:
     * -The next player is displayed in the gui
     *
     */
    fun refreshAfterTurnEnd(){}

    /**
     *This function allows that errors get handled in the GUI
     *
     *Preconditions:
     * -An Error has occured.
     *
     *Post-conditions:
     * -Error message gets displayed
     *
     */
    fun refreshAfterError(message: String){}

    /**
     * refreshes connection status with the given parameter
     *
     * @param newState new connection state
     * */
    fun refreshConnectionState(newState: ConnectionState){}
}