package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.PngOutput;

public class LoadAndSave {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Provide params: infile outfile");
            System.exit(1);
        }
        var image = new ImageLoader().loadImageFrom(args[0]);
        new PngOutput().write(args[1], image);
    }
}
