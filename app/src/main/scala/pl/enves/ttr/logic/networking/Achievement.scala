package pl.enves.ttr.logic.networking

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games

/**
 * Class to manage achevements
 */
class Achievement(client: Option[GoogleApiClient]) {
  def unlock(id: String) = if (PlayServices.isConnected)
    Games.Achievements.unlock(client.get, id)

  def step(id: String) = if (PlayServices.isConnected)
    Games.Achievements.increment(client.get, id, 1)

  def increment(id: String, i: Int) = if (PlayServices.isConnected)
    Games.Achievements.increment(client.get, id, i)
}
