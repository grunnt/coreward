package dev.basvs.coreward.combat.effects;

import com.badlogic.gdx.utils.Array;

/**
 * A particle effect consisting of a set of particle emitters.
 */
public class ParticleEffect {

  public String name;
  public Array<ParticleEmitterDef> emitters = new Array<>();
}
