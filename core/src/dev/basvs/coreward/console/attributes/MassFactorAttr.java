package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class MassFactorAttr extends AbstractAttr {

  public MassFactorAttr() {
    super("mass_factor");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.MASS_FACTOR);
  }

  @Override
  public void set(String value) {
    Attributes.MASS_FACTOR = Float.parseFloat(value);
  }
}
