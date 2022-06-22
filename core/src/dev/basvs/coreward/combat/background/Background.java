package dev.basvs.coreward.combat.background;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dev.basvs.lib.game.View;

public class Background {

  static final float DUST_OBJECTS_SPREAD_FACTOR = 1.5f;
  static final int DUST_OBJECTS_PER_1000PX2 = 40;

  TextureRegion backTile;
  Array<TextureRegion> wrapTextures;

  Array<BackLayer> layers;
  boolean screenIsLarger;
  float lastCameraX, lastCameraY, deltaCamX, deltaCamY;

  public Background(AssetManager assets, int screenWidth, int screenHeight) {

    TextureAtlas atlas = assets.get("coreward.atlas");

    backTile = atlas.findRegion("background/backtile", 1);

    wrapTextures = new Array<>();
    TextureRegion tex;
    int textCount = 1;
    do {
      tex = atlas.findRegion("background/dust", textCount++);
      if (tex != null) {
        wrapTextures.add(tex);
      }
    } while (tex != null);

    // Create several wrap layers with dust particles
    layers = new Array<>();
    BackLayer starLayer = new BackLayer();
    starLayer.objs = new Array<>();
    starLayer.relSpeed = 0.002f;
    starLayer.relZoom = 0.001f;
    starLayer.wrap = false;
    BackObject starObject = new BackObject();
    if (MathUtils.randomBoolean()) {
      starObject.bigTexture = (Texture) assets.get("background/star1.png");
    } else {
      starObject.bigTexture = (Texture) assets.get("background/star2.png");
    }
    starObject.bigTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    starObject.position = new Vector2(-300, -145);
    starLayer.objs.add(starObject);
    layers.add(starLayer);
    BackLayer planetLayer = new BackLayer();
    planetLayer.objs = new Array<>();
    planetLayer.relSpeed = 0.01f;
    planetLayer.relZoom = 0.02f;
    planetLayer.wrap = false;
    BackObject planetObject = new BackObject();
    if (MathUtils.randomBoolean()) {
      planetObject.bigTexture = (Texture) assets.get("background/planet1.png");
    } else {
      planetObject.bigTexture = (Texture) assets.get("background/planet2.png");
    }
    planetObject.bigTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    planetObject.position = new Vector2(-200, -150);
    planetLayer.objs.add(planetObject);
    layers.add(planetLayer);

    for (int i = 1; i < 6; i++) {
      layers.add(new BackLayer());
    }
    setupDustLayers(screenWidth, screenHeight);
  }

  public void render(View screenView, float cameraX, float cameraY, float zoom) {
    screenView.begin();
    renderBackTiles(screenView);
    deltaCamX = (lastCameraX - cameraX) / zoom;
    deltaCamY = (lastCameraY - cameraY) / zoom;
    for (int i = 0; i < layers.size; i++) {
      BackLayer layer = layers.get(i);
      renderLayer(layer, screenView, cameraX, cameraY, zoom);
    }
    lastCameraX = cameraX;
    lastCameraY = cameraY;
    screenView.end();
  }

  void renderBackTiles(View screenView) {
    int x = 0, y = 0;
    while (y < screenView.getScreenHeight()) {
      x = 0;
      while (x < screenView.getScreenWidth()) {
        screenView.batch.draw(backTile, x, y);
        x += backTile.getRegionWidth();
      }
      y += backTile.getRegionHeight();
    }
  }

  void renderLayer(BackLayer layer, View screenView, float cameraX, float cameraY, float zoom) {
    // Update object positions
    float top = -screenView.getScreenHeight() * DUST_OBJECTS_SPREAD_FACTOR, bottom =
        screenView.getScreenHeight()
            * DUST_OBJECTS_SPREAD_FACTOR, left =
        -screenView.getScreenWidth() * DUST_OBJECTS_SPREAD_FACTOR, right = screenView
        .getScreenWidth() * DUST_OBJECTS_SPREAD_FACTOR;
    for (int i = 0; i < layer.objs.size; i++) {
      BackObject o = layer.objs.get(i);
      o.position.add(deltaCamX * layer.relSpeed, deltaCamY * layer.relSpeed);
      if (layer.wrap) {
        if (o.position.x < left) {
          o.position.x = right;
        } else if (o.position.x > right) {
          o.position.x = left;
        }
        if (o.position.y < top) {
          o.position.y = bottom;
        } else if (o.position.y > bottom) {
          o.position.y = top;
        }
      }
    }
    // Render moving objects in the background
    for (int i = 0; i < layer.objs.size; i++) {
      BackObject o = layer.objs.get(i);
      if (layer.wrap) {
        screenView.batch.setColor(o.r, o.g, o.b, layer.alpha * 1f / zoom);
      } else {
        screenView.batch.setColor(o.r, o.g, o.b, layer.alpha);
      }
      if (o.texture != null) {
        screenView.batch.draw(o.texture, o.position.x / zoom + screenView.getScreenWidth() / 2,
            o.position.y / zoom
                + screenView.getScreenHeight() / 2);
      } else {
        float scale = 1f / (((zoom - 1f) * layer.relZoom) + 1f);
        float w = o.bigTexture.getWidth() * scale;
        float h = o.bigTexture.getHeight() * scale;
        screenView.batch.draw(o.bigTexture,
            o.position.x * scale + screenView.getScreenWidth() / 2 - w / 2,
            o.position.y * scale + screenView.getScreenHeight() / 2 - h / 2, w / 2, h / 2, w, h, 1f,
            1f, o.angle,
            0, 0, o.bigTexture.getWidth(), o.bigTexture.getHeight(), false, false);
      }
    }
    screenView.batch.setColor(Color.WHITE);
  }

  public void setupDustLayers(int screenWidth, int screenHeight) {
    for (int i = 0; i < layers.size; i++) {
      if (layers.get(i).wrap) {
        BackLayer layer = layers.get(i);
        layer.setup(screenWidth, screenHeight, wrapTextures);
        layer.relSpeed = (i + 1f) / (float) layers.size;
        layer.alpha = (i + 1f) / (float) layers.size;
      }
    }
  }
}
