package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.io.RgbImage;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class GamutMapper {
    private final boolean processInGamutPixels;

    protected GamutMapper() {
        this.processInGamutPixels = false;
    }

    GamutMapper(boolean processInGamutPixels) {
        this.processInGamutPixels = processInGamutPixels;
    }

    public void mapToSrgb(RgbImage image) {
        image.transformAllPixels((row, column, red, green, blue) -> {
            Srgb transformed = mapToSrgb(red, green, blue, row, column);
            return new double[]{
                    applyGamma(transformed.r()),
                    applyGamma(transformed.g()),
                    applyGamma(transformed.b())
            };
        });
    }

    private static final double LINEAR_THRESHOLD = 0.0031308;

    private double applyGamma(double linear) {
        return linear <= LINEAR_THRESHOLD ?
                12.92 * linear :
                1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
    }

    private Srgb mapToSrgb(
            double rec2020Red,
            double rec2020Green,
            double rec2020Blue,
            int row, int column
    ) {
        var xyz = new Rec2020(rec2020Red, rec2020Green, rec2020Blue).toXyz();
        var sRgb = Srgb.from(xyz);
        if (processInGamutPixels || sRgb.isOutOfGamut()) {
            sRgb = getInsideGamut(xyz);
        }
        return ensurePixelIsWithinGamut(sRgb, row, column, rec2020Red, rec2020Green, rec2020Blue);
    }

    private Srgb ensurePixelIsWithinGamut(
            Srgb mappedPixel,
            int row, int column,
            double rec2020Red, double rec2020Green, double rec2020Blue) {
        for (double value : mappedPixel.coordinates()) {
            // if error is greater than what 16-bit integer rounding would mask, die
            if (value < -1.0 / 65535 / 2 || value > 1 + 1.0 / 65535 / 2) {
                var rec2020 = new Rec2020(rec2020Red, rec2020Green, rec2020Blue);
                var xyzIn = rec2020.toXyz();
                var labIn = CieLab.from(xyzIn).usingD65();
                var luvIn = CieLuv.from(xyzIn).usingD65();
                var xyzOut = mappedPixel.toXyz();
                var labOut = CieLab.from(xyzOut).usingD65();
                var luvOut = CieLuv.from(xyzOut).usingD65();
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
                                labIn, labIn.toLch(),
                                luvIn, luvIn.toLch(),

                                mappedPixel,
                                xyzOut,
                                labOut, labOut.toLch(),
                                luvOut, luvOut.toLch()
                        )
                );
            }
        }
        // clip away any remaining tiny error
        return mappedPixel.isOutOfGamut() ?
                new Srgb(
                        min(1, max(0, mappedPixel.r())),
                        min(1, max(0, mappedPixel.g())),
                        min(1, max(0, mappedPixel.b()))
                )
                : mappedPixel;
    }

    protected abstract Srgb getInsideGamut(Xyz xyz);

    public String name() {
        return this.getClass().getSimpleName();
    }
}
