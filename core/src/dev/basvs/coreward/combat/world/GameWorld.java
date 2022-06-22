package dev.basvs.coreward.combat.world;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.combat.effects.PointParticleEffects;
import dev.basvs.coreward.combat.world.factory.ShipFactory;
import dev.basvs.coreward.combat.world.handlers.BulletHandler;
import dev.basvs.coreward.combat.world.handlers.PhysicsHandler;
import dev.basvs.coreward.combat.world.handlers.ShipHandler;
import dev.basvs.coreward.combat.world.ship.Ship;
import dev.basvs.coreward.design.ShipDesign;

public class GameWorld {

  public PointParticleEffects effects;
  // Game world
  public PhysicsHandler physics;
  public BulletHandler weaponHandler;
  TextureAtlas atlas;
  Array<Ship> ships = new Array<>();
  Array<Ship> destroyedShips = new Array<>();
  // Game updating
  ShipHandler shipHandler;

  public GameWorld(TextureAtlas atlas, PointParticleEffects effects) {
    this.atlas = atlas;
    this.effects = effects;
    physics = new PhysicsHandler(this);
    shipHandler = new ShipHandler(this);
    weaponHandler = new BulletHandler(this);
  }

  public Array<Ship> getShips() {
    return ships;
  }

  public Ship addShip(String name, ShipDesign design, Alignment alignment, float startX,
      float startY,
      float startAngle) {
    Ship ship = ShipFactory.createShip(name, design, alignment, atlas, this, startX, startY,
        startAngle);
    ships.add(ship);
    return ship;
  }

  public void update(float delta) {
    weaponHandler.update(delta);
    // Update ship status
    for (int s = 0; s < ships.size; s++) {
      Ship ship = ships.get(s);
      shipHandler.update(ship, delta);
      ship.update(delta);
      if (ship.destroyed) {
        destroyedShips.add(ship);
      }
    }
    while (destroyedShips.size > 0) {
      ships.removeValue(destroyedShips.pop(), true);
    }
    destroyedShips.clear();
    physics.update(delta);
  }
}
