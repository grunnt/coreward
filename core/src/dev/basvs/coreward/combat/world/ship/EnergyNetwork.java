package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.utils.Array;

public class EnergyNetwork {

  public Array<Cell> eGenerators = new Array<>();
  public Array<Cell> eBuffers = new Array<>();
  public Array<Cell> eConsumers = new Array<>();

  public float epsGenerated;
  public float epsConsumed;
  public float eBufferSize;
  public float eBuffered;
  public float effectiveness;

  public void remove(Cell cell) {
    eGenerators.removeValue(cell, true);
    eBuffers.removeValue(cell, true);
    eConsumers.removeValue(cell, true);
  }
}
