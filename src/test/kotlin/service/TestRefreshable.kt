package service

import entity.Block

/**
 *[Refreshable] implementation that logs if a refresh has been called.
 *
 * @constructor Creates a [TestRefreshable] with given [RootService]
 */
class TestRefreshable : Refreshable {
    var refreshAfterGameLoadedCalled = false
        private set

    var refreshAfterBlockPlacedCalled = false
        private set

    var refreshSingleBlockCalled = false
        private set

    var refreshAfterGameEndCalled = false
        private set

    var refreshAfterTurnEndCalled = false
        private set

    var refreshAfterErrorCalled = false
        private set

    var rankingList : MutableList<Pair<String,Int>> = mutableListOf()

    /**
     * Resets all called properties to false
     */
    fun reset() {
        refreshAfterGameLoadedCalled = false
        refreshAfterBlockPlacedCalled = false
        refreshSingleBlockCalled = false
        refreshAfterGameEndCalled = false
        refreshAfterTurnEndCalled = false
        refreshAfterErrorCalled = false
    }

    override fun refreshAfterGameLoaded() {
        refreshAfterGameLoadedCalled = true
    }

    override fun refreshAfterBlockPlaced() {
        refreshAfterBlockPlacedCalled = true
    }

    override fun refreshSingleBlock(block: Block) {
        refreshSingleBlockCalled = true
    }

    override fun refreshAfterGameEnd(ranking: MutableList<Pair<String,Int>>) {
        rankingList = ranking
        refreshAfterGameEndCalled = true
    }

    override fun refreshAfterTurnEnd() {
        refreshAfterTurnEndCalled = true
    }

    override fun refreshAfterError(message: String) {
        refreshAfterErrorCalled = true
    }
}