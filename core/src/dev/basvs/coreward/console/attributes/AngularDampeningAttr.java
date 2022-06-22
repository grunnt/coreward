package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class AngularDampeningAttr extends AbstractAttr {

  public AngularDampeningAttr() {
    super("angular_dampening");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.ANGULAR_DAMPENING);
  }

  @Override
  public void set(String value) {
    Attributes.ANGULAR_DAMPENING = Float.parseFloat(value);
  }
}
