package pl.enves.ttr
package logic
package networking

import java.util

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.common.api.GoogleApiClient.{ConnectionCallbacks, OnConnectionFailedListener}
import com.google.android.gms.common.api.{GoogleApiClient, ResultCallback}
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.games.Games
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.turnbased.{TurnBasedMatch, TurnBasedMatchConfig, TurnBasedMultiplayer}
import pl.enves.androidx.Logging
import pl.enves.androidx.context.ContextRegistry
import pl.enves.ttr.utils.Configuration
import pl.enves.ttr.utils.ExecutorContext._
import pl.enves.ttr.utils.exceptions.ServiceUnavailableException

import scala.concurrent.Future

object PlayServices extends ConnectionCallbacks with OnConnectionFailedListener with Logging {
  final val SIGN_IN = 9001 // Because reasons
  private[this] val client = Option(clientInit())
  private[this] var signingIn = false

  def connect() = {
    if (!Configuration.isMultiplayerAvailable) throw new ServiceUnavailableException("There seems to be no Google Play Game Services available or not supported in this version")
    client.get connect ()
  }

  def disconnect() = if (isConnected) client.get disconnect ()

  def getPlayerSelectIntent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(client.get, 1, 1, true)

  def createMatch(callee: ResultCallback[TurnBasedMultiplayer.InitiateMatchResult],
                  players: util.ArrayList[String]) = {
    val config = TurnBasedMatchConfig.builder()
      .addInvitedPlayers(players)
      .build()

    Games.TurnBasedMultiplayer
      .createMatch(client.get, config)
      .setResultCallback(callee)
  }

  def takeTurn(matchInstance: TurnBasedMatch, turnData: String, participant: String) = Future {
    Games.TurnBasedMultiplayer.takeTurn(client.get, matchInstance.getMatchId, turnData.getBytes, participant).await()
  }

  def invitations: Future[List[Invitation]] = Future {
    val promise = Games.Invitations.loadInvitations(client.get)
    val result = promise.await()
    val invitationBuffer = result.getInvitations
    val e = for (i <- 0 until invitationBuffer.getCount) yield invitationBuffer.get(i)

    e.toList
  }

  def accept(invitation: Invitation) = Future {
    Games.TurnBasedMultiplayer.acceptInvitation(client.get, invitation.getInvitationId).await()
  }

  def finishMatch(matchInstance: TurnBasedMatch) = ???

  def isAvailable = client.isDefined
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
