package pl.enves.androidx

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.{Handler, Looper}
import android.support.v7.app.AppCompatActivity
import android.os.Build
import android.view.{View, WindowManager}
import pl.enves.androidx.context.ContextRegistry
import scala.reflect.{ClassTag, classTag}

abstract class ExtendedActivity extends AppCompatActivity with ContextRegistry with Logging {
  type ID = Int

  private lazy val handler = new Handler(Looper.getMainLooper)
  private lazy val uiThread = Looper.getMainLooper.getThread

  protected def find[A](id: ID) = findViewById(id).asInstanceOf[A]

  protected def intent[A: ClassTag] = new Intent(this, classTag[A].runtimeClass)

  protected def UiThread(f: () => Unit) = {
    lazy val runnable = new Runnable() { def run() = f() }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      runOnUiThread(runnable)
    }
    else {
      if (uiThread == Thread.currentThread) f()
      else handler.post(runnable)
    }
  }

  protected def setGui() = {
    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    getWindow.getDecorView.setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )
  }

  protected def setBottomBarGui() = {
    val window = getWindow
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
  }

  protected def setToolbarGui() = {
    val window = getWindow
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    //dark action bar default color is #212121
    val color = Color.argb(0x80, 0x21, 0x21, 0x21)
    getSupportActionBar.setBackgroundDrawable(new ColorDrawable(color))
    //getSupportActionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT))
  }
}
