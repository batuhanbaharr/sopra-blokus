package service

import entity.ColorType
import entity.Game
import entity.GameMode
import entity.Player
import entity.PlayerType
import entity.ScoringStrategy
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test


/**
 * Test class for [SaveService] functions that work without external save files.
 */
class SaveServiceTest {

    lateinit var game : Game
    lateinit var rootService : RootService
    lateinit var saveService : SaveService

    /**
     * Sets up game with 4 players and 2 rounds.
     * Rounds differ by [Game.currentPlayerIndex], which is set in a help function.
     */
    @BeforeTest
    fun setUp(){
        //Setting up RootService and SaveService
        rootService = RootService()
        saveService = rootService.saveService

        //Creating a player list with 2 players and 2 bots
        val player1 = Player("P1", PlayerType.LOCAL)
        val player2 = Player("P2", PlayerType.LOCAL)
        val bot1 = Player("B1", PlayerType.BOTEASY)
        val bot2 = Player("B2", PlayerType.BOTHARD)
        val playerList = mutableListOf(bot1, player1, bot2, player2)

        //creating the game
        rootService.currentGame = createGame(playerList, 0)
        game = rootService.currentGame

        //adding player turns 2-4 to the game
        addGame(game, createGame(playerList, 1))
        addGame(game, createGame(playerList, 2))
        addGame(game, createGame(playerList, 3))

        //Adding a second round
        addGame(game, createGame(playerList, 0))
        addGame(game, createGame(playerList, 1))
        addGame(game, createGame(playerList, 2))
        addGame(game, createGame(playerList, 3))
    }


    /**
     * Tests if [SaveService.findPreviousPlayerTurn] and [SaveService.findMostRecentPlayerTurn] work as intended.
     */
    @Test
    fun testFindPlayers(){
        //First player should be B1
        assertTrue(game.colors[game.currentColorIndex].players[0].name=="B1", "B1 should start the game")

        //Going to the last turn in the game order.
        game = saveService.findMostRecentPlayerTurn()
        rootService.currentGame = game

        //Current Player should be P2
        assertTrue(game.colors[game.currentColorIndex].players[0].name == "P2")

        //Going to the turn before the current one.
        game = saveService.findPreviousPlayerTurn()
        rootService.currentGame = game

        //Current Player should be P1
        assertTrue(game.colors[game.currentColorIndex].players[0].name == "P1")

        //Going back one turn in the game order.
        game = saveService.findPreviousPlayerTurn()
        rootService.currentGame = game

        //Current Player should be P2
        assertTrue(game.colors[game.currentColorIndex].players[0].name == "P2")

        //Going back one turn in the game order.
        game = saveService.findPreviousPlayerTurn()
        rootService.currentGame = game

        //Current Player should be P1
        assertTrue(game.colors[game.currentColorIndex].players[0].name == "P1")

        //Trying to go back another turn in the game order.
        game = saveService.findPreviousPlayerTurn()
        rootService.currentGame = game

        //Current Player should be P1 as this is the first player turn.
        assertTrue(game.colors[game.currentColorIndex].players[0].name == "P1")
    }


    /**
     * Test function to test if three player mode gets called correctly
     */
    @Test
    fun testWithThreePlayers(){
        //Setting up player with 1 bot and 2 players
        val b1 = Player("b1", PlayerType.BOTEASY)
        val p1 = Player("p1", PlayerType.LOCAL)
        val p2 = Player("p2", PlayerType.LOCAL)
        val playerList = mutableListOf(b1, p1, p2)

        //Creating the first round
        var game = createThreePlayerGame(playerList, 0, 1)
        addGame(game, createThreePlayerGame(playerList, 1, 1))
        addGame(game, createThreePlayerGame(playerList, 2, 1))
        addGame(game, createThreePlayerGame(playerList, 3, 1))

        //Creating the first two turns of the second round
        addGame(game, createThreePlayerGame(playerList, 0, 2))
        addGame(game, createThreePlayerGame(playerList, 1, 2))

        //setting game to most recent turn
        var nextGame = game.nextGame
        while(nextGame!=null){
            game = nextGame
            nextGame = game.nextGame
        }

        //State 1: 2nd round, turn 2. p1 is currently playing
        rootService.currentGame = game
        assertTrue {getName(rootService.currentGame)=="p1"}

        //State 2: 1st round, turn 4. Shared color is controlled by p1.
        rootService.currentGame = rootService.saveService.findPreviousPlayerTurn()
        assertTrue { getName(rootService.currentGame)=="p1" }

        //State 3: 1st round, turn 3. p2 is currently playing.
        rootService.currentGame = rootService.saveService.findPreviousPlayerTurn()
        assertTrue { getName(rootService.currentGame)=="p2" }

    }


    /**
     * Help function to create a [Game] object.
     *
     * @param playerList List of players for the game.
     * @param index Sets [Game.currentColorIndex].
     *
     * @return [Game] that gets created with the parameters and [GameMode.FOUR_PLAYER] and [ScoringStrategy.BASIC]
     */
    private fun createGame(playerList: MutableList<Player>, index: Int): Game{
        val createdGame = Game(GameMode.FOUR_PLAYER, false, ScoringStrategy.BASIC)

        createdGame.colors.clear()

        val colorTypes = listOf(
            ColorType.BLUE,
            ColorType.YELLOW,
            ColorType.RED,
            ColorType.GREEN
        )

        for ((idx, player) in playerList.withIndex()) {
            val color = entity.Color(colorTypes[idx])
            color.players = mutableListOf(player)
            createdGame.colors.add(color)
        }

        createdGame.currentColorIndex = index
        return createdGame
    }

    /**
     * Help function to create a [Game] object with 3 players.
     *
     * @param playerList List of 3 players for the game.
     * @param index Sets [Game.currentColorIndex].
     * @param sharedIdx Sets [entity.Color.sharedPlayerIndex]
     *
     * @return [Game] that gets created with the parameters and [GameMode.FOUR_PLAYER] and [ScoringStrategy.BASIC]
     */
    private fun createThreePlayerGame(playerList: MutableList<Player>, index: Int, sharedIdx : Int): Game{
        val createdGame = Game(GameMode.FOUR_PLAYER, false, ScoringStrategy.BASIC)

        createdGame.colors.clear()

        val colorTypes = listOf(
            ColorType.BLUE,
            ColorType.YELLOW,
            ColorType.RED,
            ColorType.GREEN
        )

        for ((idx, player) in playerList.withIndex()) {
            val color = entity.Color(colorTypes[idx])
            color.players = mutableListOf(player)
            createdGame.colors.add(color)
        }

        val sharedColor = entity.Color(colorTypes[3])
        sharedColor.players = playerList
        sharedColor.sharedPlayerIndex = sharedIdx
        createdGame.colors.add(sharedColor)

        createdGame.currentColorIndex = index
        return createdGame
    }


    /**
     * Help function to add a [Game] to the linked list of [Game]s saved in [RootService.currentGame]
     *
     * @param game [Game] of which the new game should be added to
     * @param gameToAdd [Game] which gets added to the list as the most recent turn.
     */
    private fun addGame(game:Game, gameToAdd:Game){
        //saves the current state
        var currentState = game

        //sets current state to the game saved in nextGame until nextGame is null.
        while(currentState.nextGame!=null){
            val nextGame = currentState.nextGame
            checkNotNull(nextGame)
            currentState = nextGame
        }

        //adds the new game to nextGame.
        currentState.nextGame = gameToAdd
        val nextGame = currentState.nextGame
        checkNotNull(nextGame)
        //sets the previousGame of the new game to the currentState
        nextGame.previousGame = currentState
    }

    /**
     * Help function to determine the name of the current player
     *
     * @param game [Game] of the current player.
     *
     * @return Name of the player as a String
     */
    private fun getName(game: Game) : String{
        val color = game.colors[game.currentColorIndex]
        val player = color.players[color.sharedPlayerIndex]
        return player.name
    }
}