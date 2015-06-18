package pl.enves.ttr.logic

/**
 * Used to mark that data depend on Board version.
 */
private[logic] class Move {
    private[this] val state = StandardGame.boardVersion

    def valid = state == StandardGame.boardVersion
}
