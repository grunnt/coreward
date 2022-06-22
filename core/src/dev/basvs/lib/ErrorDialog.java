package dev.basvs.lib;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog {

  public ErrorDialog(JFrame frame, String message, String details) {
    super(frame, true);
    setSize(612, 412);
    setResizable(false);
    setTitle("Oops! Something went wrong...");
    setLayout(new BorderLayout());

    JLabel messageLabel = new JLabel(message);
    messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD, 15));
    messageLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(messageLabel, BorderLayout.NORTH);

    JTextArea detailTextArea = new JTextArea("Detais of the error:\n" + details);
    detailTextArea.setEditable(false);
    JScrollPane sp = new JScrollPane(detailTextArea);
    Dimension size = new Dimension(590, 350);
    sp.setPreferredSize(size);
    sp.setMinimumSize(size);
    sp.setMaximumSize(size);
    add(sp, BorderLayout.CENTER);

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    add(okButton, BorderLayout.SOUTH);

    setLocationRelativeTo(frame);
    pack();
    setVisible(true);
  }

  public static void show(JFrame frame, String message, Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    new ErrorDialog(frame, message, sw.toString());
  }
}
