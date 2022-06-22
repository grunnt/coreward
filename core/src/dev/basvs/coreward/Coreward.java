package dev.basvs.coreward;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import dev.basvs.lib.game.AbstractGame;

public class Coreward extends AbstractGame {

  public Coreward(String runMode) {
    super();

    switch (runMode) {
      case "particle-editor":
        Attributes.START_MODE = StartMode.ParticleEditor;
    }
  }

  @Override
  public void onCreate() throws Exception {
    Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, 15, 4));
    LoadingScreen loadingScreen = new LoadingScreen(this);
    activateScreen(loadingScreen);
  }

  @Override
  public void onResize(int width, int height) throws Exception {
    // Do nothing
  }

  @Override
  public void onPause() throws Exception {
    // Do nothing
  }

  @Override
  public void onResume() throws Exception {
    // Do nothing
  }

  @Override
  public void onDispose() throws Exception {
    // Do nothing
  }
}
