package dev.basvs.coreward.combat.effects;

import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;
import static org.lwjgl.opengl.GL20.GL_POINT_SPRITE;
import static org.lwjgl.opengl.GL32.GL_PROGRAM_POINT_SIZE;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import dev.basvs.coreward.Constants;
import dev.basvs.lib.FloatRange;
import dev.basvs.lib.Utilities;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class PointParticleEffects {

  public static final int VERTEX_SIZE = 10;
  public static final int VERTEX_POSITION_INDEX = 0;
  public static final int VERTEX_VELOCITY_INDEX = 2;
  public static final int VERTEX_COLOR_INDEX = 4;
  public static final int VERTEX_SIZE_INDEX = 7;
  public static final int VERTEX_START_TIME_INDEX = 8;
  public static final int VERTEX_END_TIME_INDEX = 9;

  private static final String VERTEX_SHADER = """
      #version 330
            
      uniform mat4 Transform;
      uniform float Zoom;
      uniform float CurrentTime;
            
      in vec2 Position;
      in vec2 Velocity;
      in vec3 Color;
      in float Size;
      in float StartTime;
      in float EndTime;
            
      out vec3 RenderColor;
      out float LifeTimeFactor;
            
      void main() {
          if (CurrentTime >= EndTime || Size < 0.25) {
              gl_Position = vec4(0.0, 0.0, -99999999.0, 1);
              return;
          }
          float deltaT = CurrentTime - StartTime;
          vec2  velocity = pow(0.8, deltaT) * Velocity;
          vec2  renderPos = Position.xy + deltaT * velocity;
          LifeTimeFactor = deltaT / (EndTime - StartTime);
          // Fade particle color to black over time
          RenderColor = mix(Color, vec3(0,0,0), LifeTimeFactor);
          // Increase size of the particle over time
          gl_PointSize = (Size * 0.25 + (Size * 0.75) * LifeTimeFactor) * Zoom;
          gl_Position = Transform * vec4(renderPos, 0, 1);
      }
      """;

  private static final String FRAGMENT_SHADER = """
      #version 330
            
      in vec3 RenderColor;
      in float LifeTimeFactor;
            
      out vec4 OutColor;
            
      float length2(vec2 x) { return dot(x, x); }
            
      void main() {
          // Draw point sprite area as a blurred circle
          float alpha = (1.0 - 4.0 * length2(gl_PointCoord - 0.5)) * (1 - LifeTimeFactor);
          if (alpha <= 0.0)
              discard;
          OutColor = vec4(RenderColor.rgb * alpha, mix(0, alpha, LifeTimeFactor)); // Transition from additive to normal blending
      }
      """;

  private final RandomXS128 random;
  private final ShaderProgram shader;
  private final Mesh mesh;
  private float now;
  private final int maxParticles;
  private float[] activeBuffer, backBuffer;
  private int activeBufferParticles, backBufferParticles;
  private final Vector2 tempVec = new Vector2();

  private final List<Emitter> emitterPool;
  private final HashMap<String, ParticleEffect> effectRepo;
  private final Json json;

  public int getLiveParticlesCount() {
    return activeBufferParticles;
  }

  public PointParticleEffects(int maxParticles) throws Exception {
    this.maxParticles = maxParticles;
    random = new RandomXS128();
    shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    if (!shader.isCompiled()) {
      throw new Exception("Could not compile particle shader: " + shader.getLog());
    }
    mesh = new Mesh(false, maxParticles, 0,
        new VertexAttribute(Usage.Generic, 2, "Position"),
        new VertexAttribute(Usage.Generic, 2, "Velocity"),
        new VertexAttribute(Usage.Generic, 3, "Color"),
        new VertexAttribute(Usage.Generic, 1, "Size"),
        new VertexAttribute(Usage.Generic, 1, "StartTime"),
        new VertexAttribute(Usage.Generic, 1, "EndTime")
    );

    int bufferSize = maxParticles * VERTEX_SIZE;
    activeBuffer = new float[bufferSize];
    activeBufferParticles = 0;
    backBuffer = new float[bufferSize];
    backBufferParticles = 0;
    now = 1f;

    emitterPool = new ArrayList<>();
    effectRepo = new HashMap<>();

    // Setup json parser for loading and saving effects
    json = new Json();
    json.setOutputType(JsonWriter.OutputType.javascript);
    json.setUsePrototypes(false);
    json.setTypeName(null);
    json.setIgnoreUnknownFields(false);
    loadRepo("effects.json");

    // Set up a pool of emitters
    for (int p = 0; p < 64; p++) {
      emitterPool.add(new Emitter());
    }
  }


  /**
   * Load a repository of effects from a JSON file.
   *
   * @param fileName
   * @throws FileNotFoundException
   */
  public void loadRepo(String fileName) throws FileNotFoundException {
    effectRepo.clear();
    ArrayList<ParticleEffect> effects = json.fromJson(ArrayList.class, ParticleEffect.class,
        new FileInputStream(fileName));
    effects.forEach((effect) -> {
      effectRepo.put(effect.name, effect);
    });
  }

  /**
   * Save a repository of effects from a JSON file.
   *
   * @param fileName
   * @throws IOException
   */
  public void saveRepo(String fileName) throws IOException {
    ArrayList<ParticleEffect> effectList = getEffectsInRepo();
    String result = json.prettyPrint(effectList);
    Utilities.writeFile(fileName, result, StandardCharsets.UTF_8);
  }

  /**
   * Get a list of all effects in the repository, ordered by name.
   *
   * @return
   */
  public ArrayList<ParticleEffect> getEffectsInRepo() {
    ArrayList<ParticleEffect> result = new ArrayList<>(effectRepo.values());
    Collections.sort(result, Comparator.comparing((ParticleEffect e) -> e.name));
    return result;
  }

  /**
   * Start a new particle effect attached to a source.
   *
   * @param effectName
   * @param emitterSource
   */
  public void start(String effectName, ParticleEmitterSource emitterSource) {
    ParticleEffect effect = effectRepo.get(effectName);
    for (ParticleEmitterDef def : effect.emitters) {
      Emitter e = getEmitter();
      e.def = def;
      e.x = emitterSource.getEmitterX();
      e.y = emitterSource.getEmitterY();
      e.velocityX = emitterSource.getEmitterVelocityX();
      e.velocityY = emitterSource.getEmitterVelocityY();
      e.angle = emitterSource.getEmitterAngle() * MathUtils.degreesToRadians;
      e.source = emitterSource;
      e.startTime = now;
    }
  }

  /**
   * Start a new particle effect at a fixed position.
   */
  public void start(String effectName, float x, float y, float velocityX, float velocityY,
      float angle) {
    start(effectRepo.get(effectName), x, y, velocityX, velocityY, angle);
  }


  /**
   * Start a new particle effect at a fixed position.
   */
  public void start(ParticleEffect effect, float x, float y, float velocityX, float velocityY,
      float angle) {
    for (ParticleEmitterDef def : effect.emitters) {
      Emitter e = getEmitter();
      e.def = def;
      e.x = x;
      e.y = y;
      e.velocityX = velocityX;
      e.velocityY = velocityY;
      e.angle = angle;
      e.startTime = now;
    }
  }

  /**
   * Check if any emitter is currently active (i.e. emitting particles).
   *
   * @return
   */
  public boolean anyEmitterActive() {
    for (Emitter e : emitterPool) {
      if (e.startTime > 0f) {
        return true;
      }
    }
    return false;
  }

  /**
   * Immediately stop all emitters and remove all particles.
   */
  public void reset() {
    for (Emitter e : emitterPool) {
      e.destroy();
      activeBufferParticles = 0;
      backBufferParticles = 0;
    }
  }

  /**
   * Update and draw all particles. Should be called once per frame.
   *
   * @param deltaS Time since last update in seconds
   * @param camera
   */
  public void updateAndDraw(float deltaS, OrthographicCamera camera) {
    now += deltaS;

    for (Emitter emitter : emitterPool) {
      if (emitter.startTime > 0) {
        boolean emitterEnabled = true;
        if (emitter.source != null) {
          emitterEnabled = emitter.source.isEmitterEnabled();
          if (!emitter.source.isAlive()) {
            emitter.destroy();
            break;
          }
        }

        if (emitterEnabled) {
          if (now > (emitter.startTime + emitter.def.delay)) {
            // Emitter has started
            if (now < (emitter.startTime + emitter.def.delay + emitter.def.duration)
                || emitter.def.continuous) {
              // Emitter is still actively emitting
              emitter.accumulator += deltaS * emitter.def.particlesPerSecond;
              // Emit new particles
              while (emitter.accumulator > 1.0f && activeBufferParticles < maxParticles) {
                int index = activeBufferParticles * VERTEX_SIZE;
                // Position
                activeBuffer[index + VERTEX_POSITION_INDEX] =
                    emitter.getX() * Constants.WORLD_TO_SCREEN;
                activeBuffer[index + VERTEX_POSITION_INDEX + 1] =
                    emitter.getY() * Constants.WORLD_TO_SCREEN;
                // Velocity
                tempVec.set(random(emitter.def.velocity), 0f);
                Utilities.rotateVector2(tempVec,
                    emitter.getAngle() + (random.nextFloat() * emitter.def.angleDelta * 2f
                        - emitter.def.angleDelta)
                        * random.nextFloat());
                tempVec.x += emitter.getVelocityX() * Constants.WORLD_TO_SCREEN;
                tempVec.y += emitter.getVelocityY() * Constants.WORLD_TO_SCREEN;
                activeBuffer[index + VERTEX_VELOCITY_INDEX] = tempVec.x;
                activeBuffer[index + VERTEX_VELOCITY_INDEX + 1] = tempVec.y;
                // Color
                activeBuffer[index + VERTEX_COLOR_INDEX] = random(emitter.def.colorR);
                activeBuffer[index + VERTEX_COLOR_INDEX + 1] = random(emitter.def.colorG);
                activeBuffer[index + VERTEX_COLOR_INDEX + 2] = random(emitter.def.colorB);
                // Size
                activeBuffer[index + VERTEX_SIZE_INDEX] = random(emitter.def.size);
                // Start time
                activeBuffer[index + VERTEX_START_TIME_INDEX] = now;
                // End time
                activeBuffer[index + VERTEX_END_TIME_INDEX] = now + random(emitter.def.life);
                activeBufferParticles++;
                emitter.accumulator -= 1.0f;
              }
            } else {
              emitter.destroy();
            }
          }
        }
      }
    }

    mesh.setVertices(activeBuffer);  // TODO try update for speed

    shader.bind();
    shader.setUniformMatrix("Transform", camera.combined);
    shader.setUniformf("Zoom", 1f / camera.zoom);
    shader.setUniformf("CurrentTime", now);

    // Use point sprites so the fragment shader gets the texture coordinates
    GL11.glEnable(GL_POINT_SPRITE);
    // Set point size in the shader
    GL11.glEnable(GL_PROGRAM_POINT_SIZE);
    // Use blending
    GL11.glEnable(GL_BLEND);
    // Use additive blending.
    GL11.glBlendFunc(GL_ONE, GL_ONE);
    mesh.render(shader, GL_POINTS);

    // Restore default state
    GL11.glDisable(GL_POINT_SPRITE);
    GL11.glDisable(GL_PROGRAM_POINT_SIZE);
    GL11.glDisable(GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Swap vertex buffers
    float[] tmp = activeBuffer;
    activeBuffer = backBuffer;
    backBuffer = tmp;

    // Copy only live particles from backbuffer to active buffer
    backBufferParticles = activeBufferParticles;
    activeBufferParticles = 0;
    for (int i = 0; i < backBufferParticles; i++) {
      int pidx = i * VERTEX_SIZE;
      float particleEndTime = backBuffer[pidx + VERTEX_END_TIME_INDEX];
      if (particleEndTime > now) {
        // Live particle, keep it
        System.arraycopy(backBuffer, pidx, activeBuffer, activeBufferParticles * 10, VERTEX_SIZE);
        activeBufferParticles++;
      }
    }
  }

  private float random(FloatRange range) {
    return range.min() + random.nextFloat() * range.delta();
  }

  // Get an emitter from the pool or create a new one if none is available
  private Emitter getEmitter() {
    Emitter emitter = null;
    int eCount = emitterPool.size();
    for (int e = 0; e < eCount; e++) {
      Emitter emit = emitterPool.get(e);
      if (emit.startTime == 0) {
        emitter = emit;
        break;
      }
    }
    if (emitter == null) {
      emitter = new Emitter();
      emitterPool.add(emitter);
    }
    return emitter;
  }

  public static class Emitter {

    ParticleEmitterDef def = null;
    float x = 0f, y = 0f, velocityX = 0f, velocityY = 0f;
    float angle = 0f;
    float startTime = 0f;
    float accumulator = 0f;
    public ParticleEmitterSource source;

    public float getX() {
      return source != null ? source.getEmitterX() : x;
    }

    public float getY() {
      return source != null ? source.getEmitterY() : y;
    }

    public float getVelocityX() {
      return source != null ? source.getEmitterVelocityX() : velocityX;
    }

    public float getVelocityY() {
      return source != null ? source.getEmitterVelocityY() : velocityY;
    }

    public float getAngle() {
      return source != null ? source.getEmitterAngle() * MathUtils.degreesToRadians : angle;
    }

    public void destroy() {
      def = null;
      x = 0f;
      y = 0f;
      velocityX = 0f;
      velocityY = 0f;
      angle = 0f;
      startTime = 0f;
      accumulator = 0f;
      source = null;
    }
  }
}
