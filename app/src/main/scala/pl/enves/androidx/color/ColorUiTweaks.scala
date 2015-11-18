package pl.enves.androidx.color

import android.content.res.ColorStateList
import pl.enves.androidx.color.ColorTypes.ColorAndroid

trait ColorUiTweaks extends ColorManip {
  def colorStateList(baseColor: ColorAndroid, disabledTransparency: Float): ColorStateList = {
    val states = Array[Array[Int]](
      Array[Int](android.R.attr.state_enabled), // enabled
      Array[Int](-android.R.attr.state_enabled) // disabled
    )

    val colors = Array[Int](
      baseColor,
      colorTransparent(baseColor, disabledTransparency)
    )

    return new ColorStateList(states, colors)
  }
}
