package pl.enves.ttr.utils.androidx

import android.app.Activity
import android.content.{Context, Intent}
import pl.enves.ttr.utils.Logging

import scala.reflect.{ClassTag, classTag}

abstract class ExtendedActivity extends Activity with Logging {
  type ID = Int

  implicit protected[this] val context: Context = this

  protected def find[A](id: ID) = findViewById(id).asInstanceOf[A]

  protected def intent[A: ClassTag] = new Intent(this, classTag[A].runtimeClass)
}
