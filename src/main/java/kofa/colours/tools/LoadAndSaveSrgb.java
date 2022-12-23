package kofa.colours.tools;

import kofa.colours.gamutmapper.*;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;

import java.util.stream.DoubleStream;

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

            System.out.println("Transformer 15: Darken highlights, dampen C of LCh(ab), shoulder = 0");
            System.out.println("Transformer 16: Darken highlights, dampen C of LCh(uv), shoulder = 0");
            System.out.println("Transformer 17: Darken highlights, dampen C of LCh(ab), shoulder = 0.5");
            System.out.println("Transformer 18: Darken highlights, dampen C of LCh(uv), shoulder = 0.5");
            System.out.println("Transformer 19: Darken highlights, dampen C of LCh(ab), shoulder = 0.7");
            System.out.println("Transformer 20: Darken highlights, dampen C of LCh(uv), shoulder = 0.7");
            System.out.println("Transformer 21: Darken highlights, dampen C of LCh(ab), shoulder = 0.9");
            System.out.println("Transformer 22: Darken highlights, dampen C of LCh(uv), shoulder = 0.9");
            System.exit(1);
        }

        var image = new ImageLoader().loadImageFrom(args[0]);

        for (int paramIndex = 2; paramIndex < args.length; paramIndex++) {

            var transformerId = Integer.parseInt(args[paramIndex]);

            // FIXME: this is only for testing
            DoubleStream.of(0, 0.3, 0.5, 0.7, 0.8, 0.9, 0.95).forEach(lShoulder -> {
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
                    case 13 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.90);
                    case 14 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.90);

                    case 15 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchAb(0, lShoulder);
                    case 16 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchUv(0, lShoulder);
                    case 17 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchAb(0.5, lShoulder);
                    case 18 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchUv(0.5, lShoulder);
                    case 19 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchAb(0.7, lShoulder);
                    case 20 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchUv(0.7, lShoulder);
                    case 21 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchAb(0.90, lShoulder);
                    case 22 -> GradualChromaDampeningAndDarkeningLchBasedGamutMapper.forLchUv(0.90, lShoulder);

                    default -> throw new IllegalArgumentException("Unsupported transformer: " + transformerId);
                };

                if (lShoulder == 0 || transformer instanceof GradualChromaDampeningAndDarkeningLchBasedGamutMapper) {
                    System.out.println("Using " + transformer.name());
                    transformer.transform(image);
                    new PngOutput().write(args[1] + "-" + transformer.name(), image);
                }
            });
        }
    }
}
