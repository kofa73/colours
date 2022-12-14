package kofa.colours;

import kofa.maths.Vector3D;

public record Luv(float L, float u, float v) implements Vector3D {
    public Luv(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{L, u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, u, v);
    }
}
