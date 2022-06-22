package dev.basvs.lib.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.Constants;

public abstract class AbstractScreen extends ChangeListener implements InputProcessor,
    GestureListener {

  protected final AbstractGame game;
  // Gui on this screen
  protected final Stage guiStage;
  protected final Table guiRoot;
  protected final View worldView, screenView;
  // Input handling
  public Vector2 inputWorldPos = new Vector2();
  // Rendering and views
  protected Color backgroundColor = Color.BLACK;
  // Screen state
  private boolean removeAfterFadeOut = false;
  private boolean loadingScreen = false;
  // For rendering scene2d debug shapes
  private ShapeRenderer debugRenderer;
  // Some input handling helpers
  private Vector2 iv1 = new Vector2(), iv2 = new Vector2();

  public AbstractScreen(AbstractGame game) {
    this.game = game;
    // Setup world rendering
    worldView = new View();
    // Setup GUI rendering
    screenView = new View();
    guiStage = new Stage(screenView);
    guiRoot = new Table();
    guiRoot.setFillParent(true);
    guiStage.addActor(guiRoot);
    debugRenderer = new ShapeRenderer();
    debugRenderer.setAutoShapeType(true);
  }

  public boolean isRemoveAfterFadeOut() {
    return removeAfterFadeOut;
  }

  public void setRemoveAfterFadeOut(boolean removeAfterFadeOut) {
    this.removeAfterFadeOut = removeAfterFadeOut;
  }

  /**
   * If true, do not use FPS limiter and time step cap mechanism for this state. Useful for loading
   * states that do not require smooth animation and that have very large delays between renders.
   *
   * @return
   */
  public boolean isLoadingScreen() {
    return loadingScreen;
  }

  /**
   * Do not use FPS limiter and time step cap mechanism for this state. Useful for loading states
   * that do not require smooth animation and that have very large delays between renders.
   *
   * @param loadingScreen
   */
  public void setLoadingScreen(boolean loadingScreen) {
    this.loadingScreen = loadingScreen;
  }

  ;

  public void onActivate() {
  }

  public void onReactivate() {
  }

  public void onDeactivate() {
  }

  protected final void update(float delta) throws Exception {
    guiStage.setDebugAll(Attributes.DEBUG_SCENE2D);
    inputWorldPos.set(Gdx.input.getX(), Gdx.input.getY());
    inputWorldPos = worldView.unproject(inputWorldPos);
    onUpdate(delta);
  }

  public abstract void onUpdate(float delta) throws Exception;

  protected final void render(float timeDeltaSeconds) throws Exception {
    worldView.update();
    screenView.setPos(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    screenView.update();
    onRender(timeDeltaSeconds);
    // Render the GUI on top of everything
    guiStage.draw();
    if (Attributes.DEBUG_SCENE2D) {
      debugRenderer.begin();
      guiRoot.drawDebug(debugRenderer);
      debugRenderer.end();
    }
  }

  public abstract void onRender(float timeDeltaSeconds) throws Exception;

  ;

  public void onResize(int width, int height) throws Exception {
  }

  ;

  public void onPause() throws Exception {
  }

  ;

  public void onDispose() throws Exception {
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    // Ignore this and use the gesture detector event
    return false;
  }

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    worldView.unproject(iv1.set(x, y));
    return handleTouchDown(iv1.x, iv1.y, button);
  }

  public boolean handleTouchDown(float worldX, float worldY, int button) {
    return false;
  }

  ;

  @Override
  public boolean tap(float x, float y, int count, int button) {
    worldView.unproject(iv1.set(x, y));
    return handleClick(iv1.x, iv1.y, count, button);
  }

  public boolean handleClick(float worldX, float worldY, int count, int button) {
    return false;
  }

  ;

  @Override
  public boolean longPress(float x, float y) {
    worldView.unproject(iv1.set(x, y));
    return handleLongPress(iv1.x, iv1.y);
  }

  public boolean handleLongPress(float worldX, float worldY) {
    return false;
  }

  ;

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    return handleFling(velocityX, velocityY);
  }

  public boolean handleFling(float velocityX, float velocityY) {
    return false;
  }

  ;

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    worldView.unproject(iv1.set(x, y));
    worldView.unproject(iv2.set(x, y).add(deltaX, deltaY));
    iv2.sub(iv1);
    return handlePan(iv1.x, iv1.y, iv2.x, iv2.y);
  }

  public boolean handlePan(float worldX, float worldY, float deltaX, float deltaY) {
    return false;
  }

  ;

  @Override
  public boolean zoom(float initialDistance, float distance) {
    return handleZoom(initialDistance, distance);
  }

  public boolean handleZoom(float initialDistance, float distance) {
    return false;
  }

  ;

  @Override
  public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
      Vector2 pointer2) {
    // Ignore
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    worldView.unproject(iv1.set(screenX, screenY));
    return handleTouchUp(iv1.x, iv1.y, button);
  }

  public boolean handleTouchUp(float worldX, float worldY, int button) {
    return false;
  }

  ;

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    worldView.unproject(iv1.set(screenX, screenY));
    return handleTouchDragged(iv1.x, iv1.y);
  }

  @Override
  public boolean panStop(float x, float y, int pointer, int button) {
    return false;
  }

  public boolean handleTouchDragged(float worldX, float worldY) {
    return false;
  }

  ;

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    // Ignore
    return false;
  }

  @Override
  public final boolean keyDown(int keycode) {
    if (keycode == Constants.CONSOLE_KEY && game.console != null) {
      game.console.show(guiStage);
      return true;
    }
    return handleKeyDown(keycode);
  }

  public boolean handleKeyDown(int keycode) {
    return false;
  }

  @Override
  public final boolean keyUp(int keycode) {
    return handleKeyUp(keycode);
  }

  public boolean handleKeyUp(int keycode) {
    return false;
  }

  @Override
  public final boolean keyTyped(char character) {
    return handleKeyTyped(character);
  }

  public boolean handleKeyTyped(char character) {
    return false;
  }

  @Override
  public void pinchStop() {

  }
}
