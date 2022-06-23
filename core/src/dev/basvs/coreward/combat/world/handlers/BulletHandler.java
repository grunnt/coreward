package dev.basvs.coreward.combat.world.handlers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.world.Bullet;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;
import dev.basvs.coreward.design.modules.WeaponDesign;
import dev.basvs.lib.MathHelper;
import dev.basvs.lib.game.View;

public class BulletHandler {

  static final int BULLET_CACHE_SIZE = 500;

  Array<Bullet> cache = new Array<>(BULLET_CACHE_SIZE);
  Array<Bullet> bullets = new Array<>(BULLET_CACHE_SIZE);

  GameWorld world;

  BodyDef bodyDef = new BodyDef();
  FixtureDef fixtureDef = new FixtureDef();
  Vector2 tmpVec = new Vector2();
  OwnShipRayCastCallback rcCallback = new OwnShipRayCastCallback();
  Vector2 p1 = new Vector2(), p2 = new Vector2();
  Vector2 v = new Vector2();

  public BulletHandler(GameWorld world) {
    this.world = world;
    for (int i = 0; i < BULLET_CACHE_SIZE; i++) {
      cache.add(new Bullet());
    }

    // Setup bullet body definition
    bodyDef.type = BodyType.DynamicBody;
    bodyDef.bullet = true;

    // Setup bullet fixture definition
    fixtureDef.density = 0.1f;
    fixtureDef.isSensor = true;
    fixtureDef.filter.categoryBits = Constants.FRIENDLY_BULLET_CATEGORY_BITS;
    fixtureDef.filter.maskBits = Constants.ENEMY_BULLET_MASK_BITS;
    PolygonShape shape = new PolygonShape();
    shape.set(
        new Vector2[]{new Vector2(-0.1f, 0.7f), new Vector2(0.1f, 0.7f), new Vector2(0, 0.3f)});
    fixtureDef.shape = shape;
  }

  public void update(float delta) {
    for (int i = 0; i < bullets.size; i++) {
      Bullet bullet = bullets.get(i);
      bullet.lifeTime -= delta;
      if (bullet.lifeTime < 0f) {
        removeBullet(bullet);
      }
    }
  }

  public void render(View view) {
    for (int i = 0; i < bullets.size; i++) {
      Bullet bullet = bullets.get(i);
      TextureRegion tex = bullet.texture;
      float x = bullet.body.getWorldCenter().x * Constants.WORLD_TO_SCREEN;
      float y = bullet.body.getWorldCenter().y * Constants.WORLD_TO_SCREEN;
      float angle = bullet.body.getAngle();
      view.batch.draw(tex, x - tex.getRegionWidth() / 2, y - tex.getRegionHeight() / 2,
          tex.getRegionWidth() / 2,
          tex.getRegionHeight() / 2, tex.getRegionWidth(), tex.getRegionHeight(), 1f, 1f, angle
              * MathUtils.radiansToDegrees);
    }
  }

  public void removeBullet(Bullet bullet) {
    world.physics.remove(bullet.body);
    bullet.body = null;
    bullets.removeValue(bullet, true);
    cache.add(bullet);
  }

  public boolean fireBullet(Ship ship, Cell cell, boolean forceFire) {
    // Get direction to fire in
    float globalAim = cell.weaponAimGlobal();
    float acc = 1f - cell.module.design.weapon.accuracy;
    float acuracyDelta = (MathUtils.random(acc) - acc / 2f) * Attributes.MIN_ACCURACY_DEG;
    float fireAngle = globalAim + acuracyDelta - 90f;
    // Is the aim sufficient to hit the aimpoint?
    float angleDelta = MathHelper.getRelativeAngle(globalAim, cell.t.targetGlobalAngle);
    boolean canAim =
        Math.abs(angleDelta) < cell.module.design.weapon.aim / 2f;
    if (canAim || forceFire) {
      // Get bullet from cache
      if (cache.size == 0) {
        for (int i = 0; i < BULLET_CACHE_SIZE / 2; i++) {
          cache.add(new Bullet());
        }
      }
      Bullet bullet = cache.pop();
      bullets.add(bullet);

      // Create bullet body
      bodyDef.position.set(cell.t.muzzlePoint);
      bodyDef.angle = fireAngle * MathUtils.degreesToRadians;
      Body bulletBody = world.physics.getPhysicsWorld().createBody(bodyDef);

      // Add fixture to body
      Fixture f = bulletBody.createFixture(fixtureDef);
      switch (ship.alignment) {
        case Friendly -> {
          f.getFilterData().categoryBits = Constants.FRIENDLY_BULLET_CATEGORY_BITS;
          f.getFilterData().maskBits = Constants.FRIENDLY_BULLET_MASK_BITS;
        }
        case Enemy -> {
          f.getFilterData().categoryBits = Constants.ENEMY_BULLET_CATEGORY_BITS;
          f.getFilterData().maskBits = Constants.ENEMY_BULLET_MASK_BITS;
        }
      }
      f.setUserData(bullet);

      // Make bullet move by applying impulse and adding the velocity of the ship itself
      bulletBody.setLinearVelocity(new Vector2(0f, (0.05f + cell.module.design.weapon.speed * 0.95f)
          * Attributes.MAX_BULLET_VELOCITY).rotate(fireAngle).add(ship.body.getLinearVelocity()));

      // Set the bullet properties
      WeaponDesign pWeapon = cell.module.design.weapon;
      bullet.set(pWeapon.damage, pWeapon.areaOfEffect, pWeapon.lifeTime, cell.module.bulletTexture,
          bulletBody, ship,
          pWeapon.effectHit);

      return true;
    }
    return false;
  }
}
