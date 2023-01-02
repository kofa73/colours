package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.colours.tonemapper.ToneMapper;
import kofa.io.RgbImage;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Converts an image from Rec2020 to sRGB, in place.
 */
public abstract class GamutMapper {
    private final boolean processInGamutPixels;

    protected GamutMapper(ToneMapper<?> toneMapper, RgbImage image) {
        this(false, toneMapper, image);
    }

    protected GamutMapper(boolean processInGamutPixels, ToneMapper<?> toneMapper, RgbImage image) {
        this.processInGamutPixels = processInGamutPixels;
        image.init();
        toneMapper.toneMap(image);
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

    private Srgb mapToSrgb(
            double rec2020Red,
            double rec2020Green,
            double rec2020Blue,
            int row, int column
    ) {
        var sRgb = new Rec2020(rec2020Red, rec2020Green, rec2020Blue).toSRGB();
        if (processInGamutPixels || sRgb.isOutOfGamut()) {
            sRgb = getInsideGamut(sRgb);
        }
        return ensurePixelIsWithinGamut(sRgb, row, column, rec2020Red, rec2020Green, rec2020Blue);
    }

    private Srgb ensurePixelIsWithinGamut(
            Srgb mappedPixel,
            int row, int column,
            double rec2020Red, double rec2020Green, double rec2020Blue) {
        // if error is greater than what 16-bit integer rounding would mask, die
        if (mappedPixel.anyCoordinateMatches(value -> value < -1.0 / 65535 / 2 || value > 1 + 1.0 / 65535 / 2)) {
            throw exception(mappedPixel, row, column, rec2020Red, rec2020Green, rec2020Blue);
        }
        return mappedPixel.isOutOfGamut() ?
                // clip away any remaining tiny error
                new Srgb(
                        min(1, max(0, mappedPixel.r())),
                        min(1, max(0, mappedPixel.g())),
                        min(1, max(0, mappedPixel.b()))
                ) :
                mappedPixel;
    }

    private static RuntimeException exception(Srgb mappedPixel, int row, int column, double rec2020Red, double rec2020Green, double rec2020Blue) {
        var rec2020 = new Rec2020(rec2020Red, rec2020Green, rec2020Blue);
        var xyzIn = rec2020.toXyz();
        var cieLabIn = CIELAB.from(xyzIn).usingD65_2DegreeStandardObserver();
        var cieLuvIn = CIELUV.from(xyzIn).usingD65_2DegreeStandardObserver();
        var okLabIn = OkLAB.from(xyzIn);
        var xyzOut = mappedPixel.toXyz();
        var cieLabOut = CIELAB.from(xyzOut).usingD65_2DegreeStandardObserver();
        var cieLuvOut = CIELUV.from(xyzOut).usingD65_2DegreeStandardObserver();
        var okLabOut = OkLAB.from(xyzOut);
        return new RuntimeException(
                (
                        "out of gamut at [%d, %d]:%n" +
                                "Input:%n" +
                                "\t%s%n" +
                                "\t%s%n" +
                                "\t%s %s %n" +
                                "\t%s %s%n" +
                                "\t%s %s%n" +
                                "Output:%n" +
                                "\t%s%n" +
                                "\t%s%n" +
                                "\t%s %s %n" +
                                "\t%s %s %n" +
                                "\t%s %s%n"
                ).formatted(
                        row, column,
                        rec2020,
                        xyzIn,
                        cieLabIn, cieLabIn.toLch(),
                        cieLuvIn, cieLuvIn.toLch(),
                        okLabIn, okLabIn.toLch(),

                        mappedPixel,
                        xyzOut,
                        cieLabOut, cieLabOut.toLch(),
                        cieLuvOut, cieLuvOut.toLch(),
                        okLabOut, okLabOut.toLch()
                )
        );
    }

    protected abstract Srgb getInsideGamut(Srgb xyz);

    public String name() {
        return this.getClass().getSimpleName();
    }

    private static final double LINEAR_THRESHOLD = 0.0031308;

    private double applyGamma(double linear) {
        return linear <= LINEAR_THRESHOLD ?
                12.92 * linear :
                1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
    }
}
