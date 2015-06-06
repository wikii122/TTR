package pl.enves.ttr

import android.app.Activity
import android.os.Bundle
import android.view.{Menu, MenuItem}
import pl.enves.ttr.logic._
import pl.enves.ttr.utils.Logging

// TODO REMOVE IN PRODUCTION
class EngineTestActivity extends Activity with Logging {
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_engine_tests)

    log("Starting")
    Game.start(Player.X)
    log("Started")

    log("Making move")
    Game.make(Position(1,3))
    log("Moved")

    log(s"Player: ${Game.player}")

    log("Making rotation")
    Game.make(Rotation(Quadrant.first, Rotation.r90))
    log("Rotated")

    log("Reading state")
    val state = Game.state
    log("Current state")
    state foreach (d => log(d.toString()))

    log(s"Game state: ${Game.winner}")

  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_engine_tests, menu)
    return true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
