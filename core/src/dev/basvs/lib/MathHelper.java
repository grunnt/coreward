package dev.basvs.lib;

public class MathHelper {

  public static float getRelativeAngle(float angle1, float angle2) {
    float relativeAngle = angle1 - angle2;
    return (relativeAngle %= 360f) >= 0 ? (relativeAngle < 180f) ? relativeAngle
        : relativeAngle - 360f
        : (relativeAngle >= -180f) ? relativeAngle : relativeAngle + 360f;
  }
}
