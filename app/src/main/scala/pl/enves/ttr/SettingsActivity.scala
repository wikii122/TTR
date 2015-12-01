package pl.enves.ttr

import android.content.{Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.{Button, ImageButton, TextView}
import pl.enves.androidx.helpers._
import pl.enves.ttr.logic.Player
import pl.enves.ttr.utils.SymbolButton
import pl.enves.ttr.utils.styled.ToolbarActivity
import pl.enves.ttr.utils.themes.Theme

class SettingsActivity extends ToolbarActivity {
  private[this] var themesButton: Option[(Button, Button)] = None
  private[this] var tutorialButton: Option[(Button, Button)] = None
  private[this] var licensesButton: Option[(Button, Button)] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    setupToolbar(R.id.settings_toolbar)

    themesButton = Some((find[Button](R.id.button_themes), find[Button](R.id.button_themes_prompt)))
    tutorialButton = Some((find[Button](R.id.button_tutorial), find[Button](R.id.button_tutorial_prompt)))
    licensesButton = Some((find[Button](R.id.button_licenses), find[Button](R.id.button_licenses_prompt)))

    themesButton.get onClick startThemes
    tutorialButton.get onClick startTutorial
    licensesButton.get onClick startLicenses
  }

  private[this] def startTutorial(v: View) = {
    log("Intending to start tutorial")
    val itnt = intent[TutorialActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  private[this] def startThemes(v: View) = {
    log("Intending to start themes")
    val itnt = intent[ThemesActivity]
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

    themesButton.get.setTypeface(typeface)
    tutorialButton.get.setTypeface(typeface)
    licensesButton.get.setTypeface(typeface)
  }

  override def setColorTheme(theme: Theme): Unit = {
    super.setColorTheme(theme)

    themesButton.get.setTextColor(theme.color1, theme.color2)
    tutorialButton.get.setTextColor(theme.color1, theme.color2)
    licensesButton.get.setTextColor(theme.color1, theme.color2)
  }
}
