package pl.enves.ttr
package logic
package networking

import java.util

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.{GoogleApiAvailability, GooglePlayServicesUtil, ConnectionResult}
import com.google.android.gms.common.api.{ResultCallback, GoogleApiClient}
import com.google.android.gms.common.api.GoogleApiClient.{Builder, OnConnectionFailedListener, ConnectionCallbacks}
import com.google.android.gms.games.Games
import com.google.android.gms.games.multiplayer.turnbased.{TurnBasedMultiplayer, TurnBasedMatchConfig, TurnBasedMatch}
import pl.enves.androidx.Logging
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.utils.Configuration
import pl.enves.ttr.utils.exceptions.ServiceUnavailableException

object PlayServices extends ConnectionCallbacks with OnConnectionFailedListener with Logging {
  final val SIGN_IN = 9001 // Because reasons
  private[this] val client = if (Configuration.isMultiplayerAvailable) Option(clientInit())
    else None
  private[this] var counter = 0
  private[this] var signingIn = false

  def connect() = {
    if (nonAvailable) throw new ServiceUnavailableException("There seems to be no Google Play Game Services available or not supported in this version")
    client.get connect ()
    counter += 1
  }

  def disconnect() = {
    if (isConnected) {
      counter -= 1
      if (counter == 0) client.get disconnect()
    }
  }

  def getPlayerSelectIntent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(client.get, 1, 1, true)

  def createMatch(callee: ResultCallback[TurnBasedMultiplayer.InitiateMatchResult],
                  players: util.ArrayList[String]) = {
    val config = TurnBasedMatchConfig.builder()
      .addInvitedPlayers(players)
      .build()

    // TODO thresher
    Games.TurnBasedMultiplayer
      .createMatch(client.get, config)
      .setResultCallback(callee)
  }

  def takeTurn(matchInstance: TurnBasedMatch, turnData: String) = ???

  def finishMatch(matchInstance: TurnBasedMatch) = ???

  def isAvailable = Configuration.isMultiplayerAvailable && client.isDefined
  def nonAvailable = !isAvailable

  def isConnected = isAvailable && client.get.isConnected
  def notConnected = !isConnected

  override def onConnectionSuspended(i: Int): Unit = {
    log("Connection suspended, retrying")
    client.get.connect()
  }

  override def onConnected(bundle: Bundle): Unit = {
    log("Logged in")
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
        result.startResolutionForResult(activity, SIGN_IN)
      } catch {
        case e:IntentSender.SendIntentException => client.get.connect()
      }
    } else {
      // not resolvable... so show an error message
      val errorCode = result.getErrorCode
      val dialog = Option(GoogleApiAvailability.getInstance.getErrorDialog(activity, errorCode, SIGN_IN))
      if (dialog.isDefined) {
        dialog.get.show()
      } else {
        new AlertDialog.Builder(activity).setMessage(fallbackMessage).setNeutralButton(android.R.string.ok, null).create().show()
      }
    }
  }
}
