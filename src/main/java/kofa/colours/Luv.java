package kofa.colours;

import kofa.maths.FloatVector;

public record Luv(float L, float u, float v) implements FloatVector {
    public Luv(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{L, u, v};
    }
}
