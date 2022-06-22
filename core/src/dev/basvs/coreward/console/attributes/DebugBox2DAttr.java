package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class DebugBox2DAttr extends AbstractAttr {

  public DebugBox2DAttr() {
    super("debug_physics");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.DEBUG_BOX2D);
  }

  @Override
  public void set(String value) {
    Attributes.DEBUG_BOX2D = Boolean.parseBoolean(value);
  }
}
