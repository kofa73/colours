package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.stream.DoubleStream;

import static java.lang.Math.abs;

public class CIEXYZ extends Vector3<CIEXYZ> {
    public static final CIEXYZ BLACK = new CIEXYZ(0, 0, 0);
    public static final double BLACK_LEVEL = 1E-6;
    // http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    public static final CIEXYZ D65_WHITE_ASTM_E308_01 = new CIEXYZ(0.95047, 1, 1.08883);
    // alternatively, IEC 61966-2-1, https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final CIEXYZ D65_WHITE_IEC_61966_2_1 = new CIEXYZ(0.9504559, 1, 1.0890578);

    public static final CIEXYZ D65_WHITE_2DEGREE_STANDARD_OBSERVER = CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER.toXyz();
    public static final CIEXYZ D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = CIExyY.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.toXyz();

    public final double X;
    public final double Y;
    public final double Z;

    public CIEXYZ(double X, double Y, double Z) {
        super(X, Y, Z);
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }

    @Override
    public DoubleStream coordinates() {
        return DoubleStream.of(X, Y, Z);
    }

    @Override
    public String toString() {
        return Vector3.format(this, X, Y, Z);
    }

    public boolean isBlack() {
        return -BLACK_LEVEL < Y && Y < BLACK_LEVEL;
    }

    public boolean isWhite() {
        return abs(1 - Y) < 1E-6;
    }

    @Override
    public final <O extends Vector3<O>> O multiplyBy(SpaceConversionMatrix<CIEXYZ, O> conversionMatrix) {
        return multiplyBy(conversionMatrix, X, Y, Z);
    }
}
