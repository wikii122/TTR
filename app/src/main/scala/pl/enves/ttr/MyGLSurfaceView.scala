package pl.enves.ttr

import android.content.Context
;
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import pl.enves.ttr.renderer.MyRenderer

class MyGLSurfaceView(context: Context, attrs: AttributeSet) extends GLSurfaceView(context, attrs) {
  var mMyRenderer: MyRenderer = _
  if (!isInEditMode) {
    mMyRenderer = new MyRenderer()
    setEGLConfigChooser(false)
    setEGLContextClientVersion(2)

    setRenderer(mMyRenderer)

    //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
  }
}
