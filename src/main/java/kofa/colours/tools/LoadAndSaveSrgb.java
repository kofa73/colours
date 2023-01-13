package kofa.colours.tools;

import kofa.colours.gamutmapper.*;
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

        for (int mapperId : mapperIds) {
            image.init();

            GamutMapper gamutMapper = getGamutMapper(image, mapperId);

            if (gamutMapper != null) {
                System.out.println("Using " + gamutMapper.name());
                gamutMapper.mapToSrgb(image);
                new PngOutput().write(baseName + "-" + gamutMapper.name(), image);
            } else {
                printHelpAndExit();
            }
        }
    }

    private static int[] parseMapperIds(String[] args) {
        if (args.length == 2 && args[1].equals("all")) {
            return IntStream.rangeClosed(1, 17).toArray();
        }

        int[] ids = null;
        try {
            ids = Arrays.stream(args, 1, args.length)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (NumberFormatException nfe) {
            System.out.printf("%s %s%n%n", nfe.getClass().getSimpleName(), nfe.getMessage());
            printHelpAndExit();
        }
        int[] invalidIds = Arrays.stream(ids).filter(i -> i < 1 || i > 18).toArray();
        if (invalidIds.length > 0) {
            System.out.printf("Found invalid ids: %s%n%n", Arrays.toString(invalidIds));
            printHelpAndExit();
        }
        return ids;
    }

    private static GamutMapper getGamutMapper(RgbImage image, int transformerId) {
        return switch (transformerId) {
            case 0 -> new NullGamutMapper();
            case 1 -> new SrgbClippingGamutMapper(image);
            case 2 -> new BwFromCieLabLGamutMapper(image);

            case 3 -> ChromaClippingLchBasedGamutMapper.forLchAb(image);
            case 4 -> ChromaClippingLchBasedGamutMapper.forLchUv(image);
            case 5 -> ChromaClippingLchBasedGamutMapper.forOkLch(image);

            case 6 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0, image);
            case 7 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0, image);
            case 8 -> GradualChromaDampeningLchBasedGamutMapper.forOkLch(0, image);
            case 9 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.5, image);
            case 10 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.5, image);
            case 11 -> GradualChromaDampeningLchBasedGamutMapper.forOkLch(0.5, image);
            case 12 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.7, image);
            case 13 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.7, image);
            case 14 -> GradualChromaDampeningLchBasedGamutMapper.forOkLch(0.7, image);
            case 15 -> GradualChromaDampeningLchBasedGamutMapper.forLchAb(0.9, image);
            case 16 -> GradualChromaDampeningLchBasedGamutMapper.forLchUv(0.9, image);
            case 17 -> GradualChromaDampeningLchBasedGamutMapper.forOkLch(0.9, image);

            default -> null;
        };
    }

    private static void printHelpAndExit() {
        System.out.println("Usage:");
        System.out.println("Provide params: infile-linearRec2020.tif|png|jpg mappers(s)");
        System.out.println("Output will be written as in PNG format to the same file name + name of mapper");
        System.out.println("Gamut mappers: the word 'all' (equivalent to listing all mappers except for 0), or the following:");
        System.out.println("Transformer 0: Null transformer, will die if input is out of sRGB gamut");
        System.out.println("Transformer 1: Clip RGB");
        System.out.println("Transformer 2: BW using L");

        System.out.println("Transformer 3: Clip C of LCh(ab)");
        System.out.println("Transformer 4: Clip C of LCh(uv)");
        System.out.println("Transformer 5: Clip C of OKLCh");

        System.out.println("Transformer  6: Dampen C of LCh(ab), shoulder = 0");
        System.out.println("Transformer  7: Dampen C of LCh(uv), shoulder = 0");
        System.out.println("Transformer  8: Dampen C of OKLCh,   shoulder = 0");

        System.out.println("Transformer  9: Dampen C of LCh(ab), shoulder = 0.5");
        System.out.println("Transformer 10: Dampen C of LCh(uv), shoulder = 0.5");
        System.out.println("Transformer 11: Dampen C of OKLCh,   shoulder = 0.5");

        System.out.println("Transformer 12: Dampen C of LCh(ab), shoulder = 0.7");
        System.out.println("Transformer 13: Dampen C of LCh(uv), shoulder = 0.7");
        System.out.println("Transformer 14: Dampen C of OKLCh,   shoulder = 0.7");

        System.out.println("Transformer 15: Dampen C of LCh(ab), shoulder = 0.9");
        System.out.println("Transformer 16: Dampen C of LCh(uv), shoulder = 0.9");
        System.out.println("Transformer 17: Dampen C of OKLCh,   shoulder = 0.9");

        System.exit(1);
    }
}
