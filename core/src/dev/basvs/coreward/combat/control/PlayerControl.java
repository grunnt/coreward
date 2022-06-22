package dev.basvs.coreward.combat.control;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.Ship;
import java.util.EnumMap;

public class PlayerControl {

  private final EnumMap<Control, Array<Cell>> controlModuleMap = new EnumMap<>(Control.class);
  private final Control[] primaryControlKeyMap = new Control[256];
  private final Control[] primaryControlMouseMap = new Control[3];
  private final Control[] secondaryControlKeyMap = new Control[256];
  private final Control[] secondaryControlMouseMap = new Control[3];
  private final Ship ship;

  public PlayerControl(Ship ship) {
    this.ship = ship;
    // Setup default control/module mappings
    for (Control c : Control.values()) {
      controlModuleMap.put(c, new Array<Cell>());
    }
    for (int x = 0; x < ship.design.width; x++) {
      for (int y = 0; y < ship.design.height; y++) {
        Cell c = ship.cells[x][y];
        if (c.module != null) {
          if (c.module.design.thruster != null) {
            switch (c.direction) {
              case 0:
                controlModuleMap.get(Control.Forward).add(c);
                break;
              case 1:
                controlModuleMap.get(Control.StrafeRight).add(c);
                break;
              case 2:
                controlModuleMap.get(Control.Backward).add(c);
                break;
              case 3:
                controlModuleMap.get(Control.StrafeLeft).add(c);
                break;
            }
          }
          if (c.module.design.reactionWheel != null) {
            controlModuleMap.get(Control.RotateClockwise).add(c);
            controlModuleMap.get(Control.RotateCounterClockwise).add(c);
          }
          if (c.module.design.weapon != null) {
            controlModuleMap.get(Control.Fire).add(c);
          }
        }
      }
    }
    // Set default input / control mappings
    setKeyMapping(Control.Forward, Keys.W, true);
    setKeyMapping(Control.Backward, Keys.S, true);
    setKeyMapping(Control.RotateClockwise, Keys.D, true);
    setKeyMapping(Control.RotateCounterClockwise, Keys.A, false);
    setKeyMapping(Control.StrafeLeft, Keys.Q, true);
    setKeyMapping(Control.StrafeRight, Keys.E, true);
    setMouseMapping(Control.Fire, Buttons.LEFT, true);
  }

  public void update(float delta, float inputWorldX, float inputWorldY) {
    for (int i = 0; i < ship.temp.weapons.size; i++) {
      Cell cell = ship.temp.weapons.get(i);
      cell.t.targetPoint.set(inputWorldX * Constants.SCREEN_TO_WORLD,
          inputWorldY * Constants.SCREEN_TO_WORLD);
    }
  }

  void setKeyMapping(Control control, int keycode, boolean primary) {
    if (primary) {
      primaryControlKeyMap[keycode] = control;
    } else {
      secondaryControlKeyMap[keycode] = control;
    }
  }

  void setMouseMapping(Control control, int button, boolean primary) {
    if (primary) {
      primaryControlMouseMap[button] = control;
    } else {
      secondaryControlMouseMap[button] = control;
    }
  }

  public boolean handleKeyDown(int keycode) {
    if (primaryControlKeyMap[keycode] != null) {
      setPrimaryControlState(controlModuleMap.get(primaryControlKeyMap[keycode]), true);
      return true;
    }
    if (secondaryControlKeyMap[keycode] != null) {
      setSecondaryControlState(controlModuleMap.get(secondaryControlKeyMap[keycode]), true);
      return true;
    }
    return false;
  }

  public boolean handleKeyUp(int keycode) {
    if (primaryControlKeyMap[keycode] != null) {
      setPrimaryControlState(controlModuleMap.get(primaryControlKeyMap[keycode]), false);
      return true;
    }
    if (secondaryControlKeyMap[keycode] != null) {
      setSecondaryControlState(controlModuleMap.get(secondaryControlKeyMap[keycode]), false);
      return true;
    }
    return false;
  }

  public boolean handleMouseDown(int button) {
    if (primaryControlMouseMap[button] != null) {
      setPrimaryControlState(controlModuleMap.get(primaryControlMouseMap[button]), true);
      return true;
    }
    if (secondaryControlMouseMap[button] != null) {
      setPrimaryControlState(controlModuleMap.get(secondaryControlMouseMap[button]), true);
      return true;
    }
    return false;
  }

  public boolean handleMouseUp(int button) {
    if (primaryControlMouseMap[button] != null) {
      setPrimaryControlState(controlModuleMap.get(primaryControlMouseMap[button]), false);
      return true;
    }
    if (secondaryControlMouseMap[button] != null) {
      setPrimaryControlState(controlModuleMap.get(secondaryControlMouseMap[button]), false);
      return true;
    }
    return false;
  }

  void setPrimaryControlState(Array<Cell> cells, boolean primary) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      cell.t.primaryControl = primary;
    }
  }

  void setSecondaryControlState(Array<Cell> cells, boolean secondary) {
    for (int i = 0; i < cells.size; i++) {
      Cell cell = cells.get(i);
      cell.t.secondaryControl = secondary;
    }
  }

  public enum Control {
    Forward, Backward, RotateClockwise, RotateCounterClockwise, StrafeLeft, StrafeRight, Fire
  }
}
