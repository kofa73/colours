package kofa.colours.tools;

import kofa.colours.transformer.*;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;

public class LoadAndSaveSrgb {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Provide params: infile-linearRec2020 outfile-sRGB-with-gamma transformer(s)");
            System.out.println("Transformer 0: Null transformer");
            System.out.println("Transformer 1: Clip RGB");
            System.out.println("Transformer 2: BW using L");
            System.out.println("Transformer 3: Desaturate by scaling C of LCh(ab)");
            System.out.println("Transformer 4: Desaturate by scaling C of LCh(uv)");
            System.out.println("Transformer 5: Clip C of LCh(ab)");
            System.out.println("Transformer 6: Clip C of LCh(uv)");
            System.exit(1);
        }

        var image = new ImageLoader().loadImageFrom(args[0]);

        for (int paramIndex = 2; paramIndex < args.length; paramIndex++) {
            image.init();

            var transformerId = Integer.parseInt(args[paramIndex]);

            var transformer = switch (transformerId) {
                case 0 -> new NullTransformer();
                case 1 -> new RgbClippingTransformer();
                case 2 -> new BwFromLTransformer();
                case 3 -> new DesaturatingLabTransformer(image);
                case 4 -> new DesaturatingLuvTransformer(image);
                case 5 -> new ClippingLabTransformer();
                case 6 -> new ClippingLuvTransformer();
                default -> throw new IllegalArgumentException("Unsupported transformer: " + transformerId);
            };

            System.out.println("Using " + transformer.getClass().getSimpleName());
            transformer.transform(image);

            new PngOutput().write(args[1] + "-" + transformer.getClass().getSimpleName(), image);
        }
    }
}
