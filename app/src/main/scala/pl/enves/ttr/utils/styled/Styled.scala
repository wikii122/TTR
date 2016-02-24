package pl.enves.ttr.utils.styled

import android.graphics.Typeface
import pl.enves.ttr.utils.themes.Theme

trait Styled {
  protected val fontPath: String = "fonts/comfortaa.ttf"

  protected def setTypeface(typeface: Typeface): Unit

  def setColorTheme(theme: Theme): Unit
}