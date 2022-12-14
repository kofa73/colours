package kofa.colours;

import kofa.maths.FloatVector;

public record UV(float u, float v) implements FloatVector {
    public UV(float[] floats) {
        this(floats[0], floats[1]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f]".formatted(getClass().getSimpleName(), u, v);
    }

}
