package pl.enves.androidx
package views

import android.content.Intent
import android.os.{Build, Handler, Looper}
import android.support.v7.app.AppCompatActivity
import pl.enves.androidx.context.ContextRegistry
import pl.enves.androidx.helpers.FunctionHelper

import scala.reflect.{ClassTag, classTag}

abstract class ExtendedActivity extends AppCompatActivity with ContextRegistry
with Logging with FunctionHelper {
  type ID = Int

  private lazy val handler = new Handler(Looper.getMainLooper)
  private lazy val uiThread = Looper.getMainLooper.getThread

  protected def find[A](id: ID) = findViewById(id).asInstanceOf[A]

  protected def intent[A: ClassTag] = new Intent(this, classTag[A].runtimeClass)

  protected def sendIntent = new Intent(Intent.ACTION_SENDTO)

  def runOnMainThread(f: => Unit): Unit = {
    lazy val runnable = new Runnable() {
      override def run() = f
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      runOnUiThread(runnable)
    }
    else {
      if (uiThread == Thread.currentThread) f
      else handler.post(runnable)
    }
  }
}
