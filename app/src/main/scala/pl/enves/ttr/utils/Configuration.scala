package pl.enves.ttr.utils

import android.content.{Context, SharedPreferences}
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.R
import pl.enves.ttr.logic.networking.PlayServices
import pl.enves.ttr.utils.themes.Theme

object Configuration {
  private[this] lazy val prefs = ContextRegistry.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
  private[this] lazy val prefed: SharedPreferences.Editor = prefs.edit()

  val defaultColorThemeId: Int = R.array.theme_five

  val defaultTypefacePath: String = "fonts/Comfortaa-Regular.ttf"

  def isFirstRun = prefs.getBoolean("FIRSTRUN", true)

  def isFirstRun_=(b: Boolean) = {
    prefed.putBoolean("FIRSTRUN", b)
    prefed.commit()
  }

  def pickedTheme: Theme = {
    val defaultTheme = Theme(ContextRegistry.context.getResources, defaultColorThemeId)
    return Theme(prefs.getString("THEME", defaultTheme.toJsonObject.toString))
  }

  def pickedTheme_=(t: Theme) = {
    prefed.putString("THEME", t.toJsonObject.toString)
    prefed.commit()
  }

  def botDifficulty: Int = prefs.getInt("BOT_DIFFICULTY", 2)

  def botDifficulty_=(d: Int) = {
    prefed.putInt("BOT_DIFFICULTY", d)
    prefed.commit()
  }

  def GPS_opt_in = prefs.getBoolean("GPS_SUPPORT", true)

  def GPS_opt_in_=(b: Boolean) = {
    prefed.putBoolean("GPS_SUPPORT", b)
    prefed.commit()
  }

  def isMultiplayerAvailable: Boolean = PlayServices.isAvailable && GPS_opt_in
}
