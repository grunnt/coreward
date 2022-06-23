package dev.basvs.coreward.design;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.basvs.coreward.design.modules.ModuleDesign;
import dev.basvs.lib.DoubleTextureRegionDrawable;

public class ModuleSelectButton extends Button {

  private ModuleDesign module;
  private Label countLabel;

  public ModuleSelectButton(ModuleDesign module, TextureAtlas atlas, Skin skin) {
    super(skin);
    this.module = module;
    Drawable drawable;
    if (module.conduit != null) {
      drawable = new DoubleTextureRegionDrawable(
          atlas.findRegion(module.texture),
          atlas.findRegion(module.conduit.texture, 0));
    } else if (module.weapon != null) {
      drawable = new DoubleTextureRegionDrawable(
          atlas.findRegion(module.texture),
          atlas.findRegion(module.weapon.nozzleTexture));
    } else {
      drawable = new TextureRegionDrawable(atlas.findRegion(module.texture));
    }
    VerticalGroup iconCountGroup = new VerticalGroup();
    iconCountGroup.addActor(new Image(drawable));
    countLabel = new Label("0", skin);
    iconCountGroup.addActor(countLabel);
    add(iconCountGroup);
  }

  public ModuleDesign getModule() {
    return module;
  }

  public void setCount(int count) {
    countLabel.setText(count);
  }
}
