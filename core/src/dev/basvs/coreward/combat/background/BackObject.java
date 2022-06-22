package dev.basvs.coreward.combat.background;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class BackObject {

  public TextureRegion texture;
  public Texture bigTexture; // If texture is null, this contains a high resolution texture
  public Vector2 position;
  public float angle = 0f;
  public float r = 1f, g = 1f, b = 1f;
}
