package pl.enves.ttr.graphics.themes

import pl.enves.ttr.graphics.themes.ThemeId.ThemeId

object Themes {
  val byName = Map (
    ThemeId.Blue -> Blue,
    ThemeId.Brown -> Brown,
    ThemeId.First -> First,
    ThemeId.Green -> Green,
    ThemeId.Orange -> Orange,
    ThemeId.Pink -> Pink,
    ThemeId.Red -> Red,
    ThemeId.Second -> Second,
    ThemeId.White -> White
  )
  def apply(themeId: ThemeId): Theme = byName(themeId)
}
