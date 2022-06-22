package dev.basvs.coreward.combat.world.ship;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.basvs.coreward.design.modules.ModuleDesign;

public class Module {

  public ModuleDesign design;

  public TextureRegion texture;
  public TextureRegion[] conduitTexture;
  public TextureRegion bufferTexture;
  public TextureRegion bulletTexture;
  public TextureRegion nozzleTexture;

  public float hitpoints;

  public float localBuffered;

  public float weaponLocalAim;
  public float burstTime;
  public int burst;
}
