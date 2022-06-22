package dev.basvs.coreward;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class AssetList {

  public static void addAssetsTo(AssetManager assets) {
    addTextures(assets);
    addFonts(assets);
    addSounds(assets);
  }

  private static void addTextures(AssetManager assets) {
    assets.load("background/star1.png", Texture.class);
    assets.load("background/star2.png", Texture.class);
    assets.load("background/planet1.png", Texture.class);
    assets.load("background/planet2.png", Texture.class);
    assets.load("coreward.atlas", TextureAtlas.class);
  }

  private static void addFonts(AssetManager assets) {
    assets.load("azertype14.fnt", BitmapFont.class);
    assets.load("azertype24.fnt", BitmapFont.class);
    assets.load("azertype32.fnt", BitmapFont.class);
  }

  private static void addSounds(AssetManager assets) {
  }
}
