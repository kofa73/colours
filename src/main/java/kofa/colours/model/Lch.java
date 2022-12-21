package kofa.colours.model;

import kofa.maths.Vector3D;

import static java.lang.Math.*;

/**
 * LCh representation
 */
public interface Lch extends Vector3D {
    double L();

    double C();

    double h();

    static double[] fromPolar(double L, double C, double h) {
        return new double[]{L, C * cos(h), C * sin(h)};
    }

    default double[] withHueInDegrees() {
        double[] values = coordinates();
        var hue = values[2];
        var hueDegrees = toDegrees(hue);
        values[2] = hueDegrees > 360 ? hueDegrees - 360 : (hueDegrees < 0 ? hueDegrees + 360 : hueDegrees);
        return values;
    }
}
