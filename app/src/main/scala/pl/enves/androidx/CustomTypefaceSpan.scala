package pl.enves.androidx

import android.graphics.{Paint, Typeface}
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * http://stackoverflow.com/questions/4819049/how-can-i-use-typefacespan-or-stylespan-with-a-custom-typeface
 */
class CustomTypefaceSpan(typeface: Typeface) extends MetricAffectingSpan {
  override def updateDrawState(drawState: TextPaint): Unit = {
    applyCustomTypeFace(drawState, typeface)
  }

  override def updateMeasureState(paint: TextPaint): Unit = {
    applyCustomTypeFace(paint, typeface)
  }

  private def applyCustomTypeFace(paint: Paint, tf: Typeface) {
    val old: Typeface = paint.getTypeface
    val oldStyle = if (old != null) old.getStyle else 0

    val fake = oldStyle & ~tf.getStyle
    if ((fake & Typeface.BOLD) != 0) {
      paint.setFakeBoldText(true)
    }

    if ((fake & Typeface.ITALIC) != 0) {
      paint.setTextSkewX(-0.25f)
    }

    paint.setTypeface(tf)
  }
}
