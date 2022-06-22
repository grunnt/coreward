package dev.basvs.coreward.design;

import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.design.modules.ModuleDesign;

public class Requirements {

  public boolean core, forwardThrust, rotation, oneShape;

  public boolean allOk() {
    return core && forwardThrust && rotation && oneShape;
  }

  public void check(ShipDesign design) {
    // Check requirements
    core = false;
    forwardThrust = false;
    rotation = false;
    oneShape = true;
    DesignCell coreCell = prepareFillSearch(design);
    if (coreCell != null) {
      core = true;
      // Do a flood fill search from the core outward to find all attached modules
      Array<DesignCell> queue = new Array<>();
      queue.add(coreCell);
      while (queue.size > 0) {
        DesignCell cell = queue.pop();
        if (!cell.visited) {
          addSurroundingCells(design, queue, cell.gridX, cell.gridY);
          if (cell.module.thruster != null && cell.direction == 0) {
            forwardThrust = true;
          }
          if (cell.module.reactionWheel != null) {
            rotation = true;
          }
          cell.visited = true;
        }
      }
    } else {
      oneShape = false;
    }
    // Check for modules not attached to core
    for (int x = 0; x < design.width; x++) {
      for (int y = 0; y < design.height; y++) {
        if (!design.cells[x][y].visited && design.cells[x][y].module != null) {
          oneShape = false;
        }
      }
    }
  }

  DesignCell prepareFillSearch(ShipDesign design) {
    DesignCell coreCell = null;
    for (int x = 0; x < design.width; x++) {
      for (int y = 0; y < design.height; y++) {
        design.cells[x][y].visited = false;
        ModuleDesign module = design.cells[x][y].module;
        if (module != null) {
          if (module.core) {
            coreCell = design.cells[x][y];
          }
        }
      }
    }
    return coreCell;
  }

  void addSurroundingCells(ShipDesign design, Array<DesignCell> cells, int gridX, int gridY) {
    addCellIfExists(design, cells, gridX - 1, gridY);
    addCellIfExists(design, cells, gridX + 1, gridY);
    addCellIfExists(design, cells, gridX, gridY - 1);
    addCellIfExists(design, cells, gridX, gridY + 1);
  }

  void addCellIfExists(ShipDesign design, Array<DesignCell> cells, int gridX, int gridY) {
    if (gridX > 0 && gridX < design.width && gridY > 0 && gridY < design.height) {
      if (design.cells[gridX][gridY].module != null) {
        cells.add(design.cells[gridX][gridY]);
      }
    }
  }
}
