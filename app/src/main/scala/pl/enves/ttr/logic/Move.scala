package pl.enves.ttr.logic

/**
 * Used to mark that data depend on Board version.
 */
private[logic] class Move {
    private[this] val state = Game.boardVersion

    def valid = state == Game.boardVersion
}
