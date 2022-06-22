package dev.basvs.lib.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.basvs.coreward.console.Console;
import java.util.Stack;

public abstract class AbstractGame implements ApplicationListener {

  public static final long ONE_SECOND_NS = 1000000000;
  public static final int MAX_FPS = 60;
  public static final long MAX_FRAME_DURATION_NS = 100000000;
  public static final float FADE_DURATION_S = 0.4f;

  // Global gui style
  public Skin guiSkin;
  // Console for setting attributes
  public Console console;
  // Stack of current screens
  private Stack<AbstractScreen> screens = new Stack<AbstractScreen>();
  // This screen is activated after the topmost screen faded out
  private AbstractScreen newScreen = null;
  // State of the current screen: fading in, out, or active
  private ScreenState screenState = ScreenState.FadeIn;
  // Progress when fading in or out
  private float fadeProgress = 0f;
  // Timing stuff
  private int maxUpdates = 10;
  private long lastTime = System.nanoTime();
  private boolean paused = false;
  private boolean useYield = false;
  // Asset loading
  private AssetManager assets = new AssetManager();
  // Overlay rendering for screen transitions
  private Texture overlayTexture;
  private View overlayView;

  /**
   * Get the asset manager.
   *
   * @return
   */
  public AssetManager getAssets() {
    return assets;
  }

  /**
   * Activate a new screen on top of the current screen.
   *
   * @param screen
   */
  public void activateScreen(AbstractScreen screen) {
    if (screens.isEmpty()) {
      // Fade in a new screen
      screen.onActivate();
      screens.push(screen);
      screen = null;
      screenState = ScreenState.FadeIn;
      fadeProgress = 0f;
    } else {
      // Fade out the current screen
      deactivateScreen();
      newScreen = screen;
    }
  }

  /**
   * Deactivate current screen without activating a new one.
   */
  public void deactivateScreen() {
    if (!screens.isEmpty()) {
      screens.peek().onDeactivate();
      screenState = ScreenState.FadeOut;
      fadeProgress = 1f;
      newScreen = null;
      // Stop giving input to this screen
      Gdx.input.setInputProcessor(null);
    }
  }

  /**
   * Remove a screen from the stack directly.
   */
  public void removeScreen(AbstractScreen screen) {
    if (screens.contains(screen)) {
      screen.onDeactivate();
      screens.remove(screen);
      if (newScreen == screen) {
        newScreen = null;
      }
    }
  }

  @Override
  public void create() {
    try {
      overlayView = new View();
      // Setup a black overlay texture
      setupOverlay();
      // Do user initialization
      onCreate();
      // Setup timer
      lastTime = System.nanoTime();
    } catch (Exception e) {
      Gdx.app.error("AbstractGame", "Uncaught exception in main loop, shutting down...", e);
      Gdx.app.exit();
    }
  }

  public abstract void onCreate() throws Exception;

  @Override
  public void resize(int width, int height) {
    try {
      for (AbstractScreen screen : screens) {
        screen.onResize(width, height);
      }
      if (newScreen != null) {
        newScreen.onResize(width, height);
      }
      onResize(width, height);
    } catch (Exception e) {
      Gdx.app.error("AbstractGame", "Uncaught exception in main loop, shutting down...", e);
      Gdx.app.exit();
    }
  }

  public abstract void onResize(int width, int height) throws Exception;

  @Override
  public void render() {
    try {
      long time = System.nanoTime();
      long timeDelta = Math.min(time - lastTime, MAX_FRAME_DURATION_NS);
      float timeDeltaSeconds = timeDelta / (float) ONE_SECOND_NS;
      lastTime = time;

      if (screens.isEmpty()) {

        // Clear the display
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // No screen to activate, so quit
        Gdx.app.exit();

      } else {

        // Clear the display
        Color backColor = screens.peek().backgroundColor;
        Gdx.gl.glClearColor(backColor.r, backColor.g, backColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (screenState == ScreenState.FadeOut) {
          // Fade out the current screen
          fadeProgress -= timeDeltaSeconds / FADE_DURATION_S;
          if (fadeProgress <= 0f) {
            // Done fading out
            if (screens.peek().isRemoveAfterFadeOut()) {
              // Remove this screen so we do not return here
              screens.pop().onDeactivate();
            }
            if (newScreen != null) {
              // Fade in a new screen
              newScreen.onActivate();
              screens.push(newScreen);
              newScreen = null;
              screenState = ScreenState.FadeIn;
              fadeProgress = 0f;
            } else {
              // Activate topmost screen
              if (!screens.isEmpty()) {
                screens.peek().onReactivate();
                screenState = ScreenState.FadeIn;
                fadeProgress = 0f;
              } else {
                // No screen to activate, so quit
                Gdx.app.exit();
              }
            }
          }

        } else if (screenState == ScreenState.FadeIn) {
          // Fade in the current screen
          fadeProgress += timeDeltaSeconds / FADE_DURATION_S;
          if (fadeProgress >= 1f) {
            // Done fading in, current screen is active
            screenState = ScreenState.Active;
            // Give input focus to this screen
            AbstractScreen screen = screens.peek();
            GestureDetector gDec = new GestureDetector(screen);
            // Reduce interval for long press detection
            gDec.setLongPressSeconds(0.35f);
            Gdx.input
                .setInputProcessor(
                    new InputMultiplexer(screen.guiStage, new InputMultiplexer(gDec, screen)));
          }

        }

        // Always update the topmost screen'
        if (!screens.isEmpty()) {
          if (screens.peek().isLoadingScreen()) {
            // Update the loading screen without using a fixed
            // timestep
            screens.peek().update(timeDeltaSeconds);

          } else {
            // Update the game state in capped time steps (in case
            // we're running too slow)
            int updateCount = 0;
            while (timeDelta > 0 && (maxUpdates <= 0 || updateCount < maxUpdates) && !paused) {
              // Update using a time step in seconds
              long updateTimeStep = Math.min(timeDelta, ONE_SECOND_NS / MAX_FPS);
              float updateTimeStepSeconds = updateTimeStep / (float) ONE_SECOND_NS;

              screens.peek().update(updateTimeStepSeconds);
              screens.peek().guiStage.act(Gdx.graphics.getDeltaTime());

              timeDelta -= updateTimeStep;
              updateCount++;
            }
          }
        }
      }

      // Render the topmost screen
      if (!screens.isEmpty()) {
        screens.peek().render(timeDeltaSeconds);
        overlayView.setPos(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        overlayView.update();
        // Render fade in/out overlay if needed
        if (screenState == ScreenState.FadeIn || screenState == ScreenState.FadeOut) {
          overlayView.begin();
          overlayView.batch.setColor(1f, 1f, 1f, 1f - fadeProgress);
          overlayView.batch.draw(overlayTexture, 0, 0, Gdx.graphics.getWidth(),
              Gdx.graphics.getHeight());
          overlayView.end();
        }
      }

      if (screens.isEmpty() || !screens.peek().isLoadingScreen()) {
        // Limit the maximum FPS if this is not a loading screen
        long sleepTime = Math.round((ONE_SECOND_NS / MAX_FPS) - (System.nanoTime() - lastTime));
        if (sleepTime <= 0) {
          return;
        }
        long prevTime = System.nanoTime();
        while (System.nanoTime() - prevTime <= sleepTime) {
          if (useYield) {
            Thread.yield(); // More smooth, high CPU usage
          } else {
            Thread.sleep(1); // Less smooth, lower CPU usage
          }
        }
      }

    } catch (Exception e) {
      Gdx.app.error("AbstractGame.render()", "Uncaught exception in main loop, shutting down...",
          e);
      Gdx.app.exit();
    }
  }

  @Override
  public void pause() {
    try {
      for (AbstractScreen screen : screens) {
        screen.onPause();
      }
      onPause();
    } catch (Exception e) {
      Gdx.app.error("AbstractGame", "Uncaught exception in main loop, shutting down...", e);
      Gdx.app.exit();
    }
  }

  public abstract void onPause() throws Exception;

  @Override
  public void resume() {
    try {
      // Setup a black overlay texture
      setupOverlay();
      onResume();
    } catch (Exception e) {
      Gdx.app.error("AbstractGame", "Uncaught exception in main loop, shutting down...", e);
      Gdx.app.exit();
    }
  }

  public abstract void onResume() throws Exception;

  @Override
  public void dispose() {
    try {
      for (AbstractScreen screen : screens) {
        screen.guiStage.dispose();
        screen.onDispose();
      }
      onDispose();
      // Dispose of all assets
      assets.dispose();
    } catch (Exception e) {
      Gdx.app.error("AbstractGame", "Uncaught exception in main loop, shutting down...", e);
      Gdx.app.exit();
    }
  }

  public abstract void onDispose() throws Exception;

  private void setupOverlay() {
    Pixmap overlayPixmap = new Pixmap(2, 2, Format.RGBA8888);
    overlayPixmap.setColor(Color.BLACK);
    overlayPixmap.fillRectangle(0, 0, 2, 2);
    overlayTexture = new Texture(overlayPixmap);
  }

  public enum ScreenState {
    FadeIn, Active, FadeOut
  }
}
