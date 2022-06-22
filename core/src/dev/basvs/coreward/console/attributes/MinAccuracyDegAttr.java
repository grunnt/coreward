package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class MinAccuracyDegAttr extends AbstractAttr {

  public MinAccuracyDegAttr() {
    super("min_accuracy_deg");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.MIN_ACCURACY_DEG);
  }

  @Override
  public void set(String value) {
    Attributes.MIN_ACCURACY_DEG = Float.parseFloat(value);
  }
}
