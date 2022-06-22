package dev.basvs.coreward;

public class Attributes {

  public static StartMode START_MODE = StartMode.Normal;

  public static float ANGULAR_DAMPENING = 0.1f;
  public static float LINEAR_DAMPENING = 0.1f;

  public static float THRUST_FACTOR = 30f;
  public static float TORQUE_FACTOR = 40f;

  public static float MASS_FACTOR = 1f;

  public static float COLLISION_DAMAGE_FACTOR = 1f;
  public static float WEAPON_DAMAGE_FACTOR = 1f;

  public static float MIN_ACCURACY_DEG = 90f;
  public static float MAX_AIM_DEG = 360f;

  /* TODO make max ship velocity dependent on TWR */
  public static float MAX_SHIP_VELOCITY = 60f;
  public static float MAX_SHIP_ANGULAR_VELOCITY = 2.5f;
  public static float MAX_BULLET_VELOCITY = 120f;

  public static boolean DEBUG_BOX2D = false;
  public static boolean DEBUG_SCENE2D = false;
  public static boolean DEBUG_AI = false;

  public static boolean SHOW_FPS = true;
}
