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
import pl.enves.ttr.utils.themes.ThemePicker

class SettingsActivity extends ExtendedActivity with ColorManip {
  private[this] var prefs: Option[SharedPreferences] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_layout)

    val toolbar = find[Toolbar](R.id.settings_toolbar)
    setSupportActionBar(toolbar)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))

    val tutorialButton = find[Button](R.id.button_tutorial)
    tutorialButton onClick startTutorial

    val tutorialPrompt = find[Button](R.id.button_tutorial_prompt)
    tutorialPrompt onClick startTutorial

    applyCustomFont("fonts/comfortaa.ttf")
  }

  override def onStart() = {
    super.onStart()

    setToolbarGui()

    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

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

  private[this] def startTutorial(v: View) = {
    log("Intending to start tutorial")
    val itnt = intent[TutorialActivity]
    itnt addFlags Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    itnt start()
  }

  private[this] def applyCustomFont(path: String): Unit = {
    val typeface: Typeface = Typeface.createFromAsset(getAssets, path)

    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTypeface(typeface)

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

  private[this] def getPickedTheme: String = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    return themePicker.getCurrentJSON
  }

  private[this] def setPreviousTheme() = {
    val themePicker = find[ThemePicker](R.id.theme_picker)
    themePicker.setColorChanger(setColors)
    themePicker.setCurrentFromJSON(prefs.get.getString("THEME", themePicker.getDefaultJSON))
  }

  private[this] def setColors(background: Int, content1: Int, content2: Int): Unit = {
    val pickThemeText = find[TextView](R.id.text_pick_theme)
    pickThemeText.setTextColor(content1)

    val tutorialButton = find[Button](R.id.button_tutorial)
    tutorialButton.setTextColor(content1)

    val tutorialPrompt = find[Button](R.id.button_tutorial_prompt)
    tutorialPrompt.setTextColor(content2)

    val toolbar = find[Toolbar](R.id.settings_toolbar)
    toolbar.setTitleTextColor(content1)
    //toolbar.setBackgroundColor(color)

    pickThemeText.getRootView.setBackgroundColor(background)
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
