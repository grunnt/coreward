package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.utils.Array;

public class ShipTemp {

  public int aliveModuleCount, designModuleCount;
  public Cell core;
  public Array<Cell> allCells = new Array<>();
  public Array<Cell> thrusters = new Array<>();
  public Array<Cell> reactionWheels = new Array<>();
  public Array<EnergyNetwork> eNetworks = new Array<>();
  public Array<Cell> weapons = new Array<>();
  public Array<Cell> generators = new Array<>();
}
