package dev.basvs.coreward.combat.effects;

/**
 * A source of particle emissions. This allows particle emitters to act dynamically, e.g. by moving,
 * rotating or enabling / disabling based on the state of some object in the world.
 */
public interface ParticleEmitterSource {

  /**
   * X position where the particles should be emitted.
   *
   * @return
   */
  float getEmitterX();

  /**
   * Y position where the particles should be emitted.
   *
   * @return
   */
  float getEmitterY();

  /**
   * X velocity of the particle source. This will be added to the particle's verlocity.
   *
   * @return
   */
  float getEmitterVelocityX();

  /**
   * Y velocity of the particle source. This will be added to the particle's verlocity.
   *
   * @return
   */
  float getEmitterVelocityY();

  /**
   * Angle in radians where to the particles should be emitted.
   *
   * @return
   */
  float getEmitterAngle();

  /**
   * Is this particle source active? In other words, should particles be emitted? This will be
   * ignored if the particle source is no longer alive.
   *
   * @return
   */
  boolean isEmitterEnabled();

  /**
   * Is the particle source "alive"? If not, the particle emitter will be cleaned up after the last
   * emitted particles have ended their life.
   *
   * @return
   */
  boolean isAlive();
}
