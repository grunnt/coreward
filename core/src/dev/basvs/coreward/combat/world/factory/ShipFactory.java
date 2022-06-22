package dev.basvs.coreward.combat.world.factory;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.effects.PointParticleEffects;
import dev.basvs.coreward.combat.world.Alignment;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Module;
import dev.basvs.coreward.combat.world.ship.Ship;
import dev.basvs.coreward.design.DesignCell;
import dev.basvs.coreward.design.ShipDesign;

public class ShipFactory {

  public static Ship createShip(String name, ShipDesign design, Alignment alignment,
      TextureAtlas atlas, GameWorld world,
      float startX,
      float startY, float startAngle) {
    Ship ship = new Ship();
    ship.world = world;
    ship.name = name;
    ship.design = design;
    ship.alignment = alignment;
    ship.cells = new Cell[design.width][design.height];
    ship.centerOfMass = new Vector2();
    ship.mass = 0f;
    for (int x = 0; x < design.width; x++) {
      for (int y = 0; y < design.height; y++) {
        Cell cell = new Cell();
        ship.cells[x][y] = cell;
        cell.ship = ship;
        cell.gridX = x;
        cell.gridY = y;
        cell.t.enabled = true;
        DesignCell dCell = design.cells[x][y];
        cell.direction = dCell.direction;
        if (dCell.module != null) {
          ship.addModule(cell, dCell.module, dCell.direction, atlas);
        }
        ship.temp.designModuleCount = ship.temp.aliveModuleCount;
      }
    }
    ship.centerOfMass.scl(1f / ship.mass);
    setBody(ship, world, startX, startY, startAngle);
    setEffects(ship, world.effects, atlas);
    ship.setQuickLists();
    ship.rebuildEnergyNetworks();
    return ship;
  }

  static void setEffects(Ship ship, PointParticleEffects effects, TextureAtlas atlas) {
    for (int x = 0; x < ship.design.width; x++) {
      for (int y = 0; y < ship.design.height; y++) {
        Cell c = ship.cells[x][y];
        if (c.module != null && c.module.design.thruster != null) {
          // This thruster needs a thruster effect
          effects.start("thruster", c);
        }
      }
    }
  }

  static boolean[] shiftArrayRight(boolean[] array, int amount) {
    boolean[] result = new boolean[array.length];
    System.arraycopy(array, 0, result, amount, array.length - amount);
    System.arraycopy(array, array.length - amount, result, 0, amount);
    return result;
  }

  static void setBody(Ship ship, GameWorld world, float x, float y, float angle) {
    // Create physics body for this part
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DynamicBody;
    bodyDef.angularDamping = Attributes.ANGULAR_DAMPENING;
    bodyDef.linearDamping = Attributes.LINEAR_DAMPENING;
    bodyDef.position.set(x, y);
    bodyDef.angle = angle;
    ship.body = world.physics.getPhysicsWorld().createBody(bodyDef);
    ship.body.setUserData(ship);
    // Give body a shape
    generateFixtures(ship);
  }

  static void generateFixtures(Ship ship) {
    ShipDesign design = ship.design;
    for (int y = 0; y < design.height; y++) {
      for (int x = 0; x < design.width; x++) {
        Module module = ship.cells[x][y].module;
        if (module != null) {
          // TODO reuse fixturedef
          FixtureDef fixtureDef = new FixtureDef();
          PolygonShape shape = new PolygonShape();
          setShape(shape, x, y, design);
          switch (ship.alignment) {
            case Friendly -> {
              fixtureDef.filter.categoryBits = Constants.FRIENDLY_SHIP_CATEGORY_BITS;
              fixtureDef.filter.maskBits = Constants.FRIENDLY_SHIP_MASK_BITS;
            }
            case Enemy -> {
              fixtureDef.filter.categoryBits = Constants.ENEMY_SHIP_CATEGORY_BITS;
              fixtureDef.filter.maskBits = Constants.ENEMY_SHIP_MASK_BITS;
            }
          }

          fixtureDef.shape = shape;
          fixtureDef.density = module.design.density;
          fixtureDef.friction = 0.25f;
          fixtureDef.restitution = 0.25f;
          Fixture f = ship.body.createFixture(fixtureDef);
          f.setUserData(ship.cells[x][y]);
          ship.cells[x][y].t.fixture = f;
          shape.dispose();
        }
      }
    }
  }

  static void setShape(PolygonShape shape, int gridX, int gridY, ShipDesign design) {
    float left = (gridX - 0.5f - design.width / 2f);
    float right = (gridX + 0.5f - design.width / 2f);
    float bottom = (gridY - 0.5f - design.height / 2f);
    float top = (gridY + 0.5f - design.height / 2f);
    DesignCell cell = design.cells[gridX][gridY];
    if (cell.module.triangular) {
      switch (cell.direction) {
        case 0:
          shape.set(new Vector2[]{new Vector2(left, top), new Vector2(right, top),
              new Vector2(right, bottom)});
          break;
        case 1:
          shape.set(new Vector2[]{new Vector2(right, top), new Vector2(right, bottom),
              new Vector2(left, bottom)});
          break;
        case 2:
          shape.set(new Vector2[]{new Vector2(left, top), new Vector2(right, bottom),
              new Vector2(left, bottom)});
          break;
        case 3:
          shape.set(new Vector2[]{new Vector2(left, top), new Vector2(right, top),
              new Vector2(left, bottom)});
          break;
      }
    } else {
      shape.set(
          new Vector2[]{new Vector2(left, top), new Vector2(right, top), new Vector2(right, bottom),
              new Vector2(left, bottom)});
    }
  }
}
