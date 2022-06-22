package dev.basvs.coreward.design;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.combat.CombatScreen;
import dev.basvs.coreward.design.modules.ModuleDesign;
import dev.basvs.lib.DoubleTextureRegionDrawable;
import dev.basvs.lib.Logging;
import dev.basvs.lib.game.AbstractGame;
import dev.basvs.lib.game.AbstractScreen;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class ShipDesignScreen extends AbstractScreen {

  static final String BOTTOM_TEXT = "Coreward prototype - designer, 2022";

  private final BitmapFont font14;
  private final GlyphLayout textBounds = new GlyphLayout();
  private final Requirements requirements;
  private final TextureRegion gridTexture, comTexture, cotTexture, mirrorTexture;
  private final TextureRegion[] conduitTexture = new TextureRegion[16];
  private final ArrayList<ModuleDesign> modules;
  private final Json json;
  private final TextureAtlas atlas;
  private final Vector2 com = new Vector2(), cotF = new Vector2(), cotB = new Vector2(), cotL = new Vector2(), cotR = new Vector2();
  private ButtonGroup moduleButtons;
  private CheckBox comCheckBox;
  private CheckBox cotCheckBox;
  private CheckBox mirrorCheckBox;
  private Label reqLabelCore, reqLabelForward, reqLabelRotation, reqLabelWhole;
  private TextButton testButton;
  private ShipDesign design;
  private int direction = 0;
  private boolean leftDown, rightDown, middleDown;

  public ShipDesignScreen(AbstractGame game) {
    super(game);

    font14 = game.getAssets().get("azertype14.fnt", BitmapFont.class);

    atlas = game.getAssets().get("coreward.atlas");

    gridTexture = atlas.findRegion("editor/grid");
    comTexture = atlas.findRegion("editor/com");
    cotTexture = atlas.findRegion("editor/cot");
    mirrorTexture = atlas.findRegion("editor/mirror");

    for (int i = 0; i < conduitTexture.length; i++) {
      conduitTexture[i] = atlas.findRegion("modules/conduit", i);
    }

    json = new Json();
    json.setSerializer(DesignCell.class, new Json.Serializer<>() {
      public void write(Json json, DesignCell cell, Class knownType) {
        json.writeObjectStart();
        if (cell.module != null) {
          String modString = cell.module.code + ";" + cell.direction + ";" + cell.reserved;
          json.writeValue("m", modString);
        } else {
          json.writeValue("m", "");
        }
        json.writeObjectEnd();
      }

      public DesignCell read(Json json, JsonValue jsonData, Class type) {
        DesignCell cell = new DesignCell();
        String data = jsonData.getString("m");
        if (!data.isEmpty()) {
          String[] parts = data.split(";");
          cell.module = getModuleByCode(parts[0]);
          cell.direction = Integer.parseInt(parts[1]);
          cell.reserved = Boolean.parseBoolean(parts[2]);
        }
        return cell;
      }
    });

    modules = json.fromJson(ArrayList.class, ModuleDesign.class,
        Gdx.files.internal("modules.json"));

    setupGui();

    requirements = new Requirements();
    design = loadDesign(new File(Constants.DESIGNS_DIRECTORY, Constants.DESIGNS_DEFAULT + "."
        + Constants.DESIGNS_EXTENSION));
    doCalculations(design);
  }

  ShipDesign loadDesign(File fileName) {
    ShipDesign des;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      des = json.fromJson(ShipDesign.class, reader);
      reader.close();
    } catch (Exception e) {
      des = new ShipDesign("default", Constants.DESIGNS_SIZE, Constants.DESIGNS_SIZE);
    }
    for (int x = 0; x < des.width; x++) {
      for (int y = 0; y < des.height; y++) {
        des.cells[x][y].gridX = x;
        des.cells[x][y].gridY = y;
      }
    }
    Button checked = moduleButtons.getChecked();
    if (checked != null) {
      checked.setChecked(false);
    }
    return des;
  }

  ModuleDesign getModuleByCode(String code) {
    for (ModuleDesign module : modules) {
      if (module.code.equals(code)) {
        return module;
      }
    }
    return null;
  }

  private void setupGui() {
    Table moduleMenu = new Table();
    moduleMenu.defaults().pad(3);
    guiRoot.add(moduleMenu).expand().top().left();

    moduleButtons = new ButtonGroup();
    moduleButtons.setMinCheckCount(0);
    int col = 0;
    for (ModuleDesign module : modules) {
      Button moduleButton;
      if (module.conduit != null) {
        DoubleTextureRegionDrawable drawable = new DoubleTextureRegionDrawable(
            atlas.findRegion(module.texture),
            atlas.findRegion(module.conduit.texture, 0));
        moduleButton = new Button(new Image(drawable), game.guiSkin, "toggle");
      } else if (module.weapon != null) {
        DoubleTextureRegionDrawable drawable = new DoubleTextureRegionDrawable(
            atlas.findRegion(module.texture),
            atlas.findRegion(module.weapon.nozzleTexture));
        moduleButton = new Button(new Image(drawable), game.guiSkin, "toggle");
      } else {
        moduleButton = new Button(new Image(atlas.findRegion(module.texture)), game.guiSkin,
            "toggle");
      }
      moduleButton.setDisabled(!module.core);
      moduleMenu.add(moduleButton);
      moduleButton.setUserObject(module);
      moduleButtons.add(moduleButton);
      if (++col >= 2) {
        moduleMenu.row();
        col = 0;
      }
    }

    Table rightBar = new Table();
    guiRoot.add(rightBar).expand().top().right();
    Window systemMenu = new Window("System", game.guiSkin);
    systemMenu.defaults().width(60).pad(2);
    rightBar.add(systemMenu).row();

    TextButton newButton = new TextButton("New", game.guiSkin);
    newButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        new Dialog("Please confirm", game.guiSkin) {
          protected void result(Object object) {
            if ((Boolean) object) {
              design = new ShipDesign("default", Constants.DESIGNS_SIZE, Constants.DESIGNS_SIZE);
              Button checked = moduleButtons.getChecked();
              if (checked != null) {
                checked.setChecked(false);
              }
              doCalculations(design);
            }
          }
        }.text("Start a new design?").button("Yes", true).button("No", false).key(Keys.ENTER, true)
            .key(Keys.ESCAPE, false).show(guiStage);
      }
    });
    systemMenu.add(newButton);

    TextButton exitButton = new TextButton("Exit", game.guiSkin);
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setRemoveAfterFadeOut(true);
        game.deactivateScreen();
      }
    });
    systemMenu.add(exitButton).row();

    TextButton loadButton = new TextButton("Load", game.guiSkin);
    loadButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        final LoadSaveDialog fileDialog = new LoadSaveDialog(Constants.DESIGNS_DIRECTORY, "json",
            "Load design",
            game.guiSkin);
        fileDialog.addListener(event1 -> {
          if (event1 instanceof LoadSaveDialog.DialogEvent) {
            try {
              String fileName = (String) ((LoadSaveDialog.DialogEvent) event1).getResult();
              Logging.info("file selected for loading: " + fileName);
              design = loadDesign(new File(Constants.DESIGNS_DIRECTORY, fileName + "."
                  + Constants.DESIGNS_EXTENSION));
            } catch (Exception e) {
              Logging.error("Could not load design file", e);
            }
            return true;
          } else {
            return false;
          }
        });
        fileDialog.show(guiStage);
      }
    });
    systemMenu.add(loadButton);

    TextButton saveButton = new TextButton("Save", game.guiSkin);
    saveButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        final LoadSaveDialog fileDialog = new LoadSaveDialog(Constants.DESIGNS_DIRECTORY, "json",
            "Save design",
            game.guiSkin);
        fileDialog.addListener(event12 -> {
          if (event12 instanceof LoadSaveDialog.DialogEvent) {
            String fileName = (String) ((LoadSaveDialog.DialogEvent) event12).getResult();
            Logging.info("filename selected for saving: " + fileName);
            try {
              BufferedWriter writer = new BufferedWriter(
                  new FileWriter(new File(Constants.DESIGNS_DIRECTORY,
                      fileName + "." + Constants.DESIGNS_EXTENSION)));
              writer.write(json.toJson(design));
              writer.close();
            } catch (Exception e) {
              Logging.error("Could not save design file", e);
            }
            return true;
          } else {
            return false;
          }
        });
        fileDialog.show(guiStage);
      }
    });
    systemMenu.add(saveButton).row();

    Window testMenu = new Window("Test", game.guiSkin);
    testMenu.defaults().width(124).pad(2);
    rightBar.add(testMenu).row();

    reqLabelCore = new Label("Core", game.guiSkin);
    testMenu.add(reqLabelCore).row();
    reqLabelForward = new Label("Forward thrust", game.guiSkin);
    testMenu.add(reqLabelForward).row();
    reqLabelRotation = new Label("Rotation", game.guiSkin);
    testMenu.add(reqLabelRotation).row();
    reqLabelWhole = new Label("One shape", game.guiSkin);
    testMenu.add(reqLabelWhole).row();

    testButton = new TextButton("Test", game.guiSkin);
    testButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        try {
          ShipDesignScreen.this.setRemoveAfterFadeOut(false);
          ShipDesign enemyDesign = loadDesign(new File(Constants.DESIGNS_DIRECTORY, "enemy" + "."
              + Constants.DESIGNS_EXTENSION));
          CombatScreen combatScreen = new CombatScreen(game, design, enemyDesign);
          game.activateScreen(combatScreen);
        } catch (Exception e) {
          java.util.logging.Logger.getLogger(ShipDesignScreen.class.getName())
              .log(Level.SEVERE, null, e);
        }
      }
    });
    testMenu.add(testButton).expandX().fillX().width(Value.maxWidth);

    Window editorMenu = new Window("Editor", game.guiSkin);
    editorMenu.defaults().pad(2).left();
    rightBar.add(editorMenu).fillX().expandX();

    comCheckBox = new CheckBox("CoM marker", game.guiSkin);
    editorMenu.add(comCheckBox).row();
    cotCheckBox = new CheckBox("CoT marker", game.guiSkin);
    editorMenu.add(cotCheckBox).row();
    mirrorCheckBox = new CheckBox("Mirror", game.guiSkin);
    editorMenu.add(mirrorCheckBox);
  }

  @Override
  public void onReactivate() {
    worldView.setPos(0, 0);
  }

  @Override
  public void onUpdate(float delta) {
    // Is there a core module in the design?
    boolean corePlaced = false;
    for (int x = 0; x < design.width; x++) {
      for (int y = 0; y < design.height; y++) {
        ModuleDesign module = design.cells[x][y].module;
        if (module != null && module.core) {
          corePlaced = true;
          break;
        }
      }
    }
    // Set button status depending on whether a core module is in the design
    for (int i = 0; i < moduleButtons.getButtons().size; i++) {
      Button btn = (Button) moduleButtons.getButtons().get(i);
      ModuleDesign module = (ModuleDesign) btn.getUserObject();
      if (corePlaced) {
        btn.setDisabled(module.core);
      } else {
        btn.setDisabled(!module.core);
      }
    }
    // Handle module placement / removal
    if (leftDown) {
      int gridX = toGridX(inputWorldPos.x);
      int gridY = toGridY(inputWorldPos.y);
      if (gridX != -1 && gridY != -1) {
        Button checked = moduleButtons.getChecked();
        if (checked != null) {
          ModuleDesign module = (ModuleDesign) moduleButtons.getChecked().getUserObject();
          if (isValidPlacement(gridX, gridY, module)) {
            DesignCell cell = design.cells[gridX][gridY];
            cell.module = module;
            cell.direction = direction;
            setReserved(gridX, gridY, direction, module.freeSpace, true);
            if (module.core) {
              checked.setChecked(false);
            } else {
              if (mirrorCheckBox.isChecked()) {
                if (gridX != design.width / 2) {
                  int mirrorX = design.width / 2 - (gridX - design.width / 2);
                  if (isValidPlacement(mirrorX, gridY, module)) {
                    DesignCell mCell = design.cells[mirrorX][gridY];
                    mCell.module = module;
                    if (module.triangular) {
                      switch (direction) {
                        case 0 -> mCell.direction = 3;
                        case 1 -> mCell.direction = 2;
                        case 2 -> mCell.direction = 1;
                        case 3 -> mCell.direction = 0;
                      }
                    } else {
                      switch (direction) {
                        case 0 -> mCell.direction = 0;
                        case 1 -> mCell.direction = 3;
                        case 2 -> mCell.direction = 2;
                        case 3 -> mCell.direction = 1;
                      }
                    }
                    setReserved(mirrorX, gridY, mCell.direction, module.freeSpace, true);
                  }
                }
              }
            }
            doCalculations(design);
          }
        }
      }
    } else if (rightDown) {
      int gridX = toGridX(inputWorldPos.x);
      int gridY = toGridY(inputWorldPos.y);
      if (gridX != -1 && gridY != -1) {
        if (design.cells[gridX][gridY].module != null) {
          if (design.cells[gridX][gridY].module.core) {
            Button checked = moduleButtons.getChecked();
            if (checked != null) {
              checked.setChecked(false);
            }
          } else if (mirrorCheckBox.isChecked()) {
            if (gridX != design.width / 2) {
              int mirrorX = design.width / 2 - (gridX - design.width / 2);
              if (design.cells[mirrorX][gridY].module != null) {
                setReserved(mirrorX, gridY, design.cells[mirrorX][gridY].direction,
                    design.cells[mirrorX][gridY].module.freeSpace, false);
                design.cells[mirrorX][gridY].clear();
              }
            }
          }
          setReserved(gridX, gridY, design.cells[gridX][gridY].direction,
              design.cells[gridX][gridY].module.freeSpace, false);
          design.cells[gridX][gridY].clear();
          doCalculations(design);
        }
      }
    }
  }

  @Override
  public void onRender(float timeDeltaSeconds) {
    worldView.begin();

    int designWidthPx = design.width * Constants.CELL_SIZE_PX;
    int designHeightPx = design.height * Constants.CELL_SIZE_PX;

    // Draw grid
    for (int x = 0; x < design.width + 1; x++) {
      float lineX = x * Constants.CELL_SIZE_PX - designWidthPx / 2 - 1;
      float dst =
          1f - MathUtils.clamp(Math.abs((inputWorldPos.x - lineX)) / (4f * Constants.CELL_SIZE_PX),
              0f, 1f);
      float alpha = 0.25f + dst * dst * 0.75f;
      if (x == design.width / 2 || x == design.width / 2 + 1) {
        // Center of the grid is drawn red
        worldView.batch.setColor(1f, 0.2f, 0.2f, alpha);
      } else {
        worldView.batch.setColor(1f, 1f, 1f, alpha);
      }
      worldView.batch.draw(gridTexture, lineX, -designHeightPx / 2 - 1, 2,
          design.height * Constants.CELL_SIZE_PX);
    }
    for (int y = 0; y < design.height + 1; y++) {
      float lineY = y * Constants.CELL_SIZE_PX - designHeightPx / 2 - 1;
      float dst =
          1f - MathUtils.clamp(Math.abs((inputWorldPos.y - lineY)) / (4f * Constants.CELL_SIZE_PX),
              0f, 1f);
      float alpha = 0.25f + dst * dst * 0.75f;
      if (y == design.height / 2 || y == design.height / 2 + 1) {
        // Center of the grid is drawn red
        worldView.batch.setColor(1f, 0.2f, 0.2f, alpha);
      } else {

        worldView.batch.setColor(1f, 1f, 1f, alpha);
      }
      worldView.batch.draw(gridTexture, -designWidthPx / 2 - 1, lineY,
          design.width * Constants.CELL_SIZE_PX, 2);
    }
    worldView.batch.setColor(Color.WHITE);

    // Draw ship design
    for (int x = 0; x < design.width; x++) {
      for (int y = 0; y < design.height; y++) {
        ModuleDesign module = design.cells[x][y].module;
        if (module != null) {
          TextureRegion tex = atlas.findRegion(module.texture);
          worldView.batch.draw(tex, x * Constants.CELL_SIZE_PX - designWidthPx / 2,
              y * Constants.CELL_SIZE_PX
                  - designHeightPx / 2, Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX / 2,
              Constants.CELL_SIZE_PX, Constants.CELL_SIZE_PX, 1f, 1f,
              directionToDegrees(design.cells[x][y].direction));
          if (module.conduit != null) {
            byte index = 0;
            index = (byte) (index | (isConduit(x, y + 1) << 0));
            index = (byte) (index | (isConduit(x + 1, y) << 1));
            index = (byte) (index | (isConduit(x, y - 1) << 2));
            index = (byte) (index | (isConduit(x - 1, y) << 3));
            worldView.batch.draw(atlas.findRegion(module.conduit.texture, index),
                x * Constants.CELL_SIZE_PX
                    - designWidthPx / 2, y * Constants.CELL_SIZE_PX - designHeightPx / 2,
                Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX,
                Constants.CELL_SIZE_PX, 1f, 1f, directionToDegrees(0));
          }
          if (module.weapon != null) {
            tex = atlas.findRegion(module.weapon.nozzleTexture);
            worldView.batch.draw(tex, x * Constants.CELL_SIZE_PX - designWidthPx / 2,
                y * Constants.CELL_SIZE_PX
                    - designHeightPx / 2, Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX / 2,
                Constants.CELL_SIZE_PX, Constants.CELL_SIZE_PX, 1f, 1f,
                directionToDegrees(design.cells[x][y].direction));
          }
        }
      }
    }

    // Draw indicators
    if (comCheckBox.isChecked()) {
      if (com.x != 0 || com.y != 0) {
        worldView.batch.draw(comTexture,
            com.x * Constants.WORLD_TO_SCREEN - comTexture.getRegionWidth() / 2
                - designWidthPx / 2,
            com.y * Constants.WORLD_TO_SCREEN - comTexture.getRegionHeight() / 2
                - designHeightPx / 2);
      }
    }
    if (cotCheckBox.isChecked()) {
      if (cotF.x != 0 || cotF.y != 0) {
        worldView.batch.draw(cotTexture,
            cotF.x * Constants.WORLD_TO_SCREEN - cotTexture.getRegionWidth() / 2
                - designWidthPx / 2,
            cotF.y * Constants.WORLD_TO_SCREEN - cotTexture.getRegionHeight() / 2
                - designHeightPx / 2, cotTexture.getRegionWidth() / 2,
            cotTexture.getRegionHeight() / 2,
            cotTexture.getRegionWidth(), cotTexture.getRegionHeight(), 1f, 1f,
            directionToDegrees(0));
      }
      if (cotR.x != 0 || cotR.y != 0) {
        worldView.batch.draw(cotTexture,
            cotR.x * Constants.WORLD_TO_SCREEN - cotTexture.getRegionWidth() / 2
                - designWidthPx / 2,
            cotR.y * Constants.WORLD_TO_SCREEN - cotTexture.getRegionHeight() / 2
                - designHeightPx / 2, cotTexture.getRegionWidth() / 2,
            cotTexture.getRegionHeight() / 2,
            cotTexture.getRegionWidth(), cotTexture.getRegionHeight(), 1f, 1f,
            directionToDegrees(1));
      }
      if (cotB.x != 0 || cotB.y != 0) {
        worldView.batch.draw(cotTexture,
            cotB.x * Constants.WORLD_TO_SCREEN - cotTexture.getRegionWidth() / 2
                - designWidthPx / 2,
            cotB.y * Constants.WORLD_TO_SCREEN - cotTexture.getRegionHeight() / 2
                - designHeightPx / 2, cotTexture.getRegionWidth() / 2,
            cotTexture.getRegionHeight() / 2,
            cotTexture.getRegionWidth(), cotTexture.getRegionHeight(), 1f, 1f,
            directionToDegrees(2));
      }
      if (cotL.x != 0 || cotL.y != 0) {
        worldView.batch.draw(cotTexture,
            cotL.x * Constants.WORLD_TO_SCREEN - cotTexture.getRegionWidth() / 2
                - designWidthPx / 2,
            cotL.y * Constants.WORLD_TO_SCREEN - cotTexture.getRegionHeight() / 2
                - designHeightPx / 2, cotTexture.getRegionWidth() / 2,
            cotTexture.getRegionHeight() / 2,
            cotTexture.getRegionWidth(), cotTexture.getRegionHeight(), 1f, 1f,
            directionToDegrees(3));
      }
    }

    // Draw mirror line
    if (mirrorCheckBox.isChecked()) {
      for (int y = 0; y < design.height + 1; y++) {
        float yPos = y * Constants.CELL_SIZE_PX - designHeightPx / 2 - 1;
        worldView.batch.draw(mirrorTexture, -mirrorTexture.getRegionWidth() / 2,
            yPos - mirrorTexture.getRegionHeight() / 2);
      }
    }

    // Draw currently selected module
    // TODO move to separate method
    if (moduleButtons.getChecked() != null) {
      ModuleDesign module = (ModuleDesign) moduleButtons.getChecked().getUserObject();
      TextureRegion tex = atlas.findRegion(module.texture);
      int x = toGridX(inputWorldPos.x);
      int y = toGridY(inputWorldPos.y);
      if (x != -1 && y != -1) {
        if (isValidPlacement(x, y, module)) {
          worldView.batch.setColor(0.7f, 1f, 0.7f, 0.7f);
        } else {
          worldView.batch.setColor(1f, 0.7f, 0.7f, 0.7f);
        }
        worldView.batch.draw(tex, x * Constants.CELL_SIZE_PX - designWidthPx / 2,
            y * Constants.CELL_SIZE_PX
                - designHeightPx / 2, Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX / 2,
            Constants.CELL_SIZE_PX,
            Constants.CELL_SIZE_PX, 1f, 1f, directionToDegrees(direction));
        worldView.batch.setColor(Color.WHITE);
        if (module.weapon != null) {
          tex = atlas.findRegion(module.weapon.nozzleTexture);
          worldView.batch.draw(tex, x * Constants.CELL_SIZE_PX - designWidthPx / 2,
              y * Constants.CELL_SIZE_PX
                  - designHeightPx / 2, Constants.CELL_SIZE_PX / 2, Constants.CELL_SIZE_PX / 2,
              Constants.CELL_SIZE_PX, Constants.CELL_SIZE_PX, 1f, 1f,
              directionToDegrees(direction));
        }
      }
    }
    worldView.end();

    screenView.begin();
    textBounds.setText(font14, BOTTOM_TEXT);
    font14.draw(screenView.batch, BOTTOM_TEXT, Gdx.graphics.getWidth() / 2 - textBounds.width / 2,
        textBounds.height + 10);
    screenView.end();
  }

  byte isConduit(int gridX, int gridY) {
    if (gridX >= 0 && gridX < design.width && gridY >= 0 && gridY < design.height) {
      if (design.cells[gridX][gridY].module != null
          && design.cells[gridX][gridY].module.isEnergyConductor()) {
        return 1;
      }
    }
    return 0;
  }

  boolean isValidPlacement(int gridX, int gridY, ModuleDesign m) {
    if (design.cells[gridX][gridY].module != null || design.cells[gridX][gridY].reserved) {
      return false;
    }
    return true;
  }

  @Override
  public boolean handleTouchDown(float worldX, float worldY, int button) {
    if (button == Buttons.LEFT) {
      leftDown = true;
    } else if (button == Buttons.RIGHT) {
      rightDown = true;
    } else if (button == Buttons.MIDDLE) {
      middleDown = true;
    }
    return true;
  }

  @Override
  public boolean handleTouchUp(float worldX, float worldY, int button) {
    if (button == Buttons.LEFT) {
      leftDown = false;
    } else if (button == Buttons.RIGHT) {
      rightDown = false;
    } else if (button == Buttons.MIDDLE) {
      middleDown = false;
    }
    return true;
  }

  @Override
  public boolean handlePan(float worldX, float worldY, float deltaX, float deltaY) {
    if (middleDown) {
      Vector3 pos = worldView.cam.position;
      pos.add(-deltaX, -deltaY, 0f);
      float top = design.height * Constants.CELL_SIZE_PX / 2;
      float bottom = -design.height * Constants.CELL_SIZE_PX / 2;
      float left = -design.width * Constants.CELL_SIZE_PX / 2;
      float right = design.width * Constants.CELL_SIZE_PX / 2;
      if (pos.x > right) {
        pos.x = right;
      }
      if (pos.x < left) {
        pos.x = left;
      }
      if (pos.y > top) {
        pos.y = top;
      }
      if (pos.y < bottom) {
        pos.y = bottom;
      }
      worldView.update();
    }
    return false;
  }

  public int toGridX(float worldX) {
    int x = MathUtils.floor(
        (worldX + (design.width * Constants.CELL_SIZE_PX) / 2) / Constants.CELL_SIZE_PX);
    if (x < 0 || x > design.width - 1) {
      x = -1;
    }
    return x;
  }

  public int toGridY(float worldY) {
    int y = MathUtils.floor(
        (worldY + (design.height * Constants.CELL_SIZE_PX) / 2) / Constants.CELL_SIZE_PX);
    if (y < 0 || y > design.height - 1) {
      y = -1;
    }
    return y;
  }

  public float directionToDegrees(int direction) {
    return -90f * direction;
  }

  void doCalculations(ShipDesign des) {
    float massCount = 0;
    com.set(0, 0);
    cotF.set(0, 0);
    float cotFcount = 0;
    cotB.set(0, 0);
    float cotBcount = 0;
    cotL.set(0, 0);
    float cotLcount = 0;
    cotR.set(0, 0);
    float cotRcount = 0;
    for (int x = 0; x < des.width; x++) {
      for (int y = 0; y < des.height; y++) {
        ModuleDesign module = des.cells[x][y].module;
        if (module != null) {
          float m = module.density;
          if (module.triangular) {
            m *= 0.5f;
          }
          com.add(x * m, y * m);
          massCount += m;
          if (module.thruster != null) {
            switch (des.cells[x][y].direction) {
              case 0:
                cotFcount += module.thruster.force;
                cotF.add(x * module.thruster.force, y * module.thruster.force);
                break;
              case 1:
                cotRcount += module.thruster.force;
                cotR.add(x * module.thruster.force, y * module.thruster.force);
                break;
              case 2:
                cotBcount += module.thruster.force;
                cotB.add(x * module.thruster.force, y * module.thruster.force);
                break;
              case 3:
                cotLcount += module.thruster.force;
                cotL.add(x * module.thruster.force, y * module.thruster.force);
                break;
            }
          }
        }
      }
    }
    com.scl(1f / massCount).add(0.5f, 0.5f);
    cotF.scl(1f / cotFcount).add(0.5f, 0);
    cotB.scl(1f / cotBcount).add(0.5f, 1f);
    cotL.scl(1f / cotLcount).add(1f, 0.5f);
    cotR.scl(1f / cotRcount).add(0f, 0.5f);
    // Check the requirements
    requirements.check(des);
    // Update labels
    reqLabelCore.setColor(requirements.core ? Color.GREEN : Color.RED);
    reqLabelForward.setColor(requirements.forwardThrust ? Color.GREEN : Color.RED);
    reqLabelRotation.setColor(requirements.rotation ? Color.GREEN : Color.RED);
    reqLabelWhole.setColor(requirements.oneShape ? Color.GREEN : Color.RED);
    // Enable test button
    testButton.setDisabled(!requirements.allOk());
  }

  void setReserved(int gridX, int gridY, int direction, int count, boolean state) {
    for (int i = 1; i < count + 1; i++) {
      switch (direction) {
        case 0:
          if (gridY - i < 0) {
            return;
          }
          design.cells[gridX][gridY - i].reserved = state;
          break;
        case 1:
          if (gridX - i < 0) {
            return;
          }
          design.cells[gridX - i][gridY].reserved = state;
          break;
        case 2:
          if (gridY + i >= design.height) {
            return;
          }
          design.cells[gridX][gridY + i].reserved = state;
          break;
        case 3:
          if (gridX + i >= design.width) {
            return;
          }
          design.cells[gridX + i][gridY].reserved = state;
          break;
      }
    }
  }

  @Override
  public void pinchStop() {
    throw new UnsupportedOperationException(
        "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    direction = (int) ((direction - amountY) % 4);
    if (direction < 0) {
      direction += 4;
    }
    return true;
  }

  @Override
  public void changed(ChangeEvent event, Actor actor) {

  }
}
