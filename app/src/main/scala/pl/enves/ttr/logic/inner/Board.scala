package pl.enves.ttr.logic
package inner

import pl.enves.androidx.Logging
import pl.enves.ttr.utils.JsonMappable
import pl.enves.ttr.utils.JsonProtocol._
import spray.json._

/**
 * Manages fields states.
 */
private[logic] class Board private () extends Logging with JsonMappable {
  private[this] var _winner: Option[Player.Value] = None
  private[this] var _combination: List[(Int, Int)] = Nil

  private val quadrants = createQuadrants.toMap
  private var _version = 0
  private var freeFields = 36

  def version: Int = _version

  def move(x: Int, y: Int)(implicit player: Player.Value): Boolean = {
    if ((freeFields == 0) && _winner.isEmpty) throw new GameDrawn

    // TODO automate this
    val quad = if (y < Quadrant.size) {
      if (x < Quadrant.size) Quadrant.first
      else Quadrant.second
    } else {
      if (x < Quadrant.size) Quadrant.third
      else Quadrant.fourth
    }

    log(s"Move of $player at ($x, $y) in $quad quadrant")
    quadrants.move(quad, x, y, player)

    _version += 1
    freeFields -= 1

    return checkVictory()
  }

  def rotate(quadrant: Quadrant.Value, rotation: QRotation.Value)(implicit player: Player.Value): Boolean = {
    log(s"Rotation for $quadrant by $rotation for player $player")
    quadrants.rotate(quadrant, rotation)


    _version += 1

    return checkVictory()
  }

  def winner = _winner

  def finished = _winner.isDefined || (freeFields == 0)

  def finishingMove = _combination

  def lines: Game#State = (0 to 5) map {
    i => if (i < Quadrant.size)
      quadrants(Quadrant.first).line(i) ++ quadrants(Quadrant.second).line(i)
    else
      quadrants(Quadrant.third).line(i % Quadrant.size) ++ quadrants(Quadrant.fourth).line(i % Quadrant.size)
  }

  def quadrantState(quadrant: Quadrant.Value) = quadrants(quadrant).state

  def availableRotations = quadrants filter (_._2.canRotate) keys

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named

  private def checkVictory(): Boolean = VictoryConditions.check(lines) exists {
    t => val (player, fields) = t
      _winner = Option(player orNull)
      _combination = fields
      freeFields = 0

      log(s"Game finished! $player won on $fields")
      true
  }

  private implicit class QuadrantManager(map: Map[Quadrant.Value, BoardQuadrant]) {
    private def tick() = map foreach (_._2.tickCooldown())

    def rotate(quadrant: Quadrant.Value, rotation: QRotation.Value) = {
      map(quadrant).rotate(rotation)
      tick()
    }

    def move(quadrant: Quadrant.Value, x: Int, y: Int, player: Player.Value) = {
      map(quadrant).move(x, y, player)
      tick()
    }

    def toJson: JsValue = (map.toList map {
      p: (Quadrant.Value, BoardQuadrant) => val (k, v) = p
        JsObject(
          "quadrant" -> k.toJson,
          "data" -> v.toJson
        )
    }).toJson
  }

  override def toMap: Map[String, Any] = Map(
    "freeFields" -> freeFields,
    "version" -> _version,
    "quadrants" -> quadrants.toJson
  )
}

object Board {
  def apply() = new Board()
  def apply(jsValue: JsValue): Board = {
    val fields = jsValue.asJsObject.fields
    val board = new Board()
    board.freeFields = fields("freeFields").convertTo[Int]
    board._version = fields("version").convertTo[Int]

    val quadrants = fields("quadrants").asInstanceOf[JsArray].elements map (_.asJsObject.fields) map {
      field =>
        field("quadrant").convertTo[Quadrant.Value] ->
        field("data").asJsObject.fields
    }

    quadrants foreach {
      p => val (quad, data) = p
        val quadrant = board.quadrants(quad)
        quadrant setRotation data("rotation").convertTo[Int]
        quadrant setCooldown data("cooldown").convertTo[Int]
        val triple = data("fields").asInstanceOf[JsArray].elements map (_.asInstanceOf[JsArray].elements)
        for (i <- 0 until triple.length;
          j <- 0 until triple(i).length
        ) quadrant.fields(i)(j) = triple(i)(j).convertTo[Option[Player.Value]]
    }

    return board
  }
}
