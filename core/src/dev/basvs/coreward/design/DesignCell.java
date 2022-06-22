package dev.basvs.coreward.design;

import dev.basvs.coreward.design.modules.ModuleDesign;

public class DesignCell {

  public ModuleDesign module; // is a module placed in this cell?
  public int direction; // direction in which module is facing: 0=up, 1=right, etc.
  public boolean reserved; // reserved, e.g. because of module free space
  public int gridX, gridY;

  public boolean visited;

  public void clear() {
    module = null;
    direction = 0;
    reserved = false;
  }
}
