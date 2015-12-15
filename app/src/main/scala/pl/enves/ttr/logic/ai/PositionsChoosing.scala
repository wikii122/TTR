package pl.enves.ttr.logic.ai

import android.content.Context
import android.widget.ArrayAdapter

object PositionsChoosing extends Enumeration {
  val Reasonable, GEMedian, GEAverage, Max8, Max12, Max16 = Value

  def getAdapter(context: Context): ArrayAdapter[String] = {
    val strings = PositionsChoosing.values.map(v => v.toString).toArray
    val aa = new ArrayAdapter[String](context, android.R.layout.simple_list_item_1, strings)
    return aa
  }
}
