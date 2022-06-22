package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

/**
 * Temporary data for cells. Can be regenerated and does not need to be saved.
 */
public class CellTemp {

  // Control state
  public boolean enabled;
  public boolean primaryControl, secondaryControl;
  // Energy network data
  public float bufferedEnergy;
  public EnergyNetwork eNetwork;
  // Physics
  public Fixture fixture;
  // Weapon data
  public Vector2 muzzlePoint = new Vector2();
  public float targetGlobalAngle;
  public Vector2 targetPoint = new Vector2();
  // Fill algorithm flag
  public boolean visitFlag;
}
