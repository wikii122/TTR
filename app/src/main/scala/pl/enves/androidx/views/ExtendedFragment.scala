package pl.enves.androidx.views

import android.support.v4.app.Fragment
import android.view.View
import pl.enves.androidx.context.ContextRegistry

abstract class ExtendedFragment extends Fragment {
  protected def find[A](view: View, id: Int) = view.findViewById(id).asInstanceOf[A]

  protected def UiThread(function: () => Unit) =
    ContextRegistry.context.asInstanceOf[ExtendedActivity] runOnMainThread function

}
