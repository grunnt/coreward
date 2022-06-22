package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class ShowFPSAttr extends AbstractAttr {

  public ShowFPSAttr() {
    super("show_fps");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.SHOW_FPS);
  }

  @Override
  public void set(String value) {
    Attributes.SHOW_FPS = Boolean.parseBoolean(value);
  }
}
