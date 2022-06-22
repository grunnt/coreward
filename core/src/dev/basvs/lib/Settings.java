package dev.basvs.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Simple settings manager, somewhat like java properties.
 */
public class Settings {

  HashMap<String, String> settings = new HashMap<>();
  File file;
  String fileHeader;

  public Settings(String fileName, String fileHeader) {
    this.file = new File(fileName);
    this.fileHeader = fileHeader;
  }

  /**
   * Set a value.
   *
   * @param key
   * @param value
   * @throws SettingsException
   */
  public void set(String key, String value) throws SettingsException {
    settings.put(key, value);
  }

  /**
   * Get a setting value.
   *
   * @param key
   * @return
   * @throws SettingsException
   */
  public String get(String key) throws SettingsException {
    if (!settings.containsKey(key)) {
      throw new SettingsException("Setting '" + key + "' does not exist");
    }
    return settings.get(key);
  }

  /**
   * Get a setting value cast to integer.
   *
   * @param key
   * @return
   * @throws NumberFormatException
   * @throws SettingsException
   */
  public int getAsInt(String key) throws NumberFormatException, SettingsException {
    return Integer.parseInt(get(key));
  }

  /**
   * Get a setting value cast to boolean.
   *
   * @param key
   * @return
   * @throws NumberFormatException
   * @throws SettingsException
   */
  public boolean getAsBoolean(String key) throws NumberFormatException, SettingsException {
    return Boolean.parseBoolean(get(key));
  }

  /**
   * Check if the settings file already exists.
   *
   * @return
   */
  public boolean exists() {
    return file.exists();
  }

  /**
   * Load the settings from disk.
   *
   * @throws IOException
   * @throws SettingsException
   */
  public void load() throws IOException, SettingsException {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        // Ignore empty lines and comment lines starting with #
        if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
          String[] parts = line.split("=");
          if (parts.length != 2) {
            throw new SettingsException("Settings line '" + line + "'" + "' in file '"
                + file.getAbsolutePath() + "' has invalid format (should be <key>=<value>)");
          }
          String key = parts[0].trim();
          String value = parts[1].trim();
          settings.put(key, value);
        }
      }
    }
  }

  /**
   * Save the settings to disk.
   *
   * @throws IOException
   */
  public void save() throws IOException {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.write("# " + fileHeader + "\n");
      for (String key : settings.keySet()) {
        bw.write(key + " = " + settings.get(key) + "\n");
      }
    }
  }

  @SuppressWarnings("serial")
  public class SettingsException extends Exception {

    public SettingsException(String message) {
      super(message);
    }
  }
}
