package dev.basvs.coreward.combat.world.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;
import dev.basvs.lib.MathHelper;

public class ShipHandler {

  GameWorld world;

  Vector2 tempPoint = new Vector2(), tempVec = new Vector2();
  Vector2 v = new Vector2();

  public ShipHandler(GameWorld world) {
    this.world = world;
  }

  public void update(Ship ship, float delta) {
    if (!ship.destroyed) {
      updateThrusters(ship, ship.temp.thrusters);
      updateReactionWheels(ship, ship.temp.reactionWheels);
      updateWeapons(delta, ship, ship.temp.weapons);
      if (ship.body.getLinearVelocity().len() > 20.0) {
        ship.body.setLinearVelocity(ship.body.getLinearVelocity().limit(20));
      }
    }
  }

  void updateThrusters(Ship ship, Array<Cell> cells) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      if (cell.isPrimaryActive()) {
        tempPoint = ship.toWorld(tempPoint.set(cell.gridX, cell.gridY));
        float force = cell.module.design.thruster.force * Attributes.THRUST_FACTOR
            * cell.t.eNetwork.effectiveness;
        switch (cell.direction) {
          case 0:
            tempVec.set(0, force);
            break;
          case 1:
            tempVec.set(force, 0);
            break;
          case 2:
            tempVec.set(0, -force);
            break;
          case 3:
            tempVec.set(-force, 0);
            break;
        }
        ship.body.applyForce(ship.body.getWorldVector(tempVec), tempPoint, true);
      }
    }
  }

  void updateReactionWheels(Ship ship, Array<Cell> cells) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      if (cell.isPrimaryActive() || cell.isSecondaryActive()) {
        float force = cell.module.design.reactionWheel.force * Attributes.TORQUE_FACTOR
            * cell.t.eNetwork.effectiveness;
        if (cell.isPrimaryActive()) {
          force = -force;
        }
        ship.body.applyTorque(force, true);
      }
    }
  }

  void updateWeapons(float delta, Ship ship, Array<Cell> cells) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      if (cell.isAlive()) {
        cell.t.muzzlePoint.set(0f, 0f);
        cell.t.muzzlePoint.rotateDeg(cell.module.weaponLocalAim - 90f);
        cell.t.muzzlePoint.add(cell.gridX, cell.gridY);
        cell.t.muzzlePoint.set(ship.toWorld(cell.t.muzzlePoint));
        cell.t.targetGlobalAngle = MathUtils.atan2(cell.t.targetPoint.y - cell.t.muzzlePoint.y,
            cell.t.targetPoint.x - cell.t.muzzlePoint.x) * MathUtils.radiansToDegrees;
        // Rotate weapon towards target angle, if possible
        float muzzleAngle = cell.weaponAimGlobal();
        float angleDelta = MathHelper.getRelativeAngle(muzzleAngle, cell.t.targetGlobalAngle);
        if (angleDelta < 0) {
          cell.module.weaponLocalAim += cell.module.design.weapon.aimSpeed * delta;
          if (cell.module.design.weapon.aim < 360f) {
            cell.module.weaponLocalAim = Math.min(cell.module.weaponLocalAim,
                cell.maxWeaponAimLocal());
          }
        } else if (angleDelta > 0) {
          cell.module.weaponLocalAim -= cell.module.design.weapon.aimSpeed * delta;
          if (cell.module.design.weapon.aim < 360f) {
            cell.module.weaponLocalAim = Math.max(cell.module.weaponLocalAim,
                cell.minWeaponAimLocal());
          }
        }
        // Weapon is ready and controlled to fire?
        if (cell.isPrimaryActive()) {
          if (world.weaponHandler.fireBullet(ship, cell, false)) {
            // Consume buffered energy
            cell.module.localBuffered = 0f;
            if (cell.module.design.weapon.burst > 1) {
              cell.module.burst = cell.module.design.weapon.burst - 1;
              cell.module.burstTime = cell.module.design.weapon.burstInterval;
            }
          }
        }
        if (cell.module.design.weapon.burst > 1 && cell.module.burst > 0) {
          cell.module.burstTime -= delta;
          while (cell.module.burstTime <= 0f && cell.module.burst > 0) {
            cell.module.burstTime += cell.module.design.weapon.burstInterval;
            cell.module.burst--;
            world.weaponHandler.fireBullet(ship, cell, true);
          }
        }
      }
    }
  }
}
