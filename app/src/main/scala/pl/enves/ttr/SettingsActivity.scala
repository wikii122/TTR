package pl.enves.ttr

import android.content.{Context, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import pl.enves.androidx.ExtendedActivity
import pl.enves.ttr.utils.themes.ThemePicker

class SettingsActivity extends ExtendedActivity {
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onStart() = {
    super.onStart()

    setGui()

    setPreviousTheme()
  }

  override def onPause() {
    super.onPause()

    val themePicker = find[ThemePicker](R.id.theme_picker)
    if (themePicker.hasChanged) {
      val ed: SharedPreferences.Editor = prefs.get.edit()
      ed.putString("THEME", getPickedTheme)
      ed.commit()
    }
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val settingsText = find[TextView](R.id.settings_title)
    settingsText.setTypeface(typeface)

    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTypeface(typeface)
  }

  private[this] def getPickedTheme: String = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    return themePicker.getCurrentJSON
  }

  private[this] def setPreviousTheme() = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    themePicker.setColorChanger(setColors)
    themePicker.setCurrentFromJSON(prefs.get.getString("THEME", themePicker.getDefaultJSON))
  }

  def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val settingsText = find[TextView](R.id.settings_title)
    settingsText.setTextColor(content1)

    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTextColor(content1)

    pickThemeText.getRootView.setBackgroundColor(background)
  }
}
