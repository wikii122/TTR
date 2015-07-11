package pl.enves.ttr.graphics

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import pl.enves.ttr.R

object DefaultTextureId extends Enumeration {
  type TextureId = Value
  val ArrowLeft, ArrowLeftGray, ArrowRight, ArrowRightGray, Ring, Cross = Value
}

class DefaultTextures(context: Context) extends BitmapProvider {

  private def texNameToDrawable(name: String): Int = DefaultTextureId.withName(name) match {
    case DefaultTextureId.ArrowLeft => R.drawable.arrow_left
    case DefaultTextureId.ArrowRight => R.drawable.arrow_right
    case DefaultTextureId.ArrowLeftGray => R.drawable.arrow_left_gray
    case DefaultTextureId.ArrowRightGray => R.drawable.arrow_right_gray
    case DefaultTextureId.Ring => R.drawable.ring
    case DefaultTextureId.Cross => R.drawable.cross
  }

  override def getBitmapsNames: List[String] =
    DefaultTextureId.values.toList map {_.toString}

  override def getBitmap(name: String): Bitmap =
    BitmapFactory.decodeResource(context.getResources, texNameToDrawable(name))

}
