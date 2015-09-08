package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Blue extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 27, 20, 100),
    ColorId.ring -> Color.argb(255, 27, 20, 100),
    ColorId.outer1 ->  Color.argb(255, 179, 179, 179),
    ColorId.outer2 -> Color.argb(255, 255, 255, 255),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0),
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0),
    ColorId.text -> Color.argb(255, 179, 179, 179),
    ColorId.background -> Color.argb(255, 27, 20, 100)
  )
}
