package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.combat.world.Alignment;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.design.ShipDesign;
import dev.basvs.coreward.design.modules.ModuleDesign;

public class Ship {

  public String name;
  public ShipDesign design;
  public Alignment alignment;

  public boolean destroyed = false;

  public Cell[][] cells;
  public Body body;
  public Vector2 centerOfMass;
  public float mass;
  public GameWorld world;

  public ShipTemp temp = new ShipTemp();

  Vector2 tmpV = new Vector2();

  public void update(float delta) {
    // Add some light dampening to make bodies tend to go to rest to improve performance
    body.setLinearDamping(Attributes.LINEAR_DAMPENING);
    body.setAngularDamping(Attributes.ANGULAR_DAMPENING);
    // Limit maximum speed and rotation
    float vel = body.getLinearVelocity().len();
    if (vel > Attributes.MAX_SHIP_VELOCITY) {
      body.setLinearVelocity(body.getLinearVelocity().scl(Attributes.MAX_SHIP_VELOCITY / vel));
    }
    float angVel = body.getAngularVelocity();
    if (angVel > Attributes.MAX_SHIP_ANGULAR_VELOCITY) {
      body.setAngularVelocity(Attributes.MAX_SHIP_ANGULAR_VELOCITY);
    }
    if (angVel < -Attributes.MAX_SHIP_ANGULAR_VELOCITY) {
      body.setAngularVelocity(-Attributes.MAX_SHIP_ANGULAR_VELOCITY);
    }

    // Update energy production and consumption
    for (int i = 0; i < temp.eNetworks.size; i++) {
      EnergyNetwork eNetwork = temp.eNetworks.get(i);
      eNetwork.epsConsumed = 0f;
      for (int j = 0; j < eNetwork.eConsumers.size; j++) {
        Cell consumer = eNetwork.eConsumers.get(j);
        if (consumer.isAlive()) {
          if (consumer.module.design.consumer.localBufferSize > 0f) {
            // Claim energy for filling up the local buffer
            float energyConsumed = Math.min(consumer.module.design.consumer.energyPerSecond,
                consumer.module.design.consumer.localBufferSize - consumer.module.localBuffered);
            eNetwork.epsConsumed += energyConsumed;
          } else if (consumer.isPrimaryActive() || consumer.isSecondaryActive()) {
            // Do continuous consumption if active
            eNetwork.epsConsumed += consumer.module.design.consumer.energyPerSecond;
          }
        }
      }
      eNetwork.eBuffered += eNetwork.epsGenerated * delta;
      float consumption = eNetwork.epsConsumed * delta;
      if (consumption <= eNetwork.eBuffered) {
        eNetwork.eBuffered -= consumption;
        eNetwork.effectiveness = 1f;
      } else if (consumption > 0f) {
        eNetwork.effectiveness = eNetwork.eBuffered / consumption;
        eNetwork.eBuffered = 0f;
      }
      if (eNetwork.eBuffered > eNetwork.eBufferSize) {
        eNetwork.eBuffered = eNetwork.eBufferSize;
      }
      // Put consumed energy in the local buffers
      for (int j = 0; j < eNetwork.eConsumers.size; j++) {
        Cell consumer = eNetwork.eConsumers.get(j);
        if (consumer.isAlive() && consumer.module.design.consumer.localBufferSize > 0f) {
          float energyConsumed = Math.min(
              consumer.module.design.consumer.energyPerSecond * eNetwork.effectiveness
                  * delta,
              consumer.module.design.consumer.localBufferSize - consumer.module.localBuffered);
          consumer.module.localBuffered += energyConsumed;
          if (consumer.module.localBuffered > consumer.module.design.consumer.localBufferSize) {
            consumer.module.localBuffered = consumer.module.design.consumer.localBufferSize;
          }
        }
      }
    }
  }

  public void setQuickLists() {
    for (int y = 0; y < design.height; y++) {
      for (int x = 0; x < design.width; x++) {
        Cell c = cells[x][y];
        if (c.module != null) {

          temp.allCells.add(c);
          if (c.module.design.core) {
            temp.core = c;
          }
          if (c.module.design.thruster != null) {
            temp.thrusters.add(c);
          }
          if (c.module.design.reactionWheel != null) {
            temp.reactionWheels.add(c);
          }
          if (c.module.design.producer != null) {
            temp.generators.add(c);
          }
          if (c.module.design.weapon != null) {
            temp.weapons.add(c);
          }
        }
      }
    }
  }

  // TODO: separate generateModel and updateModel
  public void rebuildEnergyNetworks() {
    // Build energy networks starting at the generators
    Array<Cell> generators = new Array<>(temp.generators);
    while (generators.size > 0) {
      Cell generator = generators.pop();
      if (generator.isAlive() && generator.t.eNetwork == null) {
        EnergyNetwork eNetwork = buildNetwork(generator);
        temp.eNetworks.add(eNetwork);
      }
    }
  }

  EnergyNetwork buildNetwork(Cell startingCell) {
    EnergyNetwork eNetwork = new EnergyNetwork();
    Array<Cell> queue = new Array<>();
    queue.add(startingCell);
    while (queue.size > 0) {
      Cell cell = queue.pop();
      // Is this an energy cell?
      if (cell.isAlive() && cell.t.eNetwork == null && cell.module.design.isEnergyConductor()) {
        ModuleDesign mDesign = cell.module.design;
        // Add surrounding cells to queue to examine whether these are connected as well
        addSurroundingCells(queue, cell.gridX, cell.gridY);
        // Add to totals of network
        if (mDesign.producer != null) {
          eNetwork.eGenerators.add(cell);
          eNetwork.epsGenerated += mDesign.producer.energyPerSecond;
        }
        if (mDesign.consumer != null) {
          eNetwork.eConsumers.add(cell);
          eNetwork.epsConsumed += mDesign.consumer.energyPerSecond;
        }
        if (mDesign.buffer != null) {
          eNetwork.eBuffers.add(cell);
          eNetwork.eBufferSize += mDesign.buffer.capacity;
          eNetwork.eBuffered += cell.t.bufferedEnergy;
        }
        cell.t.eNetwork = eNetwork;
      }
    }
    return eNetwork;
  }

  public void addModule(Cell cell, ModuleDesign moduleDesign, int direction, TextureAtlas atlas) {
    temp.aliveModuleCount++;
    cell.t.enabled = true;
    cell.direction = direction;
    cell.module = new Module();
    cell.module.design = moduleDesign;
    cell.module.hitpoints = moduleDesign.hitpoints;
    cell.module.texture = atlas.findRegion(moduleDesign.texture);
    if (moduleDesign.conduit != null) {
      cell.module.conduitTexture = new TextureRegion[16];
      for (int i = 0; i < 16; i++) {
        cell.module.conduitTexture[i] = atlas.findRegion(moduleDesign.conduit.texture, i);
      }
    }
    if (moduleDesign.buffer != null) {
      cell.module.bufferTexture = atlas.findRegion(moduleDesign.buffer.texture);
    }
    if (moduleDesign.weapon != null) {
      cell.module.bulletTexture = atlas.findRegion(moduleDesign.weapon.bulletTexture);
      cell.module.nozzleTexture = atlas.findRegion(moduleDesign.weapon.nozzleTexture);
      cell.module.weaponLocalAim = cell.cellDirection() + 90f;
    }
  }

  public void destroyModule(Cell cell, boolean rootCell) {
    temp.aliveModuleCount--;
    boolean destroyShip = false;
    String destroyedEffect = cell.module.design.effectDestroyed;
    if (cell.module.design.core) {
      destroyShip = true;
    }
    // Remove the cell itself
    world.physics.remove(cell.t.fixture);
    cell.direction = 0;
    cell.t.enabled = false;
    cell.module = null;
    cell.t.primaryControl = false;
    cell.t.secondaryControl = false;
    cell.t.eNetwork = null;
    cell.t.fixture = null;
    tmpV = toWorld(tmpV.set(cell.gridX, cell.gridY));
    world.effects.start(destroyedEffect, tmpV.x, tmpV.y, cell.ship.body.getLinearVelocity().x,
        cell.ship.body.getLinearVelocity().y, 0f);
    if (destroyShip) {
      // The ship is destroyed
      for (int i = 0; i < temp.allCells.size; i++) {
        Cell c = temp.allCells.get(i);
        if (c.isAlive() && c != cell) {
          destroyModule(c, false);
        }
      }
      world.physics.remove(body);
      // Flag this ship as destroyed, so it will be removed the next update
      destroyed = true;
    } else {
      // Check for cells that got separated from the core?
      if (rootCell) {
        // Flag all modules connected to the core
        clearVisitedFlags();
        Array<Cell> queue = new Array<>();
        queue.add(temp.core);
        while (queue.size > 0) {
          Cell c = queue.pop();
          if (!c.t.visitFlag & c.isAlive()) {
            c.t.visitFlag = true;
            addSurroundingCells(queue, c.gridX, c.gridY);
          }
        }
        // Destroy any module that is not flagged
        for (int i = 0; i < temp.allCells.size; i++) {
          Cell c = temp.allCells.get(i);
          if (!c.t.visitFlag && c.module != null) {
            destroyModule(c, false);
          }
        }
        // Recalculate energy networks.
        for (int i = 0; i < temp.eNetworks.size; i++) {
          EnergyNetwork eNet = temp.eNetworks.get(i);
          // First remember all buffered energy per buffer
          float energyPerBuffer = eNet.eBufferSize > 0 ? eNet.eBuffered / eNet.eBufferSize : 0f;
          for (int j = 0; j < eNet.eBuffers.size; j++) {
            Cell buf = eNet.eBuffers.get(j);
            buf.t.bufferedEnergy = energyPerBuffer;
          }
        }
        temp.eNetworks.clear();
        for (int i = 0; i < temp.allCells.size; i++) {
          temp.allCells.get(i).t.eNetwork = null;
        }
        rebuildEnergyNetworks();
      }
      cell.t.bufferedEnergy = 0f;
    }
  }

  void clearVisitedFlags() {
    for (int i = 0; i < temp.allCells.size; i++) {
      CellTemp t = temp.allCells.get(i).t;
      t.visitFlag = false;
    }
  }

  void addSurroundingCells(Array<Cell> cells, int gridX, int gridY) {
    addCellIfExists(cells, gridX - 1, gridY);
    addCellIfExists(cells, gridX + 1, gridY);
    addCellIfExists(cells, gridX, gridY - 1);
    addCellIfExists(cells, gridX, gridY + 1);
  }

  void addCellIfExists(Array<Cell> cells, int gridX, int gridY) {
    if (gridX > 0 && gridX < design.width && gridY > 0 && gridY < design.height) {
      Cell cell = this.cells[gridX][gridY];
      if (cell.isAlive()) {
        cells.add(cell);
      }
    }
  }

  /**
   * Get the ship body angle in degrees.
   *
   * @return
   */
  public float getAngle() {
    return body.getAngle() * MathUtils.radiansToDegrees;
  }

  /**
   * Convert a ship-local grid coordinate to a world coordinate.
   *
   * @param gridVector
   * @return
   */
  public Vector2 toWorld(Vector2 gridVector) {
    return body.getWorldPoint(
        gridVector.set(gridVector.x - design.width / 2f, gridVector.y - design.height / 2f));
  }
}
