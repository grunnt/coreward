package dev.basvs.coreward.combat.background;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class BackLayer {

  public Array<BackObject> objs = new Array<>();
  public float relSpeed = 1f, relZoom = 1f, alpha = 1f;
  public boolean wrap = true; // wraparound particles when outside of screen?

  void setup(int screenWidth, int screenHeight, Array<TextureRegion> wrapTextures) {
    objs.clear();
    int number = (int) (((screenWidth * screenHeight) / (1000000f))
        * (float) Background.DUST_OBJECTS_PER_1000PX2);
    for (int i = 0; i < number; i++) {
      BackObject o = new BackObject();
      o.texture = wrapTextures.random();
      o.position = new Vector2(
          MathUtils.random((float) -screenWidth * Background.DUST_OBJECTS_SPREAD_FACTOR,
              (float) screenWidth * Background.DUST_OBJECTS_SPREAD_FACTOR),
          MathUtils.random((float) -screenHeight
                  * Background.DUST_OBJECTS_SPREAD_FACTOR,
              (float) screenHeight * Background.DUST_OBJECTS_SPREAD_FACTOR));
      o.r = MathUtils.random(0.5f, 1f);
      o.g = MathUtils.random(0.5f, 1f);
      o.b = MathUtils.random(0.5f, 1f);
      objs.add(o);
    }
  }
}
