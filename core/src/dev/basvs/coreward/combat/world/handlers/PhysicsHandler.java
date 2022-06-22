package dev.basvs.coreward.combat.world.handlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.combat.world.GameWorld;

public class PhysicsHandler {

  World physicsWorld;
  GameWorld world;

  Array<Body> bodyRemove = new Array<>();
  Array<Fixture> fixtureRemove = new Array<>();

  public PhysicsHandler(GameWorld world) {
    this.world = world;
    physicsWorld = new World(new Vector2(0, 0), true);
    physicsWorld.setContactListener(new CollisionHandler(world));
  }

  public World getPhysicsWorld() {
    return physicsWorld;
  }

  public void update(float delta) {
    for (int i = 0; i < fixtureRemove.size; i++) {
      Fixture fixture = fixtureRemove.get(i);
      Body body = fixture.getBody();
      body.destroyFixture(fixture);
    }
    fixtureRemove.clear();
    for (int i = 0; i < bodyRemove.size; i++) {
      Body body = bodyRemove.get(i);
      physicsWorld.destroyBody(body);
    }
    bodyRemove.clear();

    physicsWorld.step(delta, 6, 2);
  }

  public void remove(Body body) {
    bodyRemove.add(body);
  }

  public void remove(Fixture fixture) {
    fixtureRemove.add(fixture);
  }
}
