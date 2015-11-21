package pl.enves.ttr

import android.content.{Context, Intent, SharedPreferences}
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.{MenuItem, View}
import android.widget.{Button, TextView}
import pl.enves.androidx.ExtendedActivity
import pl.enves.androidx.color.ColorManip
import pl.enves.androidx.helpers._

class SettingsActivity extends ExtendedActivity with ColorManip {
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    val toolbar = find[Toolbar](R.id.settings_toolbar)
    setSupportActionBar(toolbar)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    val themesButton = find[Button](R.id.button_themes)
    themesButton onClick startThemes

    val themesPrompt = find[Button](R.id.button_themes_prompt)
    themesPrompt onClick startThemes

    val tutorialButton = find[Button](R.id.button_tutorial)
    tutorialButton onClick startTutorial

    val tutorialPrompt = find[Button](R.id.button_tutorial_prompt)
    tutorialPrompt onClick startTutorial
  }

  override def onStart() = {
    super.onStart()

    setToolbarGui()

    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    applyCustomFont("fonts/comfortaa.ttf")

    val theme = getSavedTheme(prefs.get)
    setColors(theme.background, theme.outer1, theme.outer2)
  }

  override def onPause() {
    super.onPause()
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

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val themesButton = find[Button](R.id.button_themes)
    themesButton.setTypeface(typeface)

    val themesPrompt = find[Button](R.id.button_themes_prompt)
    themesPrompt.setTypeface(typeface)

    val tutorialButton = find[Button](R.id.button_tutorial)
    tutorialButton.setTypeface(typeface)

    val tutorialPrompt = find[Button](R.id.button_tutorial_prompt)
    tutorialPrompt.setTypeface(typeface)

    val toolbar = find[Toolbar](R.id.settings_toolbar)
    for (i <- 0 until toolbar.getChildCount) {
      toolbar.getChildAt(i) match {
        case view: TextView =>
          view.setTypeface(typeface)
        case _ =>
      }
    }
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val themesButton = find[Button](R.id.button_themes)
    themesButton.setTextColor(content1)

    val themesPrompt = find[Button](R.id.button_themes_prompt)
    themesPrompt.setTextColor(content2)

    val tutorialButton = find[Button](R.id.button_tutorial)
    tutorialButton.setTextColor(content1)

    val tutorialPrompt = find[Button](R.id.button_tutorial_prompt)
    tutorialPrompt.setTextColor(content2)

    val toolbar = find[Toolbar](R.id.settings_toolbar)
    toolbar.setTitleTextColor(content1)

    toolbar.getRootView.setBackgroundColor(background)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      // Respond to the action bar's Up/Home button
      case android.R.id.home =>
        onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }
}
