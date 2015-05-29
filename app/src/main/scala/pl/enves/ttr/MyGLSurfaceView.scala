package pl.enves.ttr

import android.content.Context
;
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView(context: Context , attrs: AttributeSet) extends GLSurfaceView(context, attrs) {
  var mMyRenderer: MyRenderer = _
  if(!isInEditMode) {
    mMyRenderer = new MyRenderer()
    setRenderer(mMyRenderer)
  }
}
