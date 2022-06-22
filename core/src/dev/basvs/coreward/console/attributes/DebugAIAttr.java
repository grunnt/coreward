package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class DebugAIAttr extends AbstractAttr {

  public DebugAIAttr() {
    super("debug_ai");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.DEBUG_AI);
  }

  @Override
  public void set(String value) {
    Attributes.DEBUG_AI = Boolean.parseBoolean(value);
  }
}
