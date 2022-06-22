package dev.basvs.coreward.design;

/**
 *
 */
public class ShipDesign {

  public String name;
  public int version;
  public int width, height;
  public DesignCell[][] cells;

  public ShipDesign() {
  }

  public ShipDesign(String name, int width, int height) {
    this.name = name;
    this.width = width;
    this.height = height;
    version = 1;
    cells = new DesignCell[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        cells[x][y] = new DesignCell();
        cells[x][y].gridX = x;
        cells[x][y].gridY = y;
      }
    }
  }
}
