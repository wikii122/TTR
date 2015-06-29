package pl.enves.ttr.logic

class MoveImpossible(msg: String) extends RuntimeException(msg)

class FieldTaken(msg: String) extends MoveImpossible(msg)
class RotationLocked(msg: String) extends MoveImpossible(msg)

class BoardLocked extends MoveImpossible("The user is cannot change the board at the moment.")

class GameFinished(msg: String) extends MoveImpossible(msg)
class GameWon(msg: String) extends MoveImpossible(msg)
class GameDrawn extends GameFinished("Game has been drawn")
