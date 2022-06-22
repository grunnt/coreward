package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class CollisionDamageFactorAttr extends AbstractAttr {

  public CollisionDamageFactorAttr() {
    super("col_damage_factor");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.COLLISION_DAMAGE_FACTOR);
  }

  @Override
  public void set(String value) {
    Attributes.COLLISION_DAMAGE_FACTOR = Float.parseFloat(value);
  }
}
