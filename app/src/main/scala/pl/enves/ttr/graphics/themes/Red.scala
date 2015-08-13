package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Red extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 193, 39, 45),
    ColorId.ring -> Color.argb(255, 193, 39, 45),
    ColorId.outer1 ->  Color.argb(255, 230, 230, 230),
    ColorId.outer2 -> Color.argb(255, 51, 51, 51),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0),  //TODO
    ColorId.inactive -> Color.argb(255, 0, 0, 0), //TODO
    ColorId.text -> Color.argb(255, 230, 230, 230),  //TODO
    ColorId.background -> Color.argb(255, 193, 39, 45)
  )
}
