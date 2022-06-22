package dev.basvs.coreward.combat.world.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.EnergyNetwork;
import dev.basvs.coreward.combat.world.ship.Module;
import dev.basvs.coreward.combat.world.ship.Ship;

public class ShipRenderer {

  Matrix4 m4 = new Matrix4();

  public void drawShip(Ship ship, SpriteBatch batch) {
    float shipX = ship.body.getPosition().x * Constants.WORLD_TO_SCREEN;
    float shipY = ship.body.getPosition().y * Constants.WORLD_TO_SCREEN;
    float angle = ship.body.getAngle();
    m4.setToTranslation(shipX, shipY, 0);
    m4.rotateRad(0, 0, 1, angle);
    batch.setTransformMatrix(m4);
    int designWidthPx = ship.design.width * Constants.CELL_SIZE_PX;
    int designHeightPx = ship.design.height * Constants.CELL_SIZE_PX;
    for (int x = 0; x < ship.design.width; x++) {
      for (int y = 0; y < ship.design.height; y++) {
        Cell cell = ship.cells[x][y];
        if (cell.isAlive()) {
          Module module = cell.module;
          TextureRegion tex = module.texture;
          float healthFactor = 0.25f + 0.75f * (module.hitpoints / module.design.hitpoints);
          batch.setColor(healthFactor, healthFactor, healthFactor, 1f);
          float cellX =
              x * Constants.CELL_SIZE_PX - Constants.CELL_SIZE_PX / 2f - designWidthPx / 2f;
          float cellY =
              y * Constants.CELL_SIZE_PX - Constants.CELL_SIZE_PX / 2f - designHeightPx / 2f;
          batch.draw(tex,
              cellX,
              cellY,
              Constants.CELL_SIZE_PX / 2f,
              Constants.CELL_SIZE_PX / 2f,
              Constants.CELL_SIZE_PX,
              Constants.CELL_SIZE_PX,
              1f,
              1f,
              ship.cells[x][y].cellDirection());
          if (module.design.weapon != null) {
            tex = module.nozzleTexture;
            batch.draw(tex,
                cellX,
                cellY,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX,
                Constants.CELL_SIZE_PX,
                1f,
                1f,
                cell.module.weaponLocalAim - 90f);
          }
          if (module.design.conduit != null && ship.cells[x][y].t.eNetwork != null) {
            EnergyNetwork eNetwork = ship.cells[x][y].t.eNetwork;
            batch.setColor(0.5f + 0.5f * eNetwork.effectiveness,
                0.5f + 0.5f * eNetwork.effectiveness, 0.5f + 0.5f * eNetwork.effectiveness, 1f);
            byte index = 0;
            index = (byte) (index | (isConduit(x, y + 1, ship)));
            index = (byte) (index | (isConduit(x + 1, y, ship) << 1));
            index = (byte) (index | (isConduit(x, y - 1, ship) << 2));
            index = (byte) (index | (isConduit(x - 1, y, ship) << 3));
            batch.draw(module.conduitTexture[index],
                cellX,
                cellY,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX,
                Constants.CELL_SIZE_PX,
                1f,
                1f,
                0f);
          } else if (module.design.buffer != null && ship.cells[x][y].t.eNetwork != null) {
            EnergyNetwork eNetwork = ship.cells[x][y].t.eNetwork;
            float lightness = 0.5f + 0.5f * (eNetwork.eBuffered / eNetwork.eBufferSize);
            batch.setColor(lightness, lightness, lightness, 1f);
            batch.draw(module.bufferTexture,
                cellX,
                cellY,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX / 2f,
                Constants.CELL_SIZE_PX,
                Constants.CELL_SIZE_PX,
                1f,
                1f,
                ship.cells[x][y].cellDirection());
          }
        }
      }
    }
    batch.setColor(Color.WHITE);
    m4.idt();
    batch.setTransformMatrix(m4);
  }

  byte isConduit(int gridX, int gridY, Ship ship) {
    if (gridX >= 0 && gridX < ship.design.width && gridY >= 0 && gridY < ship.design.height) {
      if (ship.cells[gridX][gridY].isAlive()
          && ship.design.cells[gridX][gridY].module.isEnergyConductor()) {
        return 1;
      }
    }
    return 0;
  }
}
