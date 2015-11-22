package pl.enves.androidx.color

import android.graphics.drawable.BitmapDrawable
import android.graphics.{ColorMatrix, ColorMatrixColorFilter}
import pl.enves.androidx.color.ColorTypes.Color3

trait DrawableManip {
  /**
   * Equivalent to MaskShader
   * @param red Color to substitute red color in the mask
   * @param green Color to substitute green color in the mask
   * @param blue Color to substitute blue color in the mask
   * @param drawable Mask
   */
  def maskColors(red: Color3, green: Color3, blue: Color3, drawable: BitmapDrawable): Unit = {
    val src = Array[Float] (
      red._1, green._1, blue._1, 0.0f, 0.0f,
      red._2, green._2, blue._2, 0.0f, 0.0f,
      red._3, green._3, blue._3, 0.0f, 0.0f,
      0.0f, 0.0f, 0.0f, 1.0f, 0.0f )
    val matrix = new ColorMatrix(src)
    val filter = new ColorMatrixColorFilter(matrix)
    drawable.setColorFilter(filter)
  }
}
