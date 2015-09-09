package pl.enves.ttr.graphics.themes

import android.graphics.Color

object ColorId extends Enumeration {
  type ColorId = Value
  val cross, ring, outer1, outer2, outerWinner, outerIllegal, inactive, text, background = Value
}

trait Theme {
  val colors: Map[ColorId.Value, Int]

  def rgb(colorId: ColorId.Value): Array[Float] = {
    val c = colors(colorId)
    return Array(Color.red(c)/255.0f, Color.green(c)/255.0f, Color.blue(c)/255.0f)
  }
  
  def rgba(colorId: ColorId.Value): Array[Float] = {
    val c = colors(colorId)
    return Array(Color.red(c)/255.0f, Color.green(c)/255.0f, Color.blue(c)/255.0f, Color.alpha(c)/255.0f)
  }

  def android(colorId: ColorId.Value): Int = colors(colorId)
}
