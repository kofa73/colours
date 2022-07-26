package kofa.colours;

import kofa.maths.SquareMatrix;

public class Converter {
    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    public static final SquareMatrix REC2020_TO_XYZ = new SquareMatrix(3)
            .row(0.6369580f, 0.1446169f, 0.1688810f)
            .row(0.2627002f, 0.6779981f, 0.0593017f)
            .row(0.0000000f, 0.0280727f,  1.0609851f);

    public static final SquareMatrix XYZ_TO_REC2020 = new SquareMatrix(3)
            .row(1.7166512f, -0.3556708f, -0.2533663f)
            .row(-0.6666844f, 1.6164812f, 0.0157685f)
            .row(0.0176399f, -0.0427706f, 0.9421031f);

    // http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html - sRGB D65
    public static final SquareMatrix LINEAR_SRGB_TO_XYZ = new SquareMatrix(3)
            .row(0.4124564f, 0.3575761f, 0.1804375f)
            .row(0.2126729f, 0.7151522f, 0.0721750f)
            .row(0.0193339f, 0.1191920f, 0.9503041f);

    public static final SquareMatrix XYZ_TO_LINEAR_SRGB = new SquareMatrix(3)
            .row(3.2404542f, -1.5371385f, -0.4985314f)
            .row(-0.9692660f, 1.8760108f, 0.0415560f)
            .row(0.0556434f, -0.2040259f, 1.0572252f);

    float[] convert(float[] values, SquareMatrix conversionMatrix) {
        return conversionMatrix.multipliedBy(values);
    }
}
