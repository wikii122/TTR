package pl.enves.androidx

import android.content.{Context, Intent, SharedPreferences}
import android.os.{Build, Bundle, Handler, Looper}
import android.support.v7.app.AppCompatActivity
import pl.enves.androidx.context.ContextRegistry

import scala.reflect.{ClassTag, classTag}

abstract class ExtendedActivity extends AppCompatActivity with ContextRegistry with Logging {
  type ID = Int
  protected var prefs: Option[SharedPreferences] = None

  private lazy val handler = new Handler(Looper.getMainLooper)
  private lazy val uiThread = Looper.getMainLooper.getThread

  protected def find[A](id: ID) = findViewById(id).asInstanceOf[A]

  protected def intent[A: ClassTag] = new Intent(this, classTag[A].runtimeClass)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    prefs = Some(getSharedPreferences("preferences", Context.MODE_PRIVATE))
  }

  protected def UiThread(f: => Unit) = {
    lazy val runnable = new Runnable() {
      def run() = f
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      runOnUiThread(runnable)
    }
    else {
      if (uiThread == Thread.currentThread) f
      else handler.post(runnable)
    }
  }

  protected implicit def UnitToUnit(f: => Unit): () => Unit = () => f
}
