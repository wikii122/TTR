package pl.enves.ttr.graphics.themes

import android.graphics.Color

object First extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 255, 255, 255),
    ColorId.ring -> Color.argb(255, 255, 255, 255),
    ColorId.outer1 ->  Color.argb(255, 188, 126, 156),
    ColorId.outer2 -> Color.argb(255, 147, 124, 143),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0), //TODO
    ColorId.text -> Color.argb(255, 179, 179, 179), //TODO
    ColorId.background -> Color.argb(255, 255, 255, 255)
  )
}