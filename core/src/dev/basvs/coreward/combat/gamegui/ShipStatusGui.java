package dev.basvs.coreward.combat.gamegui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import dev.basvs.coreward.combat.world.ship.Cell;
import dev.basvs.coreward.combat.world.ship.EnergyNetwork;
import dev.basvs.coreward.combat.world.ship.Ship;

public class ShipStatusGui extends Window {

  Ship ship;
  Label eLabel;
  Label wLabel;
  Label speedLabel;
  StringBuilder sb = new StringBuilder();

  public ShipStatusGui(Ship ship, Skin skin) {
    super(ship.name + " status", skin);
    this.ship = ship;
    speedLabel = new Label("0", skin);
    add(speedLabel).row();
    eLabel = new Label("0", skin);
    add(eLabel).padBottom(5).row();
    wLabel = new Label("0", skin);
    add(wLabel);
  }

  public void update() {
    sb.setLength(0);
    sb.append("Speed: ");
    sb.append((int) (ship.body.getLinearVelocity().len()));
    speedLabel.setText(sb);
    sb.setLength(0);
    if (ship.temp.eNetworks != null) {
      for (int i = 0; i < ship.temp.eNetworks.size; i++) {
        EnergyNetwork eNetwork = ship.temp.eNetworks.get(i);
        sb.append("E");
        sb.append(i);
        sb.append(": ");
        sb.append((int) ((eNetwork.eBuffered / eNetwork.eBufferSize) * 100f));
        sb.append("%\n");
      }
      eLabel.setText(sb);
    }
    sb.setLength(0);
    if (ship.temp.weapons.size > 0) {
      for (int i = 0; i < ship.temp.weapons.size; i++) {
        Cell weapon = ship.temp.weapons.get(i);
        if (weapon.isAlive()) {
          sb.append("W");
          sb.append(i);
          sb.append(": ");
          sb.append(
              (int) ((weapon.module.localBuffered / weapon.module.design.consumer.localBufferSize)
                  * 100f));
          sb.append("%\n");
        }
      }
      wLabel.setText(sb);
    }
  }
}
