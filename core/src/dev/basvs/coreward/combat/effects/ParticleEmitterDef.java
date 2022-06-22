package dev.basvs.coreward.combat.effects;

import dev.basvs.lib.FloatRange;
import java.util.ArrayList;
import java.util.List;

public class ParticleEmitterDef {

  // Size range of the particles
  public final FloatRange size = new FloatRange();
  // Color of the particles
  public final FloatRange colorR = new FloatRange();
  public final FloatRange colorG = new FloatRange();
  public final FloatRange colorB = new FloatRange();
  // Life time of the particles (from the moment the effect is started
  public final FloatRange life = new FloatRange();
  // Velocity of the particles
  public final FloatRange velocity = new FloatRange();
  // Delay before the emitter starts
  public float delay;
  // Duration of the emitter
  public float duration;
  // Number of particles to emit per second
  public float particlesPerSecond;
  // If the effect is continuous, "numberOfParticles" particles per duration will be emitted.
  public boolean continuous;
  // Effect spread along its X axis (defined by angle)
  public float spreadX;
  // Angle in which direction the particles will be launched
  public float angle;
  // Max difference from angle (+ and -) the particles will be launched
  public float angleDelta;
  // Texture of the particle
  public List<String> textureNames = new ArrayList<>();
  // Maximum number of particles
  public int particlePoolSize = 512;

  public ParticleEmitterDef copy() {
    ParticleEmitterDef def2 = new ParticleEmitterDef();
    def2.delay = delay;
    def2.duration = duration;
    def2.particlesPerSecond = particlesPerSecond;
    def2.continuous = continuous;
    def2.spreadX = spreadX;
    def2.angle = angle;
    def2.angleDelta = angleDelta;
    def2.size.set(size);
    def2.colorR.set(colorR);
    def2.colorG.set(colorG);
    def2.colorB.set(colorB);
    def2.life.set(life);
    def2.velocity.set(velocity);
    def2.textureNames = textureNames;
    def2.particlePoolSize = particlePoolSize;
    return def2;
  }
}
