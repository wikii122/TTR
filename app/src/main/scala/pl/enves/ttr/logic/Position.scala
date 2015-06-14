package pl.enves.ttr.logic

/**
 * Class used to pass board position.
 *
 * Invalidates after any change in the board layout, such as rotation.
 */
case class Position(x: Int, y: Int) extends Move
