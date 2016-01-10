package bg.uni_sofia.fmi.ai;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;

/**
 * Created by yordan on 1/10/2016.
 */
public class Visualizer {
    private JLabel imageView;

    public void show(Image image, String windowName){
        JFrame frame = createJFrame(windowName);
        imageView.setIcon(new ImageIcon(image));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JFrame createJFrame(String windowName) {
        JFrame frame = new JFrame(windowName);
        imageView = new JLabel();
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.setPreferredSize(new Dimension(640, 480));
        frame.add(imageScrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }
}
