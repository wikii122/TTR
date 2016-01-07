package pl.enves.ttr.utils

import android.content.{Context, SharedPreferences}
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.R
import pl.enves.ttr.utils.themes.Theme

object Configuration {
  private[this] val prefs = ContextRegistry.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
  private[this] val prefed: SharedPreferences.Editor = prefs.edit()

  private[this] val defaultColorThemeId: Int = R.array.theme_five

  def isPaid = ContextRegistry.context.getString(R.string.VERSION) == "PAID"

  def isFirstRun = prefs.getBoolean("FIRSTRUN", true)

  def isFirstRun_=(b: Boolean) = {
    prefed.putBoolean("FIRSTRUN", b)
    prefed.commit()
  }

  def isMultiplayerAvailable = isPaid

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
}
