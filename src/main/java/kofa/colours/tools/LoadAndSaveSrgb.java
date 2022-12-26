package kofa.colours.tools;

import kofa.colours.gamutmapper.*;
import kofa.colours.tonemapper.LabToneMapper;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;

import java.util.Arrays;
import java.util.stream.IntStream;

public class LoadAndSaveSrgb {
    public static void main(String[] args) {
        if (args.length < 2) {
            printHelpAndExit();
        }

        int[] mapperIds = parseMapperIds(args);

        String inputFile = args[0];
        String baseName = inputFile.substring(0, inputFile.lastIndexOf("."));

        var image = new ImageLoader().loadImageFrom(inputFile);

//        var toneMapper = new XyzToneMapper(image);
        var toneMapper = new LabToneMapper(image);

        for (int mapperId : mapperIds) {
            image.init();

            toneMapper.toneMap(image);

            GamutMapper gamutMapper = getGamutMapper(image, mapperId);

            if (gamutMapper != null) {
                System.out.println("Using " + gamutMapper.name());
                gamutMapper.mapToSrgb(image);
                new PngOutput().write(baseName + "-" + toneMapper.getClass().getSimpleName() + "-" + gamutMapper.name(), image);
            } else {
                printHelpAndExit();
            }
        }
    }

    private static int[] parseMapperIds(String[] args) {
        int[] ids = null;
        try {
            ids = IntStream.range(1, args.length)
                    .mapToObj(argIndex -> args[argIndex])
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (NumberFormatException nfe) {
            System.out.printf("%s %s%n%n", nfe.getClass().getSimpleName(), nfe.getMessage());
            printHelpAndExit();
        }
        int[] invalidIds = Arrays.stream(ids).filter(i -> i < 1 || i > 14).toArray();
        if (invalidIds.length > 0) {
            System.out.printf("Found invalid ids: %s%n%n", Arrays.toString(invalidIds));
            printHelpAndExit();
        }
        return ids;
    }

    private static GamutMapper getGamutMapper(RgbImage image, int transformerId) {
        return switch (transformerId) {
            case 0 -> new NullGamutMapper();
            case 1 -> new SrgbClippingGamutMapper();
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

            default -> null;
        };
    }

    private static void printHelpAndExit() {
        System.out.println("Usage:");
        System.out.println("Provide params: infile-linearRec2020.tif|png|jpg transformer(s)");
        System.out.println("Output will be written as in PNG format to the same file name + name of transformer");
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
}
