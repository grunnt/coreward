package dev.basvs.coreward;

import com.badlogic.gdx.Input.Keys;
import java.io.File;

public class Constants {

  public static final float WORLD_TO_SCREEN = 32f;
  public static final float SCREEN_TO_WORLD = 1f / WORLD_TO_SCREEN;

  public static final int CELL_SIZE_PX = 32;

  public static final int DESIGNS_SIZE = 101;

  // Define collision categories for the different kinds of bodies
  public static final short FRIENDLY_SHIP_CATEGORY_BITS = 1;
  public static final short ENEMY_SHIP_CATEGORY_BITS = 2;
  public static final short FRIENDLY_BULLET_CATEGORY_BITS = 4;
  public static final short ENEMY_BULLET_CATEGORY_BITS = 8;

  // Friendly ships do not collide with friendly bullets
  public static final short FRIENDLY_SHIP_MASK_BITS = 11;
  // Enemy ships do not collide with enemy bullets
  public static final short ENEMY_SHIP_MASK_BITS = 7;
  // Friendly bullets hit enemy ships
  public static final short FRIENDLY_BULLET_MASK_BITS = 2;
  // Enemy bullets hit friendly ships
  public static final short ENEMY_BULLET_MASK_BITS = 1;

  public static final File DESIGNS_DIRECTORY = new File("designs");
  public static final File DESIGNS_EXTENSION = new File("json");
  public static final File DESIGNS_DEFAULT = new File("default");

  public static final int CONSOLE_KEY = Keys.PAGE_DOWN;

  public static final boolean EFFECT_MAKER = false; // TODO make console command

  // Settings
  public static final String SETTING_WIDTH = "width";
  public static final String SETTING_HEIGHT = "height";
  public static final String SETTING_FULLSCREEN = "fullscreen";
}
