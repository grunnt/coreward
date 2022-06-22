package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class LinearDampeningAttr extends AbstractAttr {

  public LinearDampeningAttr() {
    super("linear_dampening");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.LINEAR_DAMPENING);
  }

  @Override
  public void set(String value) {
    Attributes.LINEAR_DAMPENING = Float.parseFloat(value);
  }

}
