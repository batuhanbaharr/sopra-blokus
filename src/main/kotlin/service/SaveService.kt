package service

import entity.Game
import entity.PlayerType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Service to help manage save states and save files.
 *
 * @param rootService [RootService] for access to entity layer and other services.
 *
 * @property rootService [RootService] for access to entity layer and other services.
 */
class SaveService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Saves the current state of the [Game] in a JSON file.
     *
     * Preconditions:
     * - The GameState has to be 'ACTIVE#
     *
     * Post-conditions:
     * - The game is saved in the JSON file
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE`
     * @throws IllegalArgumentException if the encoded input does not comply format's specification
     * @throws SerializationException in case of any encoding-specific error
     */
    fun saveGameState(slot : Int){
        //gets the most recent player turn
        val game : Game = findMostRecentPlayerTurn()

        //adds the turns to the list
        val gameList : MutableList<Game> = mutableListOf()
        gameList.add(game)
        var prevGame = game.previousGame

        while(prevGame!=null){
            gameList.add(prevGame)
            prevGame = prevGame.previousGame
        }

        //writes the game state list to a JSON type String
        val jsonSaveFile = Json.encodeToString(gameList)

        //writes the String to the SaveFile.json
        File("SaveFile_$slot.json").writeText(jsonSaveFile)
    }

    /**
     * Finds the most recent game state before the current state where a Player is active.
     * If the current game state is the first of such states, it is returned itself.
     *
     * Preconditions:
     * - The GameState has to be 'ACTIVE'
     *
     * Post-conditions:
     * - We get a state in which real player has a turn
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE`
     *
     * @return Returns a [Game] in which a player is currently active.
     */
    fun findPreviousPlayerTurn(): Game{
        //checks if currentGame is not null
        var game : Game? = rootService.currentGame
        checkNotNull(game){"Game should be initialized."}

        //sets game to previous turn as long as it is not null or a turn of a LOCAL player is found
        game = game.previousGame
        while(game!=null){
            val colorIndex = game.currentColorIndex
            val playerType = game.colors[colorIndex].players[game.colors[colorIndex].sharedPlayerIndex].playerType
            if(playerType != PlayerType.LOCAL) {
                game = game.previousGame
            }
            else{
                break
            }
        }

        //Returns the original state if no previous turn of LOCAL player is found.
        if(game == null){
            game = rootService.currentGame
        }
        //Returns the most recent playable turn.
        return game
    }

    /**
     * Loads a [Game] from a JSON file.
     *
     * Preconditions:
     * - There must be a saved game
     *
     * Post-conditions:
     * - The saved game is playable again
     *
     * @throws IllegalArgumentException when SaveFile.json does not contain the information for a [Game] object.
     */
    fun loadGameState(slot : Int){
        try {
            //loads file as MutableList of games without previous and next game
            val jsonSaveFile = File("SaveFile_$slot.json").readText()
            val gameList = Json.decodeFromString<MutableList<Game>>(jsonSaveFile)

            //restores the order of plays
            for((index, game) in gameList.withIndex()){
                if(index != 0) {
                    game.nextGame = gameList[index-1]
                    gameList[index-1].previousGame=game
                }
            }
            rootService.currentGame = gameList[0]

            //refreshes the gui
            onAllRefreshables{
                refreshAfterGameLoaded()
            }
        }
        catch (exception: IllegalArgumentException){
            println(exception)
            return
        }
    }

    /**
     * Finds the most recent game state in all [Game]s where a Player is active.
     *
     * Preconditions:
     * - The GameState has to be `ACTIVE`
     * - A real player must have taken a turn
     *
     * Post-conditions:
     * - The GameState has to be `ACTIVE`
     *
     * @throws IllegalStateException If the GameState is not `ACTIVE`
     *
     * @return Returns a [Game] in which a player is currently active.
     */
    fun findMostRecentPlayerTurn(): Game{
        //checks if current game is not null
        var game : Game? = rootService.currentGame
        checkNotNull(game){"Game should be initialized."}

        //loops through the turns until the most recent turn is found, which has null as nextGame
        while(game!=null && game.nextGame!=null){
            game = game.nextGame
        }

        //Returns the most recent turn
        checkNotNull(game){"Game should not be null in this instance"}
        return game
    }
}

