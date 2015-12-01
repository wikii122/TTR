package pl.enves.ttr.utils.themes

import android.content.Context
import android.widget.ImageButton

class ThemedOneImageButton(context: Context, button: ImageButton, imgRes: Int)
  extends ThemedImageButton(context, button) {
  override def getImgRes = imgRes
}
