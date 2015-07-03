package pl.enves.androidx

import android.content.{Context, Intent}
import android.support.v7.app.AppCompatActivity
import pl.enves.ttr.utils.Logging

import scala.reflect.{ClassTag, classTag}

abstract class ExtendedActivity extends AppCompatActivity with Logging {
  type ID = Int

  implicit protected[this] val context: Context = this

  protected def find[A](id: ID) = findViewById(id).asInstanceOf[A]

  protected def intent[A: ClassTag] = new Intent(this, classTag[A].runtimeClass)
}
