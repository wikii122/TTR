package pl.enves.ttr.graphics.themes

import android.content.res.Resources
import pl.enves.ttr.R
import pl.enves.ttr.graphics.ColorTypes.ColorAndroid
import pl.enves.ttr.graphics.themes.ThemeId.ThemeId

case class Theme(
                  background: ColorAndroid,
                  outer1: ColorAndroid,
                  outer2: ColorAndroid,
                  text: ColorAndroid,
                  winner: ColorAndroid
                  )

object Theme {
  private val names = Map(
    ThemeId.Blue -> R.array.theme_blue,
    ThemeId.Brown -> R.array.theme_brown,
    ThemeId.Green -> R.array.theme_green,
    ThemeId.Orange -> R.array.theme_orange,
    ThemeId.Pink -> R.array.theme_pink,
    ThemeId.Red -> R.array.theme_red,
    ThemeId.White -> R.array.theme_white
  )

  def apply(resources: Resources, arrayName: Int): Theme = {
    val themeArray: Array[Int] = resources.getIntArray(arrayName)
    return new Theme(
      themeArray(0),
      themeArray(1),
      themeArray(2),
      themeArray(3),
      themeArray(4)
    )
  }

  def apply(resources: Resources, themeId: ThemeId): Theme = apply(resources, names(themeId))

  def all(resources: Resources): Map[ThemeId, Theme] = names.mapValues[Theme](arrayName => apply(resources, arrayName))
}