package dev.basvs.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple helper class for limited logging.
 */
public class Logging {

  static File file;
  static long maxSize;
  static SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

  /**
   * Initialize the log file.
   *
   * @param fileName
   * @param maxSizeKb
   */
  public static void initialize(String fileName, long maxSizeKb) {
    if (file != null) {
      throw new RuntimeException("Logging already initialized");
    }
    file = new File(fileName);
    Logging.maxSize = maxSizeKb * 1024;
  }

  /**
   * Write error message to the logfile.
   *
   * @param message
   */
  public static void error(String message) {
    write("ERROR", message);
  }

  /**
   * Write error message to the logfile with additional exception information.
   *
   * @param message
   * @param t
   */
  public static void error(String message, Throwable t) {
    write("ERROR", message, t);
  }

  /**
   * Write warning message to the logfile.
   *
   * @param message
   */
  public static void warning(String message) {
    write("WARNING", message);
  }

  /**
   * Write information message to the logfile.
   *
   * @param message
   */
  public static void info(String message) {
    write("INFO", message);
  }

  private static void write(String prefix, String message) {
    checkRotate();
    try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
      pw.println("[" + prefix + " " + format.format(new Date()) + "] " + message);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private static void write(String prefix, String message, Throwable t) {
    checkRotate();
    try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
      pw.println("[" + prefix + " " + format.format(new Date()) + "] " + message + ":");
      t.printStackTrace(pw);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    t.printStackTrace();
  }

  private static void checkRotate() {
    try {
      if (file.length() >= maxSize) {
        // Rotate logfiles
        File secondFile = new File(file.getName() + ".old");
        if (secondFile.exists()) {
          secondFile.delete();
        }
        file.renameTo(secondFile);
      }
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
  }
}
