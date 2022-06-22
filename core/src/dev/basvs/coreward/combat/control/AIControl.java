package dev.basvs.coreward.combat.control;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;
import java.util.EnumMap;

public class AIControl {

  private final EnumMap<Control, Array<Cell>> controlModuleMap = new EnumMap<>(Control.class);
  private final Ship ship;
  private final GameWorld world;
  private Ship target;
  private float weaponsRange;
  private float relativeAngle;

  public AIControl(Ship ship, GameWorld world) {
    this.ship = ship;
    this.world = world;
    // Setup default control/module mappings
    for (Control c : Control.values()) {
      controlModuleMap.put(c, new Array<Cell>());
    }
    for (int x = 0; x < ship.design.width; x++) {
      for (int y = 0; y < ship.design.height; y++) {
        Cell c = ship.cells[x][y];
        if (c.module != null) {
          if (c.module.design.thruster != null) {
            switch (c.direction) {
              case 0:
                controlModuleMap.get(Control.Forward).add(c);
                break;
              case 1:
                controlModuleMap.get(Control.StrafeRight).add(c);
                break;
              case 2:
                controlModuleMap.get(Control.Backward).add(c);
                break;
              case 3:
                controlModuleMap.get(Control.StrafeLeft).add(c);
                break;
            }
          }
          if (c.module.design.reactionWheel != null) {
            controlModuleMap.get(Control.RotateClockwise).add(c);
            controlModuleMap.get(Control.RotateCounterClockwise).add(c);
          }
          if (c.module.design.weapon != null) {
            controlModuleMap.get(Control.Fire).add(c);
            // TODO weapons range should take into account relative movement speed
            weaponsRange = Math.max(weaponsRange,
                c.module.design.weapon.lifeTime * c.module.design.weapon.speed
                    * Attributes.MAX_BULLET_VELOCITY);
          }
        }
      }
    }
  }

  public void update(float delta) {
    if (!ship.destroyed) {
      clearControlState();
      if (target == null) {
        // Choose random target
        target = world.getShips().random();
        if (target == ship) {
          target = null;
        }
      } else {
        // Determine angle towards target
        float angleT = angleToTarget();
        float angleS = ship.body.getAngle() + MathUtils.PI / 2f;
        relativeAngle = angleS - angleT;
        relativeAngle =
            (relativeAngle %= MathUtils.PI2) >= 0 ? (relativeAngle < MathUtils.PI) ? relativeAngle
                : relativeAngle - MathUtils.PI2
                : (relativeAngle >= -MathUtils.PI) ? relativeAngle : relativeAngle
                    + MathUtils.PI2;
        if (relativeAngle > 0f) {
          setPrimaryControlState(ship.temp.reactionWheels, true);
          setSecondaryControlState(ship.temp.reactionWheels, false);
        } else {
          setPrimaryControlState(ship.temp.reactionWheels, false);
          setSecondaryControlState(ship.temp.reactionWheels, true);
        }

        for (int i = 0; i < ship.temp.weapons.size; i++) {
          Cell cell = ship.temp.weapons.get(i);
          cell.t.targetPoint.set(target.body.getPosition());
        }

        float targetRange = ship.body.getPosition().dst(target.body.getPosition());
        if (targetRange < (weaponsRange * 0.5f)) {
          setPrimaryControlState(ship.temp.weapons, true);
          setPrimaryControlState(controlModuleMap.get(Control.Forward), false);
          if (targetRange < (weaponsRange * 0.25f)) {
            setPrimaryControlState(controlModuleMap.get(Control.Backward), true);
          } else {
            setPrimaryControlState(controlModuleMap.get(Control.Backward), false);
          }
        } else {
          setPrimaryControlState(ship.temp.weapons, false);
          setPrimaryControlState(controlModuleMap.get(Control.Forward), true);
          setPrimaryControlState(controlModuleMap.get(Control.Backward), false);
        }
      }
    }
  }

  void clearControlState() {
    setPrimaryControlState(ship.temp.reactionWheels, false);
    setSecondaryControlState(ship.temp.reactionWheels, false);
  }

  float angleToTarget() {
    Vector2 tP = target.body.getPosition();
    Vector2 sP = ship.body.getPosition();
    return MathUtils.atan2(tP.y - sP.y, tP.x - sP.x);
  }

  void setPrimaryControlState(Array<Cell> cells, boolean primary) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      cell.t.primaryControl = primary;
    }
  }

  void setSecondaryControlState(Array<Cell> cells, boolean secondary) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      cell.t.secondaryControl = secondary;
    }
  }

  public enum Control {
    Forward, Backward, RotateClockwise, RotateCounterClockwise, StrafeLeft, StrafeRight, Fire
  }
}
