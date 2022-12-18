package kofa;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import kofa.image.raw.RawImage;

public class ImageLoader {
    public RawImage loadImageFrom(String path) {
        long start, stop;

        start = System.currentTimeMillis();
        BufferedImage image;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stop = System.currentTimeMillis();
        System.out.println("\tImageIO.read took " + (stop - start) + " ms");

        start = System.currentTimeMillis();
        // createChild is for debugging, to only load a small part 
        Raster raster = image.getRaster(); //.createChild(1000, 1000, 100, 100, 0, 0, null);
        stop = System.currentTimeMillis();
        System.out.println("\timage.getRaster took " + (stop - start) + " ms");

        start = System.currentTimeMillis();
        RawImage rawImage = new RawImage(raster);
        stop = System.currentTimeMillis();
        System.out.println("\tnew RawImage took " + (stop - start) + " ms");

        return rawImage;
    }
}
