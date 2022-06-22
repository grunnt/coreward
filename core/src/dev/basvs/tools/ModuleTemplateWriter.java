package dev.basvs.tools;

import com.badlogic.gdx.utils.Json;
import dev.basvs.coreward.design.modules.EnergyBufferDesign;
import dev.basvs.coreward.design.modules.EnergyConduitDesign;
import dev.basvs.coreward.design.modules.EnergyConsumerDesign;
import dev.basvs.coreward.design.modules.EnergyProducerDesign;
import dev.basvs.coreward.design.modules.ModuleDesign;
import dev.basvs.coreward.design.modules.ReactionWheelDesign;
import dev.basvs.coreward.design.modules.ThrusterDesign;
import dev.basvs.coreward.design.modules.WeaponDesign;

public class ModuleTemplateWriter {

  public static void main(String[] args) {
    Json json = new Json();
    json.setUsePrototypes(false);
    ModuleDesign moduleDesign = new ModuleDesign();
    moduleDesign.buffer = new EnergyBufferDesign();
    moduleDesign.conduit = new EnergyConduitDesign();
    moduleDesign.consumer = new EnergyConsumerDesign();
    moduleDesign.producer = new EnergyProducerDesign();
    moduleDesign.weapon = new WeaponDesign();
    moduleDesign.reactionWheel = new ReactionWheelDesign();
    moduleDesign.thruster = new ThrusterDesign();
    System.out.println(json.prettyPrint(moduleDesign));
  }
}
