package dev.basvs.coreward.combat.world.handlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.world.Bullet;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class CollisionHandler implements ContactListener {

  GameWorld world;

  public CollisionHandler(GameWorld world) {
    this.world = world;
  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {
  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
    // Determine what has collided with what
    Fixture fixtureA = contact.getFixtureA();
    Fixture fixtureB = contact.getFixtureB();
    Cell cellA = null;
    Cell cellB = null;
    if (fixtureA.getUserData() != null && fixtureA.getUserData() instanceof Cell) {
      cellA = (Cell) fixtureA.getUserData();
    }
    if (fixtureB.getUserData() != null && fixtureB.getUserData() instanceof Cell) {
      cellB = (Cell) fixtureB.getUserData();
    }
    if (cellA != null && cellB != null) {
      handleShipToShip(cellA, cellB, impulse);
    }
  }

  @Override
  public void endContact(Contact contact) {
  }

  @Override
  public void beginContact(Contact contact) {
    // Determine what has collided with what
    Fixture fixtureA = contact.getFixtureA();
    Fixture fixtureB = contact.getFixtureB();
    Cell cell = null;
    Bullet bullet = null;
    if (fixtureA.getUserData() != null) {
      if (fixtureA.getUserData() instanceof Cell) {
        cell = (Cell) fixtureA.getUserData();
      } else if (fixtureA.getUserData() instanceof Bullet) {
        bullet = (Bullet) fixtureA.getUserData();
      }
    }
    if (fixtureB.getUserData() != null) {
      if (fixtureB.getUserData() instanceof Cell) {
        cell = (Cell) fixtureB.getUserData();
      } else if (fixtureB.getUserData() instanceof Bullet) {
        bullet = (Bullet) fixtureB.getUserData();
      }
    }
    if (bullet != null && cell != null) {
      handleBulletToShip(bullet, cell);
    }
  }

  void handleShipToShip(Cell cellA, Cell cellB, ContactImpulse impulse) {
    if (cellA.module != null && cellB.module != null) {
      float damage = impulse.getNormalImpulses()[0] * Attributes.COLLISION_DAMAGE_FACTOR;
      cellA.module.hitpoints -= damage;
      cellB.module.hitpoints -= damage;
      if (cellA.module.hitpoints <= 0f) {
        cellA.ship.destroyModule(cellA, true);
      }
      if (cellB.module.hitpoints <= 0f) {
        cellB.ship.destroyModule(cellB, true);
      }
    }
  }

  private ArrayDeque<Cell> areaOfEffect = new ArrayDeque<>();

  void handleBulletToShip(Bullet bullet, Cell cell) {
    if (bullet.active && cell.module != null) {
      Ship ship = cell.ship;
      if (bullet.source != cell.ship) {
        // Apply damage within the area of effect
        areaOfEffect.add(cell);
        float bulletDamage = bullet.damage * Attributes.WEAPON_DAMAGE_FACTOR;
        while (!areaOfEffect.isEmpty()) {
          Cell nextCell = areaOfEffect.removeFirst();
          if (nextCell == null) {
            break;
          }
          if (!nextCell.isAlive()) {
            continue;
          }
          float hitpoints = nextCell.module.hitpoints;
          if (bulletDamage >= hitpoints) {
            bulletDamage -= hitpoints;
            ship.destroyModule(nextCell, true);
            ship.addSurroundingCellToQueue(areaOfEffect, nextCell.gridX, nextCell.gridY);
          } else {
            nextCell.module.hitpoints -= bulletDamage;
            areaOfEffect.clear();
          }
        }
        bullet.damage = 0f;
        world.effects.start(bullet.effectHit,
            bullet.body.getPosition().x, bullet.body.getPosition().y,
            ship.body.getLinearVelocity().x, ship.body.getLinearVelocity().y,
            0f);
      } else {
        // No damaging own ship
        bullet.damage = 0f;
      }
      if (bullet.damage <= 0f) {
        bullet.active = false;
        world.weaponHandler.removeBullet(bullet);
      }
    }
    areaOfEffect.clear();
  }
}
