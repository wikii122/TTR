package pl.enves.androidx.color

import android.graphics.Color
import pl.enves.androidx.color.ColorTypes.{ColorAndroid, ColorArray}

//TODO: make generic
trait ColorManip {
  def colorTransparent(color: ColorAndroid, alpha: Float): ColorAndroid = {
    return Color.argb(Math.round(alpha * 255), Color.red(color), Color.green(color), Color.blue(color))
  }

  def colorTransparent(color: ColorArray, alpha: Float): ColorArray = {
    return Array(color(0), color(1), color(2), alpha)
  }

  private def channelLerp(channel1: Int, channel2: Int, t: Float): Int = {
    return channel1 + (t * (channel2 - channel1)).toInt
  }

  def colorLerp(color1: ColorAndroid, color2: ColorAndroid, t: Float): ColorAndroid = {
    val a = channelLerp(Color.alpha(color1), Color.alpha(color2), t)
    val r = channelLerp(Color.red(color1), Color.red(color2), t)
    val g = channelLerp(Color.green(color1), Color.green(color2), t)
    val b = channelLerp(Color.blue(color1), Color.blue(color2), t)

    return Color.argb(a, r, g, b)
  }

  def colorDistance(color1: ColorAndroid, color2: ColorAndroid): Float = {
    val a = Math.abs(Color.alpha(color1) - Color.alpha(color2))
    val r = Math.abs(Color.red(color1) - Color.red(color2))
    val g = Math.abs(Color.green(color1) - Color.green(color2))
    val b = Math.abs(Color.blue(color1) - Color.blue(color2))

    return (a + r + g + b) / (256 * 4.0f)
  }

  def colorBrightness(color: ColorAndroid): Float = {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)

    return (r + g + b) / (256 * 3.0f)
  }
}
