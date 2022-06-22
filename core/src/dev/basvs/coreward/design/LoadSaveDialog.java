package dev.basvs.coreward.design;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.io.File;

public class LoadSaveDialog extends Dialog {

  public LoadSaveDialog(File directory, String extension, String title, Skin skin) {
    super(title, skin);

    Label fileLabel = new Label("Name: ", skin);
    add(fileLabel);
    final TextField fileField = new TextField("", skin);
    add(fileField).width(400).row();

    final List<String> fileList = new List<>(skin);
    if (!directory.exists() || !directory.isDirectory()) {
      throw new RuntimeException("Directory not found or invalid: " + directory.getAbsolutePath());
    }
    for (String file : directory.list()) {
      if (file.endsWith("." + extension)) {
        File f = new File(file);
        fileList.getItems()
            .add(f.getName().substring(0, f.getName().length() - extension.length() - 1));
      }
    }
    fileList.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        fileField.setText(fileList.getSelected());
      }
    });
    ScrollPane scrollP = new ScrollPane(fileList, skin);
    add(scrollP).padTop(10f).height(150f).colspan(3).fill().row();

    Table buttonTable = new Table(skin);
    add(buttonTable).colspan(3).fillX();

    TextButton okButton = new TextButton("OK", skin);
    okButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        LoadSaveDialog.this.fire(new DialogEvent(fileField.getText()));
        LoadSaveDialog.this.remove();
      }
    });
    buttonTable.add(okButton).expandX().right();
    TextButton cancelButton = new TextButton("Cancel", skin);
    cancelButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        LoadSaveDialog.this.remove();
      }
    });
    buttonTable.add(cancelButton);
  }

  public class DialogEvent extends Event {

    Object result;

    public DialogEvent(Object result) {
      this.result = result;
    }

    public Object getResult() {
      return result;
    }
  }
}
