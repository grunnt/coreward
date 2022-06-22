package dev.basvs.lib;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class DoubleTextureRegionDrawable extends TextureRegionDrawable {

  TextureRegion regionTop;

  public DoubleTextureRegionDrawable(TextureRegion regionBottom, TextureRegion regionTop) {
    super(regionBottom);
    this.regionTop = regionTop;
  }

  @Override
  public void draw(Batch batch, float x, float y, float width, float height) {
    super.draw(batch, x, y, width, height);
    batch.draw(regionTop, x, y, width, height);
  }

  @Override
  public void draw(Batch batch, float x, float y, float originX, float originY, float width,
      float height,
      float scaleX, float scaleY, float rotation) {
    super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    batch.draw(regionTop, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
  }
}
