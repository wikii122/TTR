package pl.enves.ttr.graphics.themes

import android.graphics.Color

object Green extends Theme {
  override val colors = Map(
    ColorId.cross -> Color.argb(255, 0, 42, 39),
    ColorId.ring -> Color.argb(255, 0, 42, 39),
    ColorId.outer1 ->  Color.argb(255, 153, 221, 216),
    ColorId.outer2 -> Color.argb(255, 0, 169, 157),
    ColorId.outerIllegal -> Color.argb(255, 255, 0, 0), //TODO
    ColorId.outerWinner -> Color.argb(255, 0, 255, 0), //TODO
    ColorId.text -> Color.argb(255, 179, 179, 179), //TODO
    ColorId.background -> Color.argb(255, 0, 42, 39)
  )
}