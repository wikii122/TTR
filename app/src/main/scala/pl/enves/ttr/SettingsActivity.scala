package pl.enves.ttr

import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.{Button, TextView}
import com.google.android.gms.ads.AdView
import pl.enves.androidx.helpers._
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.{Theme, ThemePicker}
import pl.enves.ttr.utils.{AdUtils, Configuration}

import scala.collection.mutable.ArrayBuffer

class SettingsActivity extends ToolbarActivity with AdUtils {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    setupToolbar(R.id.settings_toolbar)

    val themePicker = find[ThemePicker](R.id.view_theme_picker)
    val tutorialButton = (find[Button](R.id.button_tutorial), find[Button](R.id.button_tutorial_prompt))
    val aboutButton = (find[Button](R.id.button_about), find[Button](R.id.button_about_prompt))
    val licensesButton = (find[Button](R.id.button_licenses), find[Button](R.id.button_licenses_prompt))

    themePicker.setChangeListener(this)
    val themes = readDefaultThemes
    themePicker.setThemes(themes)
    var i = themes.indexOf(Configuration.pickedTheme)
    if (i == -1) {
      i = 0
    }
    themePicker.setCurrent(i)

    tutorialButton onClick startTutorial
    aboutButton onClick startAbout
    licensesButton onClick startLicenses

    val adView = find[AdView](R.id.ad_view_settings)
    loadAd(adView)
  }

  override def onResume(): Unit = {
    super.onResume()

    val adView = find[AdView](R.id.ad_view_settings)
    adView.resume()
  }

  override def onPause(): Unit = {
    super.onPause()

    val themePicker = find[ThemePicker](R.id.view_theme_picker)

    if (themePicker.isChanged) {
      Configuration.pickedTheme = themePicker.getTheme
    }

    val adView = find[AdView](R.id.ad_view_settings)
    adView.pause()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()

    val adView = find[AdView](R.id.ad_view_settings)
    adView.destroy()
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

  private[this] def startAbout(v: View) = {
    log("Intending to start about")
    val itnt = intent[AboutActivity]
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

    val pickThemeText = (find[TextView](R.id.text_pick_theme), find[TextView](R.id.text_pick_theme_prompt))
    val tutorialButton = (find[Button](R.id.button_tutorial), find[Button](R.id.button_tutorial_prompt))
    val aboutButton = (find[Button](R.id.button_about), find[Button](R.id.button_about_prompt))
    val licensesButton = (find[Button](R.id.button_licenses), find[Button](R.id.button_licenses_prompt))

    pickThemeText._1.setTypeface(typeface)
    pickThemeText._2.setTypeface(typeface)
    tutorialButton.setTypeface(typeface)
    aboutButton.setTypeface(typeface)
    licensesButton.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    val pickThemeText = (find[TextView](R.id.text_pick_theme), find[TextView](R.id.text_pick_theme_prompt))
    val tutorialButton = (find[Button](R.id.button_tutorial), find[Button](R.id.button_tutorial_prompt))
    val aboutButton = (find[Button](R.id.button_about), find[Button](R.id.button_about_prompt))
    val licensesButton = (find[Button](R.id.button_licenses), find[Button](R.id.button_licenses_prompt))

    pickThemeText._1.setTextColor(theme.color1)
    pickThemeText._2.setTextColor(theme.color2)
    tutorialButton.setTextColor(theme.color1, theme.color2)
    aboutButton.setTextColor(theme.color1, theme.color2)
    licensesButton.setTextColor(theme.color1, theme.color2)
  }
}
