package kofa.colours.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.*;

public class RGBImageViewer extends JFrame {
    private final BufferedImage image;

    public RGBImageViewer(String title, float[] rgbData, int width, int height, float additionalGamma) {
        // Create RGB image from the data
        image = createImageFromRGB(rgbData, width, height, additionalGamma);
        
        // Create image panel
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(image.getWidth(), image.getHeight());
            }
        };
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setPreferredSize(new Dimension(1920, 1080)); // Default window size
        
        // Setup frame
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
    }
    
    private BufferedImage createImageFromRGB(float[] rgbData, int width, int height, float additionalGamma) {
        // Create compatible buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        float max = 0;
        for (float value : rgbData) {
            max = max(max, value);
        }

        float gamma = 2.2f // sRGB-ish
                + additionalGamma; // for brightness
        float exponent = 1 / gamma;

        // Convert separate RGB values to packed pixels
        int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            // quick sRGB-ish TRC
            int r = (int) round(255 * pow(rgbData[i * 3] / max, exponent));
            int g = (int) round(255 * pow(rgbData[i * 3 + 1] / max, exponent));
            int b = (int) round(255 * pow(rgbData[i * 3 + 2] / max, exponent));
            pixels[i] = (r << 16) | (g << 8) | b;
        }
        
        // Set the pixels
        image.setRGB(0, 0, width, height, pixels, 0, width);
        
        return image;
    }

    public static void show(String title, float[] rgbData, int width, int height, float additionalGamma) {
        SwingUtilities.invokeLater(() -> {
            RGBImageViewer viewer = new RGBImageViewer(title, rgbData, width, height, additionalGamma);
            viewer.setVisible(true);
        });
    }
}