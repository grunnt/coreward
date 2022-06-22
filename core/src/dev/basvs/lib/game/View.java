package dev.basvs.lib.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class View extends ScreenViewport {

  public SpriteBatch batch;
  public OrthographicCamera cam;

  public View() {
    super();
    cam = new OrthographicCamera();
    setCamera(cam);
    batch = new SpriteBatch();
  }

  public OrthographicCamera getCamera() {
    return cam;
  }

  public void update() {
    update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  public void begin() {
    batch.setProjectionMatrix(cam.combined);
    batch.begin();
  }

  public void end() {
    batch.end();
  }

  public void setPos(float x, float y) {
    cam.position.set(x, y, 0f);
  }

  public void setPos(Vector2 v) {
    cam.position.set(v.x, v.y, 0f);
  }

  public void setRotation(float degrees) {
    cam.direction.set(0, 0, 1);
    cam.rotate(degrees);
  }

  public void setZoom(float zoom) {
    cam.zoom = zoom;
  }

  public void zoom(float zoomDelta) {
    cam.zoom += zoomDelta;
  }

  public void reset() {
    setPos(0f, 0f);
    setRotation(0f);
    setZoom(1f);
  }
}
