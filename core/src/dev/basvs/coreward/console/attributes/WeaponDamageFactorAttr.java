package dev.basvs.coreward.console.attributes;

import dev.basvs.coreward.Attributes;

public class WeaponDamageFactorAttr extends AbstractAttr {

  public WeaponDamageFactorAttr() {
    super("wep_damage_factor");
  }

  @Override
  public String get() {
    return String.valueOf(Attributes.WEAPON_DAMAGE_FACTOR);
  }

  @Override
  public void set(String value) {
    Attributes.WEAPON_DAMAGE_FACTOR = Float.parseFloat(value);
  }
}
