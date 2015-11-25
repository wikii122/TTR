package pl.enves.ttr.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageButton
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.DrawableManip
import pl.enves.androidx.helpers._
import pl.enves.ttr.R
import pl.enves.ttr.logic.Player
import pl.enves.ttr.utils.themes.Theme

class SymbolButton(context: Context, button: ImageButton) extends DrawableManip {
  private var symbol = Player.X
  private var theme: Option[Theme] = None
  button onClick changeSymbol

  def getSymbol: Player.Value = symbol

  def setSymbol(s: Player.Value): Unit = {
    symbol = s
    updateImage()
  }

  def setColorTheme(t: Theme): Unit = {
    theme = Some(t)
    updateImage()
  }

  private[this] def updateImage(): Unit = {
    val imgRes = if (symbol == Player.X) R.drawable.pat_cross_mod_mask else R.drawable.pat_ring_mod_mask
    val res = context.getResources
    val drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, imgRes))
    drawable.setAntiAlias(true)

    maskColors(theme.get.background, theme.get.background, theme.get.color1, drawable)
    button.setBackground(drawable)
  }

  private[this] def changeSymbol(v: View): Unit = {
    val s = if (symbol == Player.X) Player.O else Player.X
    setSymbol(s)
    updateImage()
  }
}
