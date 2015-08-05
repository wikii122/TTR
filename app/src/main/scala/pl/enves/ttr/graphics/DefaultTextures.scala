package pl.enves.ttr.graphics

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import pl.enves.ttr.R

object DefaultTextureId extends Enumeration {
  type TextureId = Value
  val
  MaskCross,
  MaskRing,
  MaskEmpty,
  MaskArrowRight,
  MaskArrowLeft = Value
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
    (DefaultTextureId.MaskCross.toString, decode(R.drawable.pat_cross_mod_mask, 256, 256, Bitmap.Config.ARGB_8888)),
    (DefaultTextureId.MaskRing.toString, decode(R.drawable.pat_ring_mod_mask, 256, 256, Bitmap.Config.ARGB_8888)),
    (DefaultTextureId.MaskEmpty.toString, decode(R.drawable.pat_empty_mod_mask, 256, 256, Bitmap.Config.ARGB_8888)),
    (DefaultTextureId.MaskArrowLeft.toString, decode(R.drawable.pat_arrow_left_mod_mask, 256, 256, Bitmap.Config.ARGB_8888)),
    (DefaultTextureId.MaskArrowRight.toString, decode(R.drawable.pat_arrow_right_mod_mask, 256, 256, Bitmap.Config.ARGB_8888))
  )
}
