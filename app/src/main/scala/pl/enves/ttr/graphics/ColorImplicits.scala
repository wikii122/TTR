package pl.enves.ttr.graphics

import android.graphics.Color
import pl.enves.ttr.graphics.ColorTypes._

object ColorImplicits {
  implicit def AndroidToArray(c: ColorAndroid): ColorArray = Array(Color.red(c) / 255.0f, Color.green(c) / 255.0f, Color.blue(c) / 255.0f, Color.alpha(c) / 255.0f)

  implicit def AndroidToColor4(c: ColorAndroid): Color4 = (Color.red(c) / 255.0f, Color.green(c) / 255.0f, Color.blue(c) / 255.0f, Color.alpha(c) / 255.0f)

  implicit def AndroidToColor3(c: ColorAndroid): Color3 = (Color.red(c) / 255.0f, Color.green(c) / 255.0f, Color.blue(c) / 255.0f)
}
