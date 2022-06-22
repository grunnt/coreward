package dev.basvs.coreward.combat.world.handlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;

public class OwnShipRayCastCallback implements RayCastCallback {

  Ship ship;
  boolean shipHit;

  public boolean isShipHit() {
    return shipHit;
  }

  public void reset(Ship ship) {
    this.ship = ship;
    shipHit = false;
  }

  @Override
  public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
    if (fixture.getUserData() != null) {
      if (fixture.getUserData() instanceof Cell) {
        Cell cell = (Cell) fixture.getUserData();
        if (cell.ship == ship) {
          shipHit = true;
        }
      }
    }
    return 0;
  }
}
