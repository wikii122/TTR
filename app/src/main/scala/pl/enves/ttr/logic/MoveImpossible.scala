package pl.enves.ttr.logic

class MoveImpossible(msg: String) extends RuntimeException(msg)

class FieldTaken(msg: String) extends MoveImpossible(msg)

class GameFinished(msg: String) extends MoveImpossible(msg)
class GameWon(msg: String) extends MoveImpossible(msg)
class GameDrawn extends GameFinished("There is no possibility to add a symbol")
