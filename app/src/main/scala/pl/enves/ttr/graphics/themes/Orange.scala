package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Orange extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 202, 66, 49),
    ColorId.ring -> Color.argb(255, 202, 66, 49),
    ColorId.outer1 ->  Color.argb(255, 251, 221, 216),
    ColorId.outer2 -> Color.argb(255, 255, 169, 157),
    ColorId.outerIllegal -> Color.argb(255, 179, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 179, 0), //TODO
    ColorId.text -> Color.argb(255, 179, 179, 179), //TODO
    ColorId.background -> Color.argb(255, 202, 66, 49)
  )
}