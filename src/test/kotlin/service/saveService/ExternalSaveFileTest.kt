package service.saveService

import entity.Game
import entity.GameMode
import entity.Player
import entity.PlayerType
import entity.ScoringStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import java.io.File
import kotlin.test.assertFails
import service.*

/**
 * Class used to test [SaveService.saveGameState] and [SaveService.loadGameState]
 */
class ExternalSaveFileTest {
    lateinit var game : Game
    lateinit var rootService : RootService
    lateinit var testRefreshable: TestRefreshable
    lateinit var compGame : Game

    /**
     * Sets up a [Game] and [RootService] to test the operations on.
     */
    @BeforeTest
    fun setUp(){
        //Creating RootService
        rootService = RootService()

        //Creating list of players for the game
        val player1 = Player("Hans", PlayerType.LOCAL)
        val player2 = Player("Peter", PlayerType.LOCAL)
        val players : MutableList<Player> = mutableListOf(player1, player2)

        val compPlayer1 = Player("1", PlayerType.LOCAL)
        val compPlayer2 = Player("2", PlayerType.LOCAL)
        val compPlayers = mutableListOf(compPlayer1, compPlayer2)

        //setting up the game and saving it in RootService
        game = createGame(players)
        compGame = createGame(compPlayers)
        rootService.currentGame = game

        //initializing TestRefreshable
        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
    }

    /**
     * Test function that saves the game state, resets [RootService.currentGame] to null and loads the game back
     * from the JSON file, that is created in the SaveService.
     */
    @Test
    fun testSaveGames(){
        //Saving the currentGame in a JSON file
        rootService.saveService.saveGameState(1)

        //Resets the currentGame to comparison Game
        rootService.currentGame = compGame
        assertTrue(rootService.currentGame.colors[0].players[0].name == "1")

        //Refreshable should not be called yet
        assertFalse(testRefreshable.refreshAfterGameLoadedCalled)

        //Reloads the Game object from the JSON file
        rootService.saveService.loadGameState(1)

        //Refreshable should be called
        assertTrue(testRefreshable.refreshAfterGameLoadedCalled)

        //Reset Refreshable
        testRefreshable.reset()
        //Refreshable should not be called yet
        assertFalse(testRefreshable.refreshAfterGameLoadedCalled)

        //Tests if currentGame is loaded and saving it in a variable for easier access
        assertNotNull(rootService.currentGame)
        var loadedGame = rootService.currentGame

        //Checking if the Game is loaded correctly by comparing the Player names to the ones given in the setUp()
        assertTrue(loadedGame.colors[0].players[0].name == "Hans")
        assertTrue(loadedGame.colors[1].players[0].name == "Peter")

        //Overwriting the current game with a second game with different names.
        val player1 = Player("Mario", PlayerType.LOCAL)
        val player2 = Player("Luigi", PlayerType.LOCAL)
        val players : MutableList<Player> = mutableListOf(player1, player2)
        rootService.currentGame = createGame(players)

        //Saving, resetting and loading the game state to see if loading the game works with multiple times.
        rootService.saveService.saveGameState(1)

        //Resets the currentGame to comparison Game
        rootService.currentGame = compGame
        assertTrue(rootService.currentGame.colors[0].players[0].name == "1")

        //Loading the game state
        rootService.saveService.loadGameState(1)
        assertNotNull(rootService.currentGame)
        loadedGame = rootService.currentGame

        //Testing if the save file is now containing the new player names.
        assertTrue(loadedGame.colors[0].players[0].name == "Mario")
        assertTrue(loadedGame.colors[1].players[0].name == "Luigi")

        //Refreshable should be called
        assertTrue(testRefreshable.refreshAfterGameLoadedCalled)

        //Saving a game as the Game.nextGame variable to check if linked list type structure saves correctly
        rootService.currentGame.nextGame = game
        val nextGame = rootService.currentGame.nextGame
        checkNotNull(nextGame)
        nextGame.previousGame=rootService.currentGame

        //Saving, resetting and loading the game.
        rootService.saveService.saveGameState(1)

        //Resets the currentGame to comparison Game
        rootService.currentGame = compGame
        assertTrue(rootService.currentGame.colors[0].players[0].name == "1")

        rootService.saveService.loadGameState(1)
        loadedGame = rootService.currentGame

        //Game should have a previousGame now.
        assertTrue(loadedGame.previousGame!=null)
        //checking if nextGame has correct Players inside by comparing the name.
        assertTrue(loadedGame.colors[0].players[0].name=="Hans")
    }

    /**
     * Tests if exception is thrown when a save is getting loaded without a savefile existing
     */
    @Test
    fun testLoadWithEmptySaveFile(){
        //saving the currently loaded game. Saving the player name as comparison.
        rootService.saveService.saveGameState(2)
        val comparisonGame = rootService.currentGame
        val comparisonName = comparisonGame.colors[0].players[0].name

        //overwriting the save file
        File("SaveFile_2.json").writeText("")

        //Should not load a save file, as it is not in correct format
        rootService.saveService.loadGameState(2)

        //saving the comparison name of the currently loaded game
        val loadedComparisonGame = rootService.currentGame
        val loadedComparisonName = loadedComparisonGame.colors[0].players[0].name

        //both names should still be the same
        assertTrue(loadedComparisonName==comparisonName)

        //deleting the save file to test if it fails, when loadGameState is called
        File("SaveFile_2.json").delete()
        assertFails{rootService.saveService.loadGameState(2)}
        rootService.saveService.saveGameState(2)
    }

    /**
     * Help function to create a [Game] object.
     *
     * @param playerList List of players for the game.
     *
     * @return [Game] that gets created with the parameters and [GameMode.TWO_PLAYER_SMALL] and [ScoringStrategy.BASIC]
     */
    private fun createGame(playerList: MutableList<Player>): Game{
        val createdGame = Game(GameMode.TWO_PLAYER_SMALL, false, ScoringStrategy.BASIC)
        for((idx, player) in playerList.withIndex()){
            createdGame.colors[idx].players = mutableListOf(player)
        }
        createdGame.currentColorIndex = 0
        return createdGame
    }

}