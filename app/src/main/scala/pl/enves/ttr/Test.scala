package pl.enves.ttr

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.annotation.NonNull
import android.view.{View, MenuItem, Menu}

class Test extends Activity {
  private var mGLView: MyGLSurfaceView = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_test)

    mGLView = findViewById(R.id.myGLSurfaceView).asInstanceOf[MyGLSurfaceView]
  }

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  override def onSaveInstanceState(@NonNull outState: Bundle) {
    super.onSaveInstanceState(outState)

  }

  override def onStop() {
    super.onStop()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.menu_test, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.getItemId

    id match {
      case R.id.action_settings => {
        //Intent intent = new Intent(this, SettingsActivity.class)
        //startActivity(intent)
        true
      }
      case default =>
        super.onOptionsItemSelected(item);
    }
  }
}
