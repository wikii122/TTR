package pl.enves.ttr.logic
package inner

import pl.enves.androidx.Logging

/**
 * Used to determine if game is finished.
 */
private[inner] object VictoryConditions extends Logging {
  type WinnerData = (Option[Player.Value], List[(Int, Int)])

  private val indexes = for (x <- 0 until 5) yield x

  def check(board: Game#State): Option[WinnerData] = {
    val result = (checkHorizontal(board) ++ checkVertical(board) ++ checkDiagonal(board) ++ Nil).flatten
    log(s"Found ${result.length} winning combinations")
    if (result.isEmpty) None
    else {
      // there are winning combinations
      val winner = if (result.count(_._1.get == Player.X) == 0) Some(Player.O)  // none of them belong to X
      else if(result.count(_._1.get == Player.O) == 0) Some(Player.X) // none of them belong to O
      else None // some of them belong to X, others to O

      // combine all winning combinations
      val list = result.flatMap(_._2).distinct.toList
      Some(winner, list)
    }
  }

  private def checkHorizontal(board: Game#State): Seq[Option[WinnerData]] = for (x <- 0 until 2; y <- 0 until 6)
    yield checkRow(board, x, y)

  private def checkVertical(board: Game#State): Seq[Option[WinnerData]] = for (x <- 0 until 6; y <- 0 until 2)
    yield checkColumn(board, x, y)

  private def checkDiagonal(board: Game#State): Seq[Option[WinnerData]] = {
    val normal = for (x <- 0 until 2; y <- 0 until 2)
      yield checkNormalDiagonal(board, x, y)
    val reverse = for (x <- 0 until 2; y <- 0 until 2)
      yield checkReverseDiagonal(board, 5-x, y)
    return normal ++ reverse
  }

  private def checkRow(board: Game#State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x+i)(y))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((Some(res), (indexes map {i => (x+i, y)}).toList))
  }

  private def checkColumn(board: Game#State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x)(y+i))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((Some(res), (indexes map {i => (x, y+i)}).toList))
  }

  private def checkNormalDiagonal(board: Game#State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x+i)(y+i))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((Some(res), (indexes map {i => (x+i, y+i)}).toList))
  }

  private def checkReverseDiagonal(board: Game#State, x: Int, y: Int): Option[WinnerData] = {
    val seq = indexes map (i => board(x-i)(y+i))
    val res = checkSeq(seq) getOrElse { return None }

    return Some((Some(res), (indexes map {i => (x-i, y+i)}).toList))
  }

  private def checkSeq(seq: Seq[Option[Player.Value]]): Option[Player.Value] = {
    if ((seq.flatten count (_ == Player.X)) == 5) Some(Player.X)
    else if ((seq.flatten count (_ == Player.O)) == 5) Some(Player.O)
    else None
  }
}
