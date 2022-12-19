package kofa.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

public class ImageLoader {
    public RgbImage loadImageFrom(String path) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // createChild is for debugging, to only load a small part
        Raster raster = image.getRaster(); //.createChild(1000, 1000, 100, 100, 0, 0, null);

        return new RgbImage(raster);
    }
}
