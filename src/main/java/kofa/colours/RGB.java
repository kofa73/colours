package kofa.colours;

import kofa.maths.FloatVector;

public record RGB(float R, float G, float B) implements FloatVector {
    public RGB(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{R, G, B};
    }
}
