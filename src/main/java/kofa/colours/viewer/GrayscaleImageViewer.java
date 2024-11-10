package kofa.colours.viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class GrayscaleImageViewer extends JFrame {

    
    public GrayscaleImageViewer(int[] pixels, int width, int height, String windowTitle) {
        BufferedImage image = createImageFrom16BitGray(pixels, width, height);
        
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
        setTitle(windowTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(scrollPane);
        pack();
        setLocationRelativeTo(null);
    }
    
    private BufferedImage createImageFrom16BitGray(int[] pixels, int width, int height) {
        // Create compatible buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);

        short[] pixelValues = new short[width * height];
        for (int i = 0; i < pixelValues.length; i++) {
            pixelValues[i] = (short) ((pixels[i] * 65535 / 4095) & 0xffff);
        }
        
        // Get raster and set pixels
        WritableRaster raster = image.getRaster();
        raster.setDataElements(0, 0, width, height, pixelValues);
        
        return image;
    }
    
    public static void show(int width, int height, int[] pixels, String windowTitle) {
        SwingUtilities.invokeLater(() -> {
            GrayscaleImageViewer viewer = new GrayscaleImageViewer(pixels, width, height, windowTitle);
            viewer.setVisible(true);
        });
    }
}