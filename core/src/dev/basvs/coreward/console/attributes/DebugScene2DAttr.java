package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class DebugScene2DAttr extends AbstractAttr {

  public DebugScene2DAttr() {
    super("debug_gui");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.DEBUG_SCENE2D);
  }

  @Override
  public void set(String value) {
    Attributes.DEBUG_SCENE2D = Boolean.parseBoolean(value);
  }
}
