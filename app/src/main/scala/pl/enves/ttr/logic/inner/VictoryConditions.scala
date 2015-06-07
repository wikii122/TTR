package pl.enves.ttr.logic.inner

import pl.enves.ttr.logic.Game.State
import pl.enves.ttr.logic.{Game, Player}

/**
 * Used to determine if game is finished.
 */
private[inner] object VictoryConditions {
  type WinnerData = (Player.Value, List[(Int, Int)])

  private val indexes = for (x <- 0 until 5) yield x

  def check(board: Game.State): Option[WinnerData] =
    checkHorizontal(board).flatten.headOption orElse
      checkVertical(board).flatten.headOption orElse
      checkDiagonal(board).flatten.headOption

  private def checkHorizontal(board: Game.State): Seq[Option[WinnerData]] = for (x <- 0 to 1; y <- 0 to 5)
    yield checkRow(board, x, y)

  private def checkVertical(board: Game.State): Seq[Option[WinnerData]] = for (x <- 0 to 5; y <- 0 to 1)
    yield checkColumn(board, x, y)

  private def checkDiagonal(board: Game.State): Seq[Option[WinnerData]] = for (x <- 0 to 1; y <- 0 to 1)
    yield checkNormalDiagonal(board, x, y) orElse checkReverseDiagonal(board, 5-x, y)

  private def checkRow(board: State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x+i)(y))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((res, (indexes map {i => (x+i, y)}).toList))
  }

  private def checkColumn(board: State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x)(y+i))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((res, (indexes map {i => (x, y+i)}).toList))
  }

  private def checkNormalDiagonal(board: State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x+i)(y+1))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((res, (indexes map {i => (x+i, y+i)}).toList))
  }

  private def checkReverseDiagonal(board: State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x-i)(y+i))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((res, (indexes map {i => (x-i, y+i)}).toList))
  }

  private def checkSeq(seq: Seq[Option[Player.Value]]): Option[Player.Value] =
    if ((seq count (_ == Player.X)) == 5) Some(Player.X)
    else if ((seq count (_ == Player.O)) == 5) Some(Player.O)
    else None
}