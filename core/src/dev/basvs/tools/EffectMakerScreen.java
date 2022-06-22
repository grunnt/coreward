package dev.basvs.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.StringBuilder;
import dev.basvs.coreward.Attributes;
import dev.basvs.coreward.combat.effects.ParticleEffect;
import dev.basvs.coreward.combat.effects.ParticleEmitterDef;
import dev.basvs.coreward.combat.effects.PointParticleEffects;
import dev.basvs.lib.game.AbstractGame;
import dev.basvs.lib.game.AbstractScreen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EffectMakerScreen extends AbstractScreen {

  static final String BOTTOM_TEXT = "Coreward prototype - effects, Grunnt, 2017";

  // Text rendering
  BitmapFont font14;
  GlyphLayout textBounds = new GlyphLayout();
  StringBuilder sb = new StringBuilder();

  // Game world rendering
  PointParticleEffects effects;

  Label emitterLabel;
  TextField effectName;
  Slider delaySlider;
  Slider durationSlider;
  CheckBox continuousCheckBox;
  Slider ppsSlider;
  Slider lifeMinSlider, lifeMaxSlider;

  Slider colorRminSlider, colorRmaxSlider;
  Slider colorGminSlider, colorGmaxSlider;
  Slider colorBminSlider, colorBmaxSlider;

  Slider sizeMinSlider, sizeMaxSlider;

  Slider angleSlider;
  Slider angleDeltaSlider;

  Slider velocitySlider;

  ArrayList<ParticleEffect> effectList;
  int activeEffect;
  int activeEmitter;

  // The effect can auto play at the center of the screen
  float autoPlayCountdown, maxEffectDuration;
  boolean autoPlayEffect;

  boolean suppressChangeEvents;
  Vector2 tv = new Vector2();

  GLProfiler profiler;

  public EffectMakerScreen(AbstractGame game) throws Exception {
    super(game);

    font14 = game.getAssets().get("azertype14.fnt", BitmapFont.class);

    effects = new PointParticleEffects(1000000);
    effectList = effects.getEffectsInRepo();

    setupGui();

    setActive(0, 0);
  }

  void setActive(int effectIndex, int emitterIndex) {
    suppressChangeEvents = true;

    activeEffect = effectIndex;
    activeEmitter = emitterIndex;

    ParticleEmitterDef aed = effectList.get(activeEffect).emitters.get(activeEmitter);

    effectName.setText(effectList.get(activeEffect).name);
    emitterLabel.setText("Emitter " + activeEmitter);

    delaySlider.setValue(aed.delay);

    durationSlider.setValue(aed.duration);

    ppsSlider.setValue(aed.particlesPerSecond);

    continuousCheckBox.setChecked(aed.continuous);

    velocitySlider.setValue(aed.velocity.max());

    lifeMaxSlider.setValue(aed.life.max());
    lifeMinSlider.setValue(aed.life.min());

    colorRmaxSlider.setValue(aed.colorR.max());
    colorRminSlider.setValue(aed.colorR.min());

    colorGmaxSlider.setValue(aed.colorG.max());
    colorGminSlider.setValue(aed.colorG.min());

    colorBmaxSlider.setValue(aed.colorB.max());
    colorBminSlider.setValue(aed.colorB.min());

    sizeMaxSlider.setValue(aed.size.max());
    sizeMinSlider.setValue(aed.size.min());

    angleSlider.setValue(aed.angle);
    angleDeltaSlider.setValue(aed.angleDelta * MathUtils.radiansToDegrees);

    // Determine how long the effect will last max for auto play
    maxEffectDuration = 0f;
    for (ParticleEmitterDef def : effectList.get(activeEffect).emitters) {
      float delayPlusDuration = def.delay + def.duration + def.life.max();
      if (delayPlusDuration > maxEffectDuration) {
        maxEffectDuration = delayPlusDuration;
      }
    }
    maxEffectDuration *= 1.05f;
    autoPlayCountdown = 0f;

    suppressChangeEvents = false;
  }

  Slider addLabeledSlider(Table table, String label, float min, float max, float step) {
    Label sliderLabel = new Label(label, game.guiSkin);
    table.add(sliderLabel).row();
    Slider slider = new Slider(min, max, step, false, game.guiSkin);
    table.add(slider);
    Label valLabel = new Label(String.format("%.2f", slider.getValue()), game.guiSkin);
    table.add(valLabel).row();
    slider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        valLabel.setText(String.format("%.2f", slider.getValue()));
      }
    });
    return slider;
  }

  private void setupGui() {
    Table leftMenus = new Table();
    leftMenus.defaults().pad(3).width(100).fill();
    guiRoot.add(leftMenus).expand().top().left();

    emitterLabel = new Label("", game.guiSkin);
    leftMenus.add(emitterLabel).colspan(2).fill();

    TextButton nextEmitterButton = new TextButton("Next", game.guiSkin);
    nextEmitterButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setActive(activeEffect, (activeEmitter + 1) % effectList.get(activeEffect).emitters.size);
      }
    });
    leftMenus.add(nextEmitterButton).row();

    TextButton addEmitterButton = new TextButton("Add", game.guiSkin);
    addEmitterButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ParticleEffect e = effectList.get(activeEffect);
        ParticleEmitterDef def = e.emitters.get(activeEmitter).copy();
        e.emitters.insert(activeEffect, def);
        setActive(activeEffect, 0);
      }
    });
    leftMenus.add(addEmitterButton).colspan(2);

    TextButton delEmitterButton = new TextButton("Delete", game.guiSkin);
    delEmitterButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        effectList.get(activeEffect).emitters.get(activeEmitter).continuous = false;
        effectList.get(activeEffect).emitters.removeIndex(activeEmitter);
        setActive(activeEffect, (activeEmitter + 1) % effectList.get(activeEffect).emitters.size);
      }
    });
    leftMenus.add(delEmitterButton).row();

    delaySlider = addLabeledSlider(leftMenus, "Delay", 0f, 10f, 0.05f);
    delaySlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.delay = delaySlider.getValue();
      }
    });

    durationSlider = addLabeledSlider(leftMenus, "Duration", 0.05f, 10f, 0.05f);
    durationSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.duration = durationSlider.getValue();
      }
    });

    continuousCheckBox = new CheckBox("Continuous", game.guiSkin);
    continuousCheckBox.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.continuous = continuousCheckBox.isChecked();
      }
    });
    leftMenus.add(continuousCheckBox).colspan(3).left().row();

    velocitySlider = addLabeledSlider(leftMenus, "Velocity", 1f, 1000f, 1f);
    velocitySlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.velocity.set(1f, velocitySlider.getValue());
      }
    });

    angleSlider = addLabeledSlider(leftMenus, "Angle", 0f, 360f, 1f);
    angleSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.angle = angleSlider.getValue() * MathUtils.degreesToRadians;
      }
    });

    angleDeltaSlider = addLabeledSlider(leftMenus, "Angle spread", 0f, 360f, 1f);
    angleDeltaSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.angleDelta = angleDeltaSlider.getValue() * MathUtils.degreesToRadians;
      }
    });

    ppsSlider = addLabeledSlider(leftMenus, "PPS", 1f, 1024f, 1f);
    ppsSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.particlesPerSecond = ppsSlider.getValue();
      }
    });

    // Life
    Label lifeLabel = new Label("Life", game.guiSkin);
    leftMenus.add(lifeLabel).colspan(3).left().row();
    lifeMinSlider = new Slider(0.05f, 3.9f, 0.05f, false, game.guiSkin);
    leftMenus.add(lifeMinSlider);
    lifeMaxSlider = new Slider(0.05f, 3.9f, 0.05f, false, game.guiSkin);
    leftMenus.add(lifeMaxSlider);
    Label lifeValLabel = new Label(
        String.format("%.2f / %.2f", lifeMinSlider.getValue(), lifeMaxSlider.getValue()),
        game.guiSkin);
    leftMenus.add(lifeValLabel).row();
    lifeMinSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        lifeValLabel.setText(
            String.format("%.2f / %.2f", lifeMinSlider.getValue(), lifeMaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (lifeMinSlider.getValue() > lifeMaxSlider.getValue()) {
          lifeMaxSlider.setValue(lifeMinSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.life.set(lifeMinSlider.getValue(), lifeMaxSlider.getValue());
      }
    });
    lifeMaxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        lifeValLabel.setText(
            String.format("%.2f / %.2f", lifeMinSlider.getValue(), lifeMaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (lifeMaxSlider.getValue() < lifeMinSlider.getValue()) {
          lifeMinSlider.setValue(lifeMaxSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.life.set(lifeMinSlider.getValue(), lifeMaxSlider.getValue());
      }
    });

    // Color range red
    Label colorRlabel = new Label("Red", game.guiSkin);
    leftMenus.add(colorRlabel).colspan(3).left().row();
    colorRminSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorRminSlider);
    colorRmaxSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorRmaxSlider);
    Label colorRvalLabel = new Label(
        String.format("%.2f / %.2f", colorRminSlider.getValue(), colorRmaxSlider.getValue()),
        game.guiSkin);
    leftMenus.add(colorRvalLabel).row();
    colorRminSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorRvalLabel.setText(
            String.format("%.2f / %.2f", colorRminSlider.getValue(), colorRmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorRminSlider.getValue() > colorRmaxSlider.getValue()) {
          colorRmaxSlider.setValue(colorRminSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorR.set(colorRminSlider.getValue(), colorRmaxSlider.getValue());
      }
    });
    colorRmaxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorRvalLabel.setText(
            String.format("%.2f / %.2f", colorRminSlider.getValue(), colorRmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorRmaxSlider.getValue() < colorRminSlider.getValue()) {
          colorRminSlider.setValue(colorRmaxSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorR.set(colorRminSlider.getValue(), colorRmaxSlider.getValue());
      }
    });

    // Color range green
    Label colorGlabel = new Label("Green", game.guiSkin);
    leftMenus.add(colorGlabel).colspan(3).left().row();
    colorGminSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorGminSlider);
    colorGmaxSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorGmaxSlider);
    Label colorGvalLabel = new Label(
        String.format("%.2f / %.2f", colorGminSlider.getValue(), colorGmaxSlider.getValue()),
        game.guiSkin);
    leftMenus.add(colorGvalLabel).row();
    colorGminSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorGvalLabel.setText(
            String.format("%.2f / %.2f", colorGminSlider.getValue(), colorGmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorGminSlider.getValue() > colorGmaxSlider.getValue()) {
          colorGmaxSlider.setValue(colorGminSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorG.set(colorGminSlider.getValue(), colorGmaxSlider.getValue());
      }
    });
    colorGmaxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorGvalLabel.setText(
            String.format("%.2f / %.2f", colorGminSlider.getValue(), colorGmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorGmaxSlider.getValue() < colorGminSlider.getValue()) {
          colorGminSlider.setValue(colorGmaxSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorG.set(colorGminSlider.getValue(), colorGmaxSlider.getValue());
      }
    });

    // Color range blue
    Label colorBlabel = new Label("Blue", game.guiSkin);
    leftMenus.add(colorBlabel).colspan(3).left().row();
    colorBminSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorBminSlider);
    colorBmaxSlider = new Slider(0f, 1f, 0.05f, false, game.guiSkin);
    leftMenus.add(colorBmaxSlider);
    Label colorBvalLabel = new Label(
        String.format("%.2f / %.2f", colorBminSlider.getValue(), colorBmaxSlider.getValue()),
        game.guiSkin);
    leftMenus.add(colorBvalLabel).row();
    colorBminSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorBvalLabel.setText(
            String.format("%.2f / %.2f", colorBminSlider.getValue(), colorBmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorBminSlider.getValue() > colorBmaxSlider.getValue()) {
          colorBmaxSlider.setValue(colorBminSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorB.set(colorBminSlider.getValue(), colorBmaxSlider.getValue());
      }
    });
    colorBmaxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        colorBvalLabel.setText(
            String.format("%.2f / %.2f", colorBminSlider.getValue(), colorBmaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (colorBmaxSlider.getValue() < colorBminSlider.getValue()) {
          colorBminSlider.setValue(colorBmaxSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.colorB.set(colorBminSlider.getValue(), colorBmaxSlider.getValue());
      }
    });

    // Size
    Label sizeLabel = new Label("Size", game.guiSkin);
    leftMenus.add(sizeLabel).colspan(3).left().row();
    sizeMinSlider = new Slider(5f, 256f, 1f, false, game.guiSkin);
    leftMenus.add(sizeMinSlider);
    sizeMaxSlider = new Slider(5f, 256f, 1f, false, game.guiSkin);
    leftMenus.add(sizeMaxSlider);
    Label sizeValLabel = new Label(
        String.format("%.2f / %.2f", sizeMinSlider.getValue(), sizeMaxSlider.getValue()),
        game.guiSkin);
    leftMenus.add(sizeValLabel).row();
    sizeMinSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        sizeValLabel.setText(
            String.format("%.2f / %.2f", sizeMinSlider.getValue(), sizeMaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (sizeMinSlider.getValue() > sizeMaxSlider.getValue()) {
          sizeMaxSlider.setValue(sizeMinSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.size.set(sizeMinSlider.getValue(), sizeMaxSlider.getValue());
      }
    });
    sizeMaxSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        sizeValLabel.setText(
            String.format("%.2f / %.2f", sizeMinSlider.getValue(), sizeMaxSlider.getValue()));
        if (suppressChangeEvents) {
          return;
        }
        if (sizeMaxSlider.getValue() < sizeMinSlider.getValue()) {
          sizeMinSlider.setValue(sizeMaxSlider.getValue());
        }
        ParticleEmitterDef eDef = effectList.get(activeEffect).emitters.get(activeEmitter);
        eDef.size.set(sizeMinSlider.getValue(), sizeMaxSlider.getValue());
      }
    });

    Table systemMenu = new Table();
    systemMenu.defaults().pad(3);
    guiRoot.add(systemMenu).expand().top().right();

    Label nameLabel = new Label("Name", game.guiSkin);
    systemMenu.add(nameLabel);
    effectName = new TextField("", game.guiSkin);
    effectName.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        ParticleEffect effect = effectList.get(activeEffect);
        effect.name = effectName.getText();
      }
    });
    systemMenu.add(effectName).fill().row();

    TextButton effectButton = new TextButton("Next effect", game.guiSkin);
    effectButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        effects.reset();
        setActive((activeEffect + 1) % effectList.size(), 0);
      }
    });
    systemMenu.add(effectButton).colspan(2).fill().row();

    TextButton saveButton = new TextButton("Save", game.guiSkin);
    saveButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        try {
          effects.saveRepo("effects.json");
        } catch (IOException ex) {
          Logger.getLogger(EffectMakerScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    });
    systemMenu.add(saveButton).fill();

    TextButton exitButton = new TextButton("Exit", game.guiSkin);
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setRemoveAfterFadeOut(true);
        game.deactivateScreen();
      }
    });
    systemMenu.add(exitButton).fill().row();

    CheckBox autoPlayCheckbox = new CheckBox("Auto play", game.guiSkin);
    autoPlayCheckbox.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent ce, Actor actor) {
        autoPlayEffect = autoPlayCheckbox.isChecked();
      }
    });
    systemMenu.add(autoPlayCheckbox).colspan(2).left().fill();

    profiler = new GLProfiler(Gdx.graphics);
    profiler.disable();
  }

  @Override
  public void onUpdate(float delta) throws Exception {
  }

  @Override
  public void onRender(float timeDeltaSeconds) throws Exception {

    if (autoPlayEffect) {
      boolean continuous = false;
      for (ParticleEmitterDef def : effectList.get(activeEffect).emitters) {
        if (def.continuous) {
          continuous = true;
          break;
        }
      }
      if (continuous) {
        if (!effects.anyEmitterActive()) {
          effects.start(effectList.get(activeEffect), 0f, 0f, 0f, 0f, 0f);
        }
      } else {
        autoPlayCountdown -= timeDeltaSeconds;
        if (autoPlayCountdown <= 0f) {
          autoPlayCountdown = maxEffectDuration;
          effects.start(effectList.get(activeEffect), 0f, 0f, 0f, 0f, 0f);
        }
      }
    }

    worldView.begin();
    effects.updateAndDraw(Gdx.graphics.getDeltaTime(), worldView.cam);
    worldView.end();

    // Render game gui elements
    screenView.begin();
    sb.setLength(0);
    sb.append("Active effect: ");
    sb.append(effectList.get(activeEffect).name);
//    sb.append("\nEmitters: ");
//    sb.append(effects.getActiveEmitterCount());
    sb.append("\nParticles: ");
    sb.append(effects.getLiveParticlesCount());
    sb.append("\nVertices: ");
    sb.append(profiler.getVertexCount());
//    if (effects.isBufferFull()) {
//      sb.append("\n(WARNING: BUFFER FULL)");
//    }
    sb.append("\n");
    sb.append(BOTTOM_TEXT);
    textBounds.setText(font14, sb);
    font14.draw(screenView.batch, sb, 10, textBounds.height + 25);

    if (Attributes.SHOW_FPS) {
      sb.setLength(0);
      sb.append(Gdx.graphics.getFramesPerSecond());
      textBounds.setText(font14, sb);
      font14.draw(screenView.batch, sb, Gdx.graphics.getWidth() / 2 - textBounds.width / 2,
          textBounds.height + 30);
    }
    screenView.end();

    profiler.reset();
  }

  @Override
  public boolean handleTouchDown(float worldX, float worldY, int button) {
    effects.start(effectList.get(activeEffect).name, worldX, worldY, 0f, 0f, 0f);
    return true;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    if (amountY < 0.0) {
      worldView.cam.zoom *= 0.8f;
      if (worldView.cam.zoom < 1f) {
        worldView.cam.zoom = 1f;
      }
    } else {
      worldView.cam.zoom *= 1.2f;
      if (worldView.cam.zoom > 8f) {
        worldView.cam.zoom = 8f;
      }
    }
    return true;
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {

  }
}
