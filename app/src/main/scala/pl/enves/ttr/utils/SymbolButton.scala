package pl.enves.ttr.utils

import android.content.Context
import android.view.View
import android.widget.ImageButton
import pl.enves.ttr.R
import pl.enves.ttr.logic.Player
import pl.enves.ttr.utils.themes.ThemedImageButton


class SymbolButton(context: Context, button: ImageButton) extends ThemedImageButton(context, button) {
  private var symbol = Player.X

  onClick(changeSymbol)

  def getSymbol: Player.Value = symbol

  def setSymbol(s: Player.Value): Unit = {
    symbol = s
    updateImage()
  }

  override def getImgRes = if (symbol == Player.X) R.drawable.pat_cross_mod_mask else R.drawable.pat_ring_mod_mask

  private[this] def changeSymbol(v: View): Unit = {
    val s = if (symbol == Player.X) Player.O else Player.X
    setSymbol(s)
    updateImage()
  }
}
