package dev.basvs.coreward;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.basvs.lib.Logging;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

  public static void main(String[] arg) {
    Logging.initialize("coreward.log", 128);
    Logging.info("*** Startup ***");
    try {
      Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
      config.setWindowedMode(1024, 768);
      config.setForegroundFPS(60);
      config.setTitle("Coreward");
      new Lwjgl3Application(new Coreward(arg.length > 0 ? arg[0] : ""), config);
    } catch (Throwable t) {
      Logging.error("Uncaught exception", t);
      // ErrorDialog.show(null, "Uncaught exception", t);
    }
  }
}
