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
  private val quadrants = createQuadrants.toMap

  private[this] var _combination: List[(Int, Int)] = Nil
  private var _winner: Option[Player.Value] = None
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

  def move(quadrant: Quadrant.Value, x: Int, y: Int)(implicit player: Player.Value): Boolean = {
    log(s"Move of $player at ($x, $y) in $quadrant quadrant")
    quadrants.move(quadrant, x, y, player)

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
    x => if (x < Quadrant.size)
      quadrants(Quadrant.first).line(x) ++ quadrants(Quadrant.third).line(x)
    else
      quadrants(Quadrant.second).line(x % Quadrant.size) ++ quadrants(Quadrant.fourth).line(x % Quadrant.size)
  }

  def quadrantField(quadrant: Quadrant.Value, x: Int, y: Int) = quadrants(quadrant).get(x % Quadrant.size, y % Quadrant.size)

  def quadrantRotation(quadrant: Quadrant.Value) = quadrants(quadrant).rotation

  def availableRotations = quadrants.filter(_._2.isRotable).keys

  def canRotate(quadrant: Quadrant.Value) = quadrants(quadrant).isRotable

  def sync(board: Board) = {
    freeFields = board.freeFields
    _version = board._version
    _winner = _winner

    board.quadrants foreach {
      p => val (quad, data) = p
        quadrants(quad) sync data
    }
  }

  private def createQuadrants = Quadrant.values.toList map BoardQuadrant.named

  private def checkVictory(): Boolean = VictoryConditions.check(lines) exists {
    t => val (player, fields) = t
      _winner = Option(player.orNull)
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
      map(quadrant).move(x % Quadrant.size, y % Quadrant.size, player)
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
    "winner" -> _winner,
    "quadrants" -> quadrants.toJson
  )

  def getFreeFields = freeFields
  def getQuadrant(quadrant: Quadrant.Value) = quadrants(quadrant)
}

object Board extends Logging {
  def apply() = new Board()
  def apply(jsValue: JsValue): Board = {
    val fields = jsValue.asJsObject.fields
    val board = new Board()
    board.freeFields = fields("freeFields").convertTo[Int]
    board._version = fields("version").convertTo[Int]
    board._winner = fields("winner").convertTo[Option[Player.Value]]

    fields("quadrants").asInstanceOf[JsArray].elements map (_.asJsObject.fields) foreach {
      field =>
        val quad = field("quadrant").convertTo[Quadrant.Value]
        val data = field("data").asJsObject.fields

        log(s"Recreating $quad quad from $data")

        val quadrant = board.quadrants(quad)
        quadrant.rotation = data("rotation").convertTo[Int]
        quadrant.cooldown = data("cooldown").convertTo[Int]

        val triple =
          data("fields").asInstanceOf[JsArray].elements map (_.asInstanceOf[JsArray].elements)

        for(i <- triple.indices;
            j <- triple(i).indices)
          quadrant.fields(i)(j) = triple(i)(j).convertTo[Option[Player.Value]]
    }

    board.checkVictory()

    return board
  }
}
