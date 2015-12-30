package pl.enves.ttr.logic

/**
 * Without validity check
 */
class Move

/**
 * Class used to pass board position.
 */
case class Position(x: Int, y: Int) extends Move

/**
 * Used to represent rotation move information.
 *
 * Invalidates after any change in the board layout, such as rotation.
 * @param board in form of
 *              1 2
 *              3 4
 * @param r in rotation enumerator counted in degrees clockwise.
 */
case class Rotation(board: Quadrant.Value, r: QRotation.Value) extends Move
