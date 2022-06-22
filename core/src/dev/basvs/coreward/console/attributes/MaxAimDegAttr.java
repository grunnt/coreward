package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class MaxAimDegAttr extends AbstractAttr {

  public MaxAimDegAttr() {
    super("max_aim_deg");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.MAX_AIM_DEG);
  }

  @Override
  public void set(String value) {
    Attributes.MAX_AIM_DEG = Float.parseFloat(value);
  }
}
