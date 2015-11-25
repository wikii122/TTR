package pl.enves.ttr.utils.styled

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import pl.enves.ttr.R
import pl.enves.ttr.utils.themes.Theme

trait Styled {
  protected val fontPath: String = "fonts/comfortaa.ttf"
  protected val defaultColorThemeId: Int = R.array.theme_five

  protected def setTypeface(typeface: Typeface): Unit

  protected def setColorTheme(theme: Theme): Unit

  protected def getSavedTheme(resources: Resources, prefs: SharedPreferences): Theme = {
    val defaultTheme = Theme(resources, defaultColorThemeId)
    return Theme(prefs.getString("THEME", defaultTheme.toJsonObject.toString))
  }
}