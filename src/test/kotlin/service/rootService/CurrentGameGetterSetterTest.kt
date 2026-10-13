package service.rootService

import service.RootService
import kotlin.test.*

/**
 * Class to check the function [service.rootService.CurrentGameGetterSetterTest]
 */
class CurrentGameGetterSetterTest {
    lateinit var rootService : RootService

    /**
     * Setups a [rootService]
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * Checks whether we get an error if there is no game
     */
    @Test
    fun testFailureCaseGetCurrentGame() {
        assertFails {
            rootService.currentGame
        }

    }

}