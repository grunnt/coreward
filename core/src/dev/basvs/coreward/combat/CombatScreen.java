package dev.basvs.coreward.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.background.Background;
import dev.basvs.coreward.combat.control.AIControl;
import dev.basvs.coreward.combat.control.PlayerControl;
import dev.basvs.coreward.combat.effects.PointParticleEffects;
import dev.basvs.coreward.combat.gamegui.ShipStatusGui;
import dev.basvs.coreward.combat.world.Alignment;
import dev.basvs.coreward.combat.world.GameWorld;
import dev.basvs.coreward.combat.world.rendering.GameWorldRenderer;
import dev.basvs.coreward.combat.world.ship.Ship;
import dev.basvs.coreward.design.ShipDesign;
import dev.basvs.lib.game.AbstractGame;
import dev.basvs.lib.game.AbstractScreen;
import java.io.IOException;

public class CombatScreen extends AbstractScreen {

  static final String BOTTOM_TEXT = "Coreward prototype - combat, 2022";

  // Text rendering
  private final BitmapFont font14;
  private final GlyphLayout textBounds = new GlyphLayout();
  private final StringBuilder sb = new StringBuilder();
  // Game world
  private final GameWorld world;
  private final Ship playerShip;
  private final Array<AIControl> ais = new Array<>();
  private final Background back;
  private final PointParticleEffects effects;
  private final GameWorldRenderer worldRenderer;
  // Controls
  private final PlayerControl controls;
  // Game gui
  private ShipStatusGui shipStatusGui;

  public CombatScreen(AbstractGame game, ShipDesign design, ShipDesign enemyDesign)
      throws Exception {
    super(game);

    font14 = game.getAssets().get("azertype14.fnt", BitmapFont.class);

    // Game world rendering
    TextureAtlas atlas = game.getAssets().get("coreward.atlas");
    back = new Background(game.getAssets(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    effects = new PointParticleEffects(10000);

    world = new GameWorld(atlas, effects);
    worldRenderer = new GameWorldRenderer(world);

    playerShip = world.addShip("Veto", design, Alignment.Friendly, 0f, 0f, 0f);
    Ship enemy = world.addShip("Nemesis", enemyDesign, Alignment.Enemy, 0f, 50f, MathUtils.PI);
    AIControl ai = new AIControl(enemy, world);
    ais.add(ai);

    controls = new PlayerControl(playerShip);

    setupGui();
  }

  private void setupGui() {
    Table leftMenus = new Table();
    leftMenus.defaults().pad(3);
    guiRoot.add(leftMenus).expand().top().left();

    Table systemMenu = new Table();
    systemMenu.defaults().pad(3);
    guiRoot.add(systemMenu).expand().top().right();

    TextButton exitButton = new TextButton("Exit", game.guiSkin);
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setRemoveAfterFadeOut(true);
        game.deactivateScreen();
      }
    });
    systemMenu.add(exitButton);

    shipStatusGui = new ShipStatusGui(playerShip, game.guiSkin);
    leftMenus.add(shipStatusGui);
  }

  @Override
  public void onUpdate(float delta) {
    // Player
    controls.update(delta, inputWorldPos.x, inputWorldPos.y);
    // AI
    for (int i = 0; i < ais.size; i++) {
      ais.get(i).update(delta);
    }
    // Update game world
    world.update(delta);

    // Update camera position
    worldView.setPos(playerShip.body.getWorldPoint(playerShip.body.getMassData().center).scl(
        Constants.WORLD_TO_SCREEN));
    worldView.update();
    // Update gui
    shipStatusGui.update();
  }

  @Override
  public void onRender(float timeDeltaSeconds) {
    // Render background stars and objects
    back.render(screenView, worldView.getCamera().position.x, worldView.getCamera().position.y,
        worldView.getCamera().zoom);

    // Render game world
    worldView.begin();
    worldRenderer.render(worldView);
    worldView.end();
    worldView.begin();
    effects.updateAndDraw(timeDeltaSeconds, worldView.cam);
    worldView.end();

    // Render game gui elements
    screenView.begin();
    textBounds.setText(font14, BOTTOM_TEXT);
    font14.draw(screenView.batch, BOTTOM_TEXT, Gdx.graphics.getWidth() / 2 - textBounds.width / 2,
        textBounds.height + 10);

    if (Attributes.SHOW_FPS) {
      sb.setLength(0);
      sb.append(Gdx.graphics.getFramesPerSecond());
      textBounds.setText(font14, sb);
      font14.draw(screenView.batch, sb, Gdx.graphics.getWidth() / 2 - textBounds.width / 2,
          textBounds.height + 30);
    }
    screenView.end();
  }

  @Override
  public void onResize(int width, int height) {
    back.setupDustLayers(width, height);
  }

  @Override
  public boolean handleKeyDown(int keycode) {
    return controls.handleKeyDown(keycode);
  }

  @Override
  public boolean handleKeyUp(int keycode) {
    return controls.handleKeyUp(keycode);
  }

  @Override
  public boolean handleTouchDown(float worldX, float worldY, int button) {
    return controls.handleMouseDown(button);
  }

  @Override
  public boolean handleTouchUp(float worldX, float worldY, int button) {
    return controls.handleMouseUp(button);

  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    if (amountY < 0.0) {
      worldView.cam.zoom *= 0.8f;
      if (worldView.cam.zoom < 1f) {
        worldView.cam.zoom = 1f;
      }
    } else {
      worldView.cam.zoom *= 1.2f;
      if (worldView.cam.zoom > 8f) {
        worldView.cam.zoom = 8f;
      }
    }
    return true;
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {

  }
}
