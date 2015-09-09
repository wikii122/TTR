package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Second extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 0, 113, 188),
    ColorId.ring -> Color.argb(255, 237, 28, 36),
    ColorId.outer1 ->  Color.argb(255, 39, 58, 120),
    ColorId.outer2 -> Color.argb(255, 174, 212, 217),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0), //TODO
    ColorId.inactive -> Color.argb(255, 55, 55, 55), //TODO
    ColorId.text -> Color.argb(255, 179, 179, 179), //TODO
    ColorId.background -> Color.argb(255, 255, 255, 255)
  )
}