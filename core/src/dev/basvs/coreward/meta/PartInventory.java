package dev.basvs.coreward.meta;

import java.util.HashMap;

public class PartInventory {

  private HashMap<String, Integer> parts = new HashMap();

  public int count(String partCode) {
    Integer count = parts.get(partCode);
    if (count == null) {
      return 0;
    }
    return count;
  }

  public void add(String partCode) {
    add(partCode, 1);
  }

  public void add(String partCode, int amount) {
    int count = count(partCode);
    parts.put(partCode, count + amount);
  }

  public void remove(String partCode) {
    int count = count(partCode);
    if (count > 0) {
      parts.put(partCode, count - 1);
    } else {
      parts.remove(partCode);
    }
  }
}
