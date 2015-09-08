package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Brown extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 199, 178, 153),
    ColorId.ring -> Color.argb(255, 199, 178, 153),
    ColorId.outer1 ->  Color.argb(255, 212, 20, 90),
    ColorId.outer2 -> Color.argb(255, 106, 10, 45),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0), //TODO
    ColorId.text -> Color.argb(255, 179, 179, 179), //TODO
    ColorId.background -> Color.argb(255, 199, 178, 153)
  )
}