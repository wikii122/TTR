package pl.enves.ttr.graphics.textures

import android.content.Context
import android.graphics.BitmapFactory

object Texture {
  def apply(context: Context, image: Int) = BitmapFactory.decodeResource(context.getResources, image)
  def apply(tex: ProceduralTexture) = tex.getBitmap
}