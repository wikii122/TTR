package pl.enves.androidx.views

import android.support.v4.app.Fragment
import android.view.View
import pl.enves.androidx.context.ContextRegistry
import pl.enves.androidx.helpers.FunctionHelper

abstract class ExtendedFragment extends Fragment with FunctionHelper {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected def runOnMainThread(function: => Unit): Unit =
    ContextRegistry.context.asInstanceOf[ExtendedActivity].runOnMainThread(function)
}
