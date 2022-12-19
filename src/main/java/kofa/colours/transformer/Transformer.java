package kofa.colours.transformer;

import kofa.colours.Lab;
import kofa.colours.Rec2020;
import kofa.colours.SRGB;
import kofa.colours.XYZ;
import kofa.io.RgbImage;

import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class Transformer {
    public void transform(RgbImage image) {
        var red = image.redChannel();
        var green = image.greenChannel();
        var blue = image.blueChannel();
        IntStream.range(0, image.height()).parallel().forEach(row -> {
                    for (int column = 0; column < image.width(); column++) {
                        var transformed = transform(red[row][column], green[row][column], blue[row][column]);
//                        if (transformed[0] < 0 || transformed[0] > 1 ||
//                            transformed[1] < 0 || transformed[1] > 1 ||
//                            transformed[2] < 0 || transformed[2] > 1)
//                        {
//                            die(red, green, blue, row, column, transformed);
//                        }
                        transformed[0] = ensure01(transformed[0]);
                        red[row][column] = ensure01(applyGamma(transformed[0]));
                        green[row][column] = ensure01(applyGamma(transformed[1]));
                        blue[row][column] = ensure01(applyGamma(transformed[2]));
                    }
                }
        );
    }

    private double ensure01(double value) {
        if (value < -1.0 / 65535 / 2 || value > 1 + 1.0 / 65535 / 2) {
            throw new RuntimeException("out of gamut: " + value);
        }
        return min(1, max(0, value));
    }

    private static final double LINEAR_THRESHOLD = 0.0031308;

    private double applyGamma(double linear) {
        return linear <= LINEAR_THRESHOLD ?
                12.92 * linear :
                1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
    }

    private void die(double[][] red, double[][] green, double[][] blue, int row, int column, double[] transformed) {
        var rec2020 = new Rec2020(red[row][column], green[row][column], blue[row][column]);
        var srgb = new SRGB(transformed);
        throw new IllegalArgumentException(
                rec2020 + " = " + Lab.from(rec2020.toXYZ()).usingD65().toLCh() + " was mapped to " + srgb + " = " + Lab.from(srgb.toXYZ()).usingD65().toLCh()
        );
    }

    private double[] transform(double red, double green, double blue) {
        var xyz = new Rec2020(red, green, blue).toXYZ();
        return getInsideGamut(xyz);
    }

    protected abstract double[] getInsideGamut(XYZ xyz);
}
