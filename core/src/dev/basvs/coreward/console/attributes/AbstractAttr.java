package dev.basvs.coreward.console.attributes;

public abstract class AbstractAttr {

  private String name;

  public AbstractAttr(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public abstract String get();

  public abstract void set(String value);
}
