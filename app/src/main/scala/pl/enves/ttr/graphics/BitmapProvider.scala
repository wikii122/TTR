package pl.enves.ttr.graphics

import android.graphics.Bitmap

/**
 * It doesn't return a simple map because of high memory usage
 */
trait BitmapProvider {
  def getBitmapsNames: List[String]
  def getBitmap(name: String): Bitmap
}
