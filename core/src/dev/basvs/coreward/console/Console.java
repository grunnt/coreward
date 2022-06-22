package dev.basvs.coreward.console;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import dev.basvs.coreward.Constants;
import dev.basvs.coreward.console.attributes.AbstractAttr;
import dev.basvs.lib.Logging;

public class Console extends Dialog {

  Array<AbstractAttr> attrs;

  TextArea textArea;
  TextField commandField;
  StringBuilder sb;
  String lastCommand = "";

  public Console(Skin skin) {
    super("Console", skin);
    setFillParent(true);
    attrs = new Array<AbstractAttr>();
    sb = new StringBuilder("> Welcome to the console.\n");
    textArea = new TextArea(sb.toString(), skin);
    textArea.setFocusTraversal(false);
    getContentTable().add(textArea).expand().fill();
    commandField = new TextField("< ", skin);
    commandField.setTextFieldListener(new TextFieldListener() {

      @Override
      public void keyTyped(TextField textField, char key) {
        if ((key == '\r' || key == '\n')) {
          String command = textField.getText().trim();
          if (command.startsWith("<")) {
            command = command.substring(1).trim();
          }
          lastCommand = command;
          String response = processCommand(command);
          sb.append("> ");
          sb.append(response);
          sb.append("\n");
          textArea.setText(sb.toString());
          textArea.setCursorPosition(textArea.getText().length());
          textField.setText("< ");
          textField.setCursorPosition(textField.getText().length());
        } else if (key == 18) {
          textField.setText("< " + lastCommand);
          textField.setCursorPosition(textField.getText().length());
        }
      }
    });
    getButtonTable().add(commandField).expand().fill().prefWidth(1024);
    key(Constants.CONSOLE_KEY, null);
  }

  public void addAttribute(AbstractAttr attr) {
    attrs.add(attr);
  }

  public Dialog show(Stage stage) {
    Dialog d = super.show(stage);
    stage.setKeyboardFocus(commandField);
    commandField.setCursorPosition(commandField.getText().length());
    return d;
  }

  String processCommand(String command) {
    String response = null;
    try {
      String[] parts = command.split(" ");
      String cmd = parts[0].toLowerCase().trim();
      if (cmd.equals("set")) {
        if (parts.length > 2) {
          String attr = parts[1].trim().toLowerCase();
          String val = parts[2].trim().toLowerCase();
          for (int i = 0; i < attrs.size; i++) {
            if (attrs.get(i).getName().equals(attr)) {
              attrs.get(i).set(val);
              response =
                  "Attribute " + attrs.get(i).getName() + " set to " + attrs.get(i).get() + ".";
              break;
            }
          }
        } else {
          response = "I'm sorry but I'm afraid I'm missing an attribute or value argument.";
        }
      } else if (cmd.equals("get")) {
        if (parts.length > 1) {
          String attr = parts[1].trim().toLowerCase();
          for (int i = 0; i < attrs.size; i++) {
            if (attrs.get(i).getName().equals(attr)) {
              response = "Attribute " + attrs.get(i).getName() + " is " + attrs.get(i).get() + ".";
              break;
            }
          }
        } else {
          response = "Attributes: \n  ";
          for (int i = 0; i < attrs.size; i++) {
            response += attrs.get(i).getName() + " " + attrs.get(i).get() + "\n  ";
          }
        }
      } else {
        response = "I'm sorry but I'm afraid I cannot do: " + command;
      }
    } catch (Exception e) {
      response = "I'm sorry but I'm afraid an error has occurred.";
      Logging.error("Exception while processing console command " + command, e);
    }
    return response;
  }
}
