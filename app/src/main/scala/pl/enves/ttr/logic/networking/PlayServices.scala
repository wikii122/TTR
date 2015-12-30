package pl.enves.ttr
package logic
package networking

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.{GooglePlayServicesUtil, ConnectionResult}
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.{Builder, OnConnectionFailedListener, ConnectionCallbacks}
import com.google.android.gms.games.Games
import pl.enves.androidx.Logging
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.utils.exceptions.ServiceUnavailableException

object PlayServices extends ConnectionCallbacks with OnConnectionFailedListener with Logging {
  private final val RC_SIGN_IN = 9001 // Because reasons
  private[this] val client = clientInit()
  private[this] var counter = 0
  private[this] var signingIn = false


  def connect() = {
    if (nonAvailable) throw new ServiceUnavailableException("There seems to be no Google Play Game Services installed on the device.")
    client connect ()
    counter += 1
  }

  def disconnect() = {
    if (isConnected) {
      counter -= 1
      if (counter == 0) client disconnect()
    }
  }

  def isAvailable = client != null
  def nonAvailable = !isAvailable

  def isConnected = isAvailable && client.isConnected
  def notConnected = !isConnected

  override def onConnectionSuspended(i: Int): Unit = ???

  override def onConnected(bundle: Bundle): Unit = {
    log("Connected")
  }

  override def onConnectionFailed(connectionResult: ConnectionResult): Unit = if (!signingIn) {
    log("Connection failed, trying sign in")
    signingIn = true
    resolveConnectionFailure(connectionResult, ContextRegistry.context.getString(R.string.sign_in_error))
    signingIn = false
  } else {
    log("Trying to sign in multiple times, ignoring call")
  }

  private[this] def clientInit(): GoogleApiClient = {
    val client = new GoogleApiClient.Builder(ContextRegistry.context)

    client addConnectionCallbacks this
    client addOnConnectionFailedListener this
    client addApi Games.API
    client addScope Games.SCOPE_GAMES
    client build()
  }

  private[this] def resolveConnectionFailure(result: ConnectionResult, fallbackMessage: String) {
    val activity = ContextRegistry.context.asInstanceOf[Activity]
    if (result.hasResolution) {
      try {
        result.startResolutionForResult(activity, RC_SIGN_IN)
      } catch {
        case e:IntentSender.SendIntentException => client.connect()
      }
    } else {
      // not resolvable... so show an error message
      val errorCode = result.getErrorCode
      val dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, RC_SIGN_IN)
      if (dialog != null) {
        dialog.show()
      } else {
        new AlertDialog.Builder(activity).setMessage(fallbackMessage).setNeutralButton(android.R.string.ok, null).create().show()
      }
    }
  }
}
