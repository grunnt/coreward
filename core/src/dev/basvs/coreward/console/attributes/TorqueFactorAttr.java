package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class TorqueFactorAttr extends AbstractAttr {

  public TorqueFactorAttr() {
    super("torque_factor");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.TORQUE_FACTOR);
  }

  @Override
  public void set(String value) {
    Attributes.TORQUE_FACTOR = Float.parseFloat(value);
  }
}
