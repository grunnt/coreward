package dev.basvs.lib;

public class FloatRange {

  private float min, max;

  public FloatRange(float min, float max) {
    set(min, max);
  }

  public FloatRange(float value) {
    set(value, value);
  }

  public FloatRange() {
    set(1f, 1f);
  }

  public float min() {
    return min;
  }

  public float max() {
    return max;
  }

  public float delta() {
    return max - min;
  }

  public final void set(float min, float max) {
    this.min = min;
    this.max = max;
  }

  public void set(FloatRange other) {
    set(other.min, other.max);
  }
}
