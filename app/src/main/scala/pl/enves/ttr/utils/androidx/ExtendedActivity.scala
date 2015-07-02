package pl.enves.ttr.utils.androidx

import android.app.Activity

abstract class ExtendedActivity extends Activity {
  type ID = Int

  def find[A](id: ID) = findViewById(id).asInstanceOf[A]
}
