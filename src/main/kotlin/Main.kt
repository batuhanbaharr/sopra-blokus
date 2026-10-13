import gui.GameApplication

/**
 * Main entry point that starts the [GameApplication]
 *
 * Once the application is closed, it prints a message indicating the end of the application.
 */
fun main() {
    GameApplication().show()
    println("Application ended. Goodbye")
}