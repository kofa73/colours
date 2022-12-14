package kofa.colours;

import kofa.maths.Vector3D;

public record XYZ(float X, float Y, float Z) implements Vector3D {
    public XYZ(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{X, Y, Z};
    }
}
