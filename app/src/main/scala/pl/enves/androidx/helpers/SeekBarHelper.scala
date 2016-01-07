package pl.enves.androidx.helpers

import android.graphics.{PorterDuff, PorterDuffColorFilter}
import android.widget.SeekBar
import pl.enves.androidx.color.ColorTypes.ColorAndroid

trait SeekBarHelper {

  implicit class SeekBarHelper(seekBar: SeekBar) {
    def setColors(progressColor: ColorAndroid, thumbColor: ColorAndroid): Unit = {
      seekBar.getProgressDrawable.setColorFilter(new PorterDuffColorFilter(progressColor, PorterDuff.Mode.SRC_IN))
      seekBar.getThumb.setColorFilter(new PorterDuffColorFilter(thumbColor, PorterDuff.Mode.SRC_IN))
    }
  }

}
