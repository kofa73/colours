package kofa.colours;

import kofa.maths.Vector3D;

public record LCh(float L, float C, float h) implements Vector3D {
    public LCh(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{L, C, h};
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h);
    }
}
