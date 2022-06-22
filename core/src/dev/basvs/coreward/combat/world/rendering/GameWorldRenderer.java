package dev.basvs.coreward.combat.world.rendering;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.lib.game.View;

public class GameWorldRenderer {

  // Box2D shapes debug rendering
  Matrix4 debugMatrix = new Matrix4();
  Box2DDebugRenderer debugRenderer;

  GameWorld world;

  ShipRenderer shipRenderer;

  public GameWorldRenderer(GameWorld world) {
    this.world = world;
    shipRenderer = new ShipRenderer();
    debugRenderer = new Box2DDebugRenderer();
  }

  public void render(View view) {
    for (int s = 0; s < world.getShips().size; s++) {
      shipRenderer.drawShip(world.getShips().get(s), view.batch);
    }
    world.weaponHandler.render(view);
    // Optionally render debug shapes
    if (Attributes.DEBUG_BOX2D) {
      debugMatrix.set(view.getCamera().combined)
          .scale(Constants.WORLD_TO_SCREEN, Constants.WORLD_TO_SCREEN, 1f);
      debugRenderer.render(world.physics.getPhysicsWorld(), debugMatrix);
    }
  }
}
