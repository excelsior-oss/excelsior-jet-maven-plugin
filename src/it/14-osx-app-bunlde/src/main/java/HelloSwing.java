import javax.swing.*;
import java.awt.event.*;

public class HelloSwing {

    public static void main(String args[]) {
        JFrame frame = new JFrame("HelloSwing");
        frame.setSize(50, 50);
        JLabel label = new JLabel("Hello, Swing!", 0);
        frame.add(label);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                System.exit(0);
            }
        });
        frame.show();
    }

}