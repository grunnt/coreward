package dev.basvs.lib;

import com.badlogic.gdx.math.Vector2;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Utilities {

  public static final int FLOAT_BYTES = Float.SIZE / Byte.SIZE;
  public static final int INT_BYTES = Integer.SIZE / Byte.SIZE;

  public static final float PI = 3.14159265359f;
  public static final float PI2 = PI * 2f;

  public static String readFile(String path, Charset encoding)
      throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static void writeFile(String path, String contents, Charset encoding)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), encoding)) {
      writer.write(contents);
    }
  }

  public static String streamToString(InputStream in, Charset encoding)
      throws IOException {
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in, encoding))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  public static void rotateVector2(Vector2 vector, float radians) {
    float cos = (float) Math.cos(radians);
    float sin = (float) Math.sin(radians);

    float newX = vector.x * cos - vector.y * sin;
    float newY = vector.x * sin + vector.y * cos;

    vector.x = newX;
    vector.y = newY;
  }
}
