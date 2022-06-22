package dev.basvs.coreward.design.modules;

public class ModuleDesign {

  public String code;
  public String name;

  public String texture;
  public boolean triangular;
  public int freeSpace;

  public float hitpoints;
  public float density;
  public boolean core;
  public boolean passable;

  public WeaponDesign weapon;
  public ThrusterDesign thruster;
  public ReactionWheelDesign reactionWheel;

  public EnergyConduitDesign conduit;
  public EnergyBufferDesign buffer;
  public EnergyConsumerDesign consumer;
  public EnergyProducerDesign producer;

  public float commonCost;
  public float rareCost;
  public float exoticCost;

  public String effectDestroyed;

  public boolean isEnergyConductor() {
    return conduit != null || buffer != null || consumer != null || producer != null || core;
  }
}
