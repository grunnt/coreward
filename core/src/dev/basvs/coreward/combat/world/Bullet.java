package dev.basvs.coreward.combat.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import dev.basvs.coreward.combat.world.ship.Ship;

public class Bullet {

  public boolean active;
  public Body body;
  public float damage;
  public TextureRegion texture;
  public String effectHit;
  public Vector2 lastPosition = new Vector2();
  public float lifeTime;
  public Ship source;

  public void set(float damage, float lifeTime, TextureRegion texture, Body body, Ship source,
      String effectHit) {
    active = true;
    lastPosition.set(body.getPosition());
    this.damage = damage;
    this.lifeTime = lifeTime;
    this.texture = texture;
    this.body = body;
    this.source = source;
    this.effectHit = effectHit;
  }
}
