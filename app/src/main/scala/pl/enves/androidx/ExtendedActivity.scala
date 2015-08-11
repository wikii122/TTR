package pl.enves.androidx

import android.content.{Context, Intent}
import android.os.{Handler, Looper}
import android.support.v7.app.AppCompatActivity
import android.os.Build
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
}
