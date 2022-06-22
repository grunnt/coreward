package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class ThrustFactorAttr extends AbstractAttr {

  public ThrustFactorAttr() {
    super("thrust_factor");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.THRUST_FACTOR);
  }

  @Override
  public void set(String value) {
    Attributes.THRUST_FACTOR = Float.parseFloat(value);
  }
}
