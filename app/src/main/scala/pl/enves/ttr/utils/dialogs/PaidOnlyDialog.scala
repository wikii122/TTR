package pl.enves.ttr
package utils
package dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import pl.enves.androidx.context.ContextRegistry

object PaidOnlyDialog {
  def apply(): Dialog = {
    // Use the Builder class for convenient dialog construction
    val builder = new AlertDialog.Builder(ContextRegistry.context)
    builder.setMessage(R.string.paid_only)
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, id: Int) {
        // Pass
      }
    })
    return builder.create()
  }

  def show() = apply().show()
}