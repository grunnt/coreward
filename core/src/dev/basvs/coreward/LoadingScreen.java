package dev.basvs.coreward;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.basvs.coreward.console.Console;
import dev.basvs.coreward.console.attributes.AngularDampeningAttr;
import dev.basvs.coreward.console.attributes.CollisionDamageFactorAttr;
import dev.basvs.coreward.console.attributes.DebugAIAttr;
import dev.basvs.coreward.console.attributes.DebugBox2DAttr;
import dev.basvs.coreward.console.attributes.DebugScene2DAttr;
import dev.basvs.coreward.console.attributes.LinearDampeningAttr;
import dev.basvs.coreward.console.attributes.MassFactorAttr;
import dev.basvs.coreward.console.attributes.MaxAimDegAttr;
import dev.basvs.coreward.console.attributes.MinAccuracyDegAttr;
import dev.basvs.coreward.console.attributes.ShowFPSAttr;
import dev.basvs.coreward.console.attributes.ThrustFactorAttr;
import dev.basvs.coreward.console.attributes.TorqueFactorAttr;
import dev.basvs.coreward.console.attributes.WeaponDamageFactorAttr;
import dev.basvs.coreward.design.ShipDesignScreen;
import dev.basvs.lib.Logging;
import dev.basvs.lib.game.AbstractGame;
import dev.basvs.lib.game.AbstractScreen;
import dev.basvs.tools.EffectMakerScreen;

public class LoadingScreen extends AbstractScreen {

  static final float PROGRESS_BAR_WIDTH = 200;

  boolean loadingDone = false;

  Texture loadingTexture;
  NinePatch barTexture;

  public LoadingScreen(AbstractGame game) {
    super(game);

    setLoadingScreen(true);
    setRemoveAfterFadeOut(true);

    loadingTexture = new Texture(Gdx.files.internal("loading.png"));
    Texture tex = new Texture(Gdx.files.internal("loading-bar.png"));
    tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    barTexture = new NinePatch(tex, 1, 1, 0, 0);
  }

  @Override
  public void onActivate() {
    // Queue up the assets to load
    AssetList.addAssetsTo(game.getAssets());
  }

  @Override
  public void onUpdate(float delta) throws Exception {
    if (game.getAssets().update() && !loadingDone) {

      Logging.info("Assests loaded");

      // Setup gui style
      TextureAtlas atlas = game.getAssets().get("coreward.atlas", TextureAtlas.class);
      game.guiSkin = new Skin(Gdx.files.internal("skin.json"), atlas);

      game.console = new Console(game.guiSkin);
      game.console.addAttribute(new AngularDampeningAttr());
      game.console.addAttribute(new LinearDampeningAttr());
      game.console.addAttribute(new ThrustFactorAttr());
      game.console.addAttribute(new TorqueFactorAttr());
      game.console.addAttribute(new MassFactorAttr());
      game.console.addAttribute(new CollisionDamageFactorAttr());
      game.console.addAttribute(new WeaponDamageFactorAttr());
      game.console.addAttribute(new MinAccuracyDegAttr());
      game.console.addAttribute(new MaxAimDegAttr());
      game.console.addAttribute(new DebugBox2DAttr());
      game.console.addAttribute(new DebugScene2DAttr());
      game.console.addAttribute(new DebugAIAttr());
      game.console.addAttribute(new ShowFPSAttr());

      if (Constants.EFFECT_MAKER) {
        EffectMakerScreen emScreen = new EffectMakerScreen(game);
        game.activateScreen(emScreen);
      } else {
        ShipDesignScreen designScreen = new ShipDesignScreen(game);
        game.activateScreen(designScreen);
      }
      loadingDone = true;
    }
  }

  @Override
  public void onRender(float timeDeltaSeconds) throws Exception {
    // Get resource loading progress
    float progress = game.getAssets().getProgress();
    screenView.begin();
    screenView.batch.setColor(Color.DARK_GRAY);
    float barY = Gdx.graphics.getHeight() / 2 - loadingTexture.getHeight() / 2 - 30;
    barTexture.draw(screenView.batch, Gdx.graphics.getWidth() / 2 - PROGRESS_BAR_WIDTH / 2, barY,
        PROGRESS_BAR_WIDTH,
        16);
    screenView.batch.setColor(Color.WHITE);
    barTexture.draw(screenView.batch, Gdx.graphics.getWidth() / 2 - PROGRESS_BAR_WIDTH / 2, barY,
        PROGRESS_BAR_WIDTH
            * progress, 16);
    screenView.batch.draw(loadingTexture,
        Gdx.graphics.getWidth() / 2 - loadingTexture.getWidth() / 2,
        Gdx.graphics.getHeight() / 2 - loadingTexture.getHeight() / 2);
    screenView.end();
  }

  @Override
  public void onDispose() throws Exception {
    loadingTexture.dispose();
    barTexture.getTexture().dispose();
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    return false;
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {

  }
}
