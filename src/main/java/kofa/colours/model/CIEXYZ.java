package kofa.colours.model;

import kofa.maths.Vector3;

import static java.lang.Math.abs;

public class CIEXYZ extends Vector3 {
    public static final CIEXYZ BLACK = new CIEXYZ(0, 0, 0);
    public static final double BLACK_LEVEL = 1E-6;
    // http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    public static final CIEXYZ D65_WHITE_ASTM_E308_01 = new CIEXYZ(0.95047, 1, 1.08883);
    // alternatively, IEC 61966-2-1, https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final CIEXYZ D65_WHITE_IEC_61966_2_1 = new CIEXYZ(0.9504559, 1, 1.0890578);

    public static final CIEXYZ D65_WHITE_2DEGREE_STANDARD_OBSERVER = CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER.toXyz();
    public static final CIEXYZ D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = CIExyY.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.toXyz();

    public CIEXYZ(double X, double Y, double Z) {
        super(X, Y, Z);
    }

    public double X() {
        return coordinate1;
    }

    public double Y() {
        return coordinate2;
    }

    public double Z() {
        return coordinate3;
    }

    public boolean isBlack() {
        return -BLACK_LEVEL < Y() && Y() < BLACK_LEVEL;
    }

    public boolean isWhite() {
        return abs(1 - Y()) < 1E-6;
    }


    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(this.getClass().getSimpleName(), X(), Y(), Z());
    }
}
