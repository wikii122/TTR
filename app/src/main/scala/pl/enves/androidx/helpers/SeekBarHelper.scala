package pl.enves.androidx.helpers

import android.graphics.{PorterDuff, PorterDuffColorFilter}
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import pl.enves.androidx.color.ColorTypes.ColorAndroid

trait SeekBarHelper {

  implicit class SeekBarHelper(seekBar: SeekBar) {
    def onChange(function: (SeekBar, Int, Boolean) => Unit) = seekBar.setOnSeekBarChangeListener(
      new OnSeekBarChangeListener {
        override def onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean): Unit = {
          function(seekBar, progress, fromUser)
        }

        override def onStopTrackingTouch(seekBar: SeekBar): Unit = {}

        override def onStartTrackingTouch(seekBar: SeekBar): Unit = {}
      })

    def setColors(progressColor: ColorAndroid, thumbColor: ColorAndroid): Unit = {
      seekBar.getProgressDrawable.setColorFilter(new PorterDuffColorFilter(progressColor, PorterDuff.Mode.SRC_IN))
      seekBar.getThumb.setColorFilter(new PorterDuffColorFilter(thumbColor, PorterDuff.Mode.SRC_IN))
    }
  }

}
