package kofa.colours.transformer;

import kofa.colours.model.*;
import kofa.io.RgbImage;

import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class Transformer {
    private final boolean skipGamutCheck;

    protected Transformer() {
        this.skipGamutCheck = false;
    }

    Transformer(boolean skipGamutCheck) {
        this.skipGamutCheck = skipGamutCheck;
    }

    public void transform(RgbImage image) {
        var red = image.redChannel();
        var green = image.greenChannel();
        var blue = image.blueChannel();
        IntStream.range(0, image.height()).parallel().forEach(row -> {
                    for (int column = 0; column < image.width(); column++) {
                        var transformed = transform(red[row][column], green[row][column], blue[row][column]);
                        ensurePixelIsWithinGamut(transformed, red, green, blue, row, column);
                        red[row][column] = applyGamma(transformed[0]);
                        green[row][column] = applyGamma(transformed[1]);
                        blue[row][column] = applyGamma(transformed[2]);
                    }
                }
        );
    }

    private void ensurePixelIsWithinGamut(
            double[] transformedPixel,
            double[][] red,
            double[][] green,
            double[][] blue,
            int row, int column) {
        for (double value : transformedPixel) {
            // if error is greater than what 16-bit integer rounding would mask, die
            if (value < -1.0 / 65535 / 2 || value > 1 + 1.0 / 65535 / 2) {
                var rec2020 = new Rec2020(red[row][column], green[row][column], blue[row][column]);
                var xyzIn = rec2020.toXYZ();
                var labIn = Lab.from(xyzIn).usingD65();
                var luvIn = Luv.from(xyzIn).usingD65();
                SRGB srgb = new SRGB(transformedPixel);
                var xyzOut = srgb.toXYZ();
                var labOut = Lab.from(xyzOut).usingD65();
                var luvOut = Luv.from(xyzOut).usingD65();
                throw new RuntimeException(
                        (
                                "out of gamut at [%d, %d]:%n" +
                                        "Input:%n" +
                                        "\t%s%n" +
                                        "\t%s%n" +
                                        "\t%s %s %n" +
                                        "\t%s %s%n" +
                                        "Output:%n" +
                                        "\t%s%n" +
                                        "\t%s%n" +
                                        "\t%s %s %n" +
                                        "\t%s %s%n"
                        ).formatted(
                                row, column,
                                rec2020,
                                xyzIn,
                                labIn, labIn.toLCh(),
                                luvIn, luvIn.toLCh(),

                                srgb,
                                xyzOut,
                                labOut, labOut.toLCh(),
                                luvOut, luvOut.toLCh()
                        )
                );
            }
        }
        // clip away any remaining tiny error
        transformedPixel[0] = min(1, max(0, transformedPixel[0]));
        transformedPixel[1] = min(1, max(0, transformedPixel[1]));
        transformedPixel[2] = min(1, max(0, transformedPixel[2]));
    }

    private static final double LINEAR_THRESHOLD = 0.0031308;

    private double applyGamma(double linear) {
        return linear <= LINEAR_THRESHOLD ?
                12.92 * linear :
                1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
    }

    private double[] transform(double red, double green, double blue) {
        var xyz = new Rec2020(red, green, blue).toXYZ();
        var srgb = SRGB.from(xyz);
        return (skipGamutCheck || srgb.isOutOfGamut()) ?
                getInsideGamut(xyz) :
                srgb.values();
    }

    protected abstract double[] getInsideGamut(XYZ xyz);
}
