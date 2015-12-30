package pl.enves.ttr

import android.content.res.TypedArray
import android.content.{Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.{Button, TextView}
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.{Theme, ThemePicker}

import scala.collection.mutable.ArrayBuffer

class SettingsActivity extends ToolbarActivity {
  private[this] var pickThemeText: Option[TextView] = None
  private[this] var themePicker: Option[ThemePicker] = None
  private[this] var tutorialButton: Option[(Button, Button)] = None
  private[this] var licensesButton: Option[(Button, Button)] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    setupToolbar(R.id.settings_toolbar)

    pickThemeText = Some(find[TextView](R.id.text_pick_theme))
    themePicker = Some(find[ThemePicker](R.id.view_theme_picker))
    tutorialButton = Some((find[Button](R.id.button_tutorial), find[Button](R.id.button_tutorial_prompt)))
    licensesButton = Some((find[Button](R.id.button_licenses), find[Button](R.id.button_licenses_prompt)))

    themePicker.get.setChangeListener(this)
    val themes = readDefaultThemes
    themePicker.get.setThemes(themes)
    var i = themes.indexOf(getSavedTheme(getResources, prefs.get))
    if (i == -1) {
      i = 0
    }
    themePicker.get.setCurrent(i)

    tutorialButton.get onClick startTutorial
    licensesButton.get onClick startLicenses
  }

  override def onPause(): Unit = {
    super.onPause()
    if (themePicker.get.isChanged) {
      val ed: SharedPreferences.Editor = prefs.get.edit()
      ed.putString("THEME", themePicker.get.getTheme.toJsonObject.toString)
      ed.commit()
    }
  }

  private def readDefaultThemes: Array[Theme] = {
    val themes = new ArrayBuffer[Theme]()
    val resources = getResources
    val themeArrays: TypedArray = resources.obtainTypedArray(R.array.themes)
    for (i <- 0 until themeArrays.length) {
      themes.append(Theme(resources, themeArrays.getResourceId(i, -1)))
    }
    themeArrays.recycle()
    return themes.toArray
  }

  private[this] def startTutorial(v: View) = {
    log("Intending to start tutorial")
    val itnt = intent[TutorialActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  private[this] def startLicenses(v: View) = {
    log("Intending to start licenses")
    val itnt = intent[LicensesActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  override def setTypeface(typeface: Typeface): Unit = {
    super.setTypeface(typeface)

    pickThemeText.get.setTypeface(typeface)
    tutorialButton.get.setTypeface(typeface)
    licensesButton.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    pickThemeText.get.setTextColor(theme.color1)
    tutorialButton.get.setTextColor(theme.color1, theme.color2)
    licensesButton.get.setTextColor(theme.color1, theme.color2)
  }
}
