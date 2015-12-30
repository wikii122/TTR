package pl.enves.ttr.utils.themes

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageButton
import pl.enves.androidx.color.ColorImplicits.AndroidToColor3
import pl.enves.androidx.color.DrawableManip
import pl.enves.androidx.helpers._

abstract class ThemedImageButton(context: Context, button: ImageButton) extends DrawableManip {
  private var theme: Option[Theme] = None

  def onClick(function: View => Unit) = button onClick function

  def setVisibility(visibility: Int) = button.setVisibility(visibility)

  def setColorTheme(t: Theme): Unit = {
    theme = Some(t)
    updateImage()
  }

  def getImgRes: Int

  protected def updateImage(): Unit = {
    val res = context.getResources
    val drawable = new BitmapDrawable(res, BitmapFactory.decodeResource(res, getImgRes))
    drawable.setAntiAlias(true)

    maskColors(theme.get.background, theme.get.background, theme.get.color1, drawable)
    button.setImageDrawable(drawable)
  }
}
