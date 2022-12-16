package kofa.colours;

import kofa.maths.Vector3D;

import static java.lang.Math.toDegrees;

public interface LCh extends Vector3D {
    default double[] withHueInDegrees() {
        double[] values = values();
        var hue = values[2];
        var hueDegrees = toDegrees(hue);
        values[2] = hueDegrees > 360 ? hueDegrees - 360 : (hueDegrees < 0 ? hueDegrees + 360 : hueDegrees);
        return values;
    }
}
