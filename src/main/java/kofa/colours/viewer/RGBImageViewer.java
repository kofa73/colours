package kofa.colours.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.round;

public class RGBImageViewer extends JFrame {
    private final BufferedImage image;
    
    public RGBImageViewer(int[] rgbData, int width, int height) {
        // Create RGB image from the data
        image = createImageFromRGB(rgbData, width, height);
        
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
        scrollPane.setPreferredSize(new Dimension(800, 600)); // Default window size
        
        // Setup frame
        setTitle("RGB Image Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
    }
    
    private BufferedImage createImageFromRGB(int[] rgbData, int width, int height) {
        // Create compatible buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Convert separate RGB values to packed pixels
        int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            // 12-bit raw -> 0..4095, needs to map to 0..255
            int r = round(255 * rgbData[i * 3] / 4095f);
            int g = round(255 * rgbData[i * 3 + 1] / 4095f);
            int b = round(255 * rgbData[i * 3 + 2] / 4095f);
            pixels[i] = (r << 16) | (g << 8) | b;
        }
        
        // Set the pixels
        image.setRGB(0, 0, width, height, pixels, 0, width);
        
        return image;
    }
    
    public static void show(int[] rgbData, int width, int height) {
        SwingUtilities.invokeLater(() -> {
            RGBImageViewer viewer = new RGBImageViewer(rgbData, width, height);
            viewer.setVisible(true);
        });
    }
}