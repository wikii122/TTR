package pl.enves.ttr.graphics

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import pl.enves.ttr.R

object DefaultTextureId extends Enumeration {
  type TextureId = Value
  val ArrowLeft, ArrowLeftGray, ArrowRight, ArrowRightGray, Ring, Cross = Value
}

class DefaultTextures(context: Context) extends TextureProvider {
  private val res = context.getResources

  private def calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int = {
    // Raw height and width of image
    val height: Int = options.outHeight
    val width: Int = options.outWidth
    var inSampleSize: Int = 1

    if (height > reqHeight || width > reqWidth) {
      val halfHeight: Int = height / 2
      val halfWidth: Int = width / 2

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) > reqHeight
        && (halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  def decode(drawable: Int, width: Int = 512, height: Int = 512, config:Bitmap.Config = Bitmap.Config.ARGB_8888): Int = {
    val options: BitmapFactory.Options = new BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, drawable, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, width, height)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false

    options.inPreferredConfig = config

    val bitmap = BitmapFactory.decodeResource(res, drawable, options)
    val texture = createTexture(bitmap)
    bitmap.recycle()
    return texture
  }

  override def getTextures: Map[String, Int] = Map(
    (DefaultTextureId.ArrowLeft.toString, decode(R.drawable.arrow_left, 128, 128, Bitmap.Config.ARGB_4444)),
    (DefaultTextureId.ArrowRight.toString, decode(R.drawable.arrow_right, 128, 128, Bitmap.Config.ARGB_4444)),
    (DefaultTextureId.ArrowLeftGray.toString, decode(R.drawable.arrow_left_gray, 128, 128, Bitmap.Config.ARGB_4444)),
    (DefaultTextureId.ArrowRightGray.toString, decode(R.drawable.arrow_right_gray, 128, 128, Bitmap.Config.ARGB_4444)),
    (DefaultTextureId.Ring.toString, decode(R.drawable.ring, 128, 128, Bitmap.Config.ARGB_4444)),
    (DefaultTextureId.Cross.toString, decode(R.drawable.cross, 128, 128, Bitmap.Config.ARGB_4444))
  )
}
