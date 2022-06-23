package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.basvs.coreward.combat.effects.ParticleEmitterSource;

public class Cell implements ParticleEmitterSource {

  public int gridX, gridY;
  public int direction;

  public Module module;

  public Ship ship;

  public CellTemp t = new CellTemp();

  private Vector2 v = new Vector2();

  public boolean isAlive() {
    return module != null && module.hitpoints > 0f;
  }

  public boolean isPowered() {
    // Is the module supplied with power or is its local buffer full?
    return ((module.design.consumer != null && module.design.consumer.localBufferSize > 0f
        && module.localBuffered >= module.design.consumer.localBufferSize) || (
        module.design.consumer != null && module.design.consumer.localBufferSize == 0f
            && t.eNetwork != null && t.eNetwork.effectiveness > 0f));
  }

  public boolean isReady() {
    return isAlive() && isPowered();
  }

  public boolean isPrimaryActive() {
    return isReady() && t.primaryControl;
  }

  public boolean isSecondaryActive() {
    return isReady() && t.secondaryControl;
  }

  public float cellDirection() {
    return -90f * direction;
  }

  public float minWeaponAimLocal() {
    return cellDirection() - module.design.weapon.aim / 2f + 90f;
  }

  public float maxWeaponAimLocal() {
    return cellDirection() + module.design.weapon.aim / 2f + 90f;
  }

  /**
   * @return the world aim angle of the weapon on this cell
   */
  public float weaponAimGlobal() {
    return ship.getAngle() + module.weaponLocalAim;
  }

  @Override
  public float getEmitterX() {
    v.set(0, -0.8f).rotateDeg(cellDirection()).add(gridX, gridY);
    return ship.toWorld(v).x;
  }

  @Override
  public float getEmitterY() {
    v.set(0, -0.8f).rotateDeg(cellDirection()).add(gridX, gridY);
    return ship.toWorld(v).y;
  }

  @Override
  public float getEmitterVelocityX() {
    return ship.body.getLinearVelocity().x;
  }

  @Override
  public float getEmitterVelocityY() {
    return ship.body.getLinearVelocity().y;
  }

  @Override
  public float getEmitterAngle() {
    if (module != null && module.design.weapon != null) {
      return module.weaponLocalAim + ship.body.getAngle() * MathUtils.radiansToDegrees;
    }
    return ship.body.getAngle() * MathUtils.radiansToDegrees + cellDirection() - 90f;
  }

  @Override
  public boolean isEmitterEnabled() {
    return isPrimaryActive() || isSecondaryActive();
  }
}
