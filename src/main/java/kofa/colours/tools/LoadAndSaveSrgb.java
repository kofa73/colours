package kofa.colours.tools;

import kofa.colours.gamutmapper.*;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;

public class LoadAndSaveSrgb {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Provide params: infile-linearRec2020 outfile-sRGB-with-gamma transformer(s)");
            System.out.println("Transformer 0: Null transformer, will die if input is out of sRGB gamut");
            System.out.println("Transformer 1: Clip RGB");
            System.out.println("Transformer 2: BW using L");
            System.out.println("Transformer 3: Desaturate by scaling C of LCh(ab)");
            System.out.println("Transformer 4: Desaturate by scaling C of LCh(uv)");
            System.out.println("Transformer 5: Clip C of LCh(ab)");
            System.out.println("Transformer 6: Clip C of LCh(uv)");
            System.out.println("Transformer 7: Dampen C of LCh(ab), shoulder = 0");
            System.out.println("Transformer 8: Dampen C of LCh(uv), shoulder = 0");
            System.out.println("Transformer 9: Dampen C of LCh(ab), shoulder = 0.5");
            System.out.println("Transformer 10: Dampen C of LCh(uv), shoulder = 0.5");
            System.out.println("Transformer 11: Dampen C of LCh(ab), shoulder = 0.7");
            System.out.println("Transformer 12: Dampen C of LCh(uv), shoulder = 0.7");
            System.out.println("Transformer 13: Dampen C of LCh(ab), shoulder = 0.9");
            System.out.println("Transformer 14: Dampen C of LCh(uv), shoulder = 0.9");
            System.exit(1);
        }

        var image = new ImageLoader().loadImageFrom(args[0]);

        for (int paramIndex = 2; paramIndex < args.length; paramIndex++) {
            image.init();

            var transformerId = Integer.parseInt(args[paramIndex]);

            var transformer = switch (transformerId) {
                case 0 -> new NullGamutMapper();
                case 1 -> new RgbClippingGamutMapper();
                case 2 -> new BwFromLGamutMapper();
                case 3 -> DesaturatingLchBasedGamutMapper.forLchAb(image);
                case 4 -> DesaturatingLchBasedGamutMapper.forLchUv(image);
                case 5 -> ChromaClippingLchBasedGamutMapper.forLchAb();
                case 6 -> ChromaClippingLchBasedGamutMapper.forLchUv();
                case 7 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0);
                case 8 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0);
                case 9 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.5);
                case 10 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.5);
                case 11 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.7);
                case 12 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.7);
                case 13 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.9);
                case 14 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.9);
                default -> throw new IllegalArgumentException("Unsupported transformer: " + transformerId);
            };

            System.out.println("Using " + transformer.name());
            transformer.transform(image);

            new PngOutput().write(args[1] + "-" + transformer.name(), image);
        }
    }
}
