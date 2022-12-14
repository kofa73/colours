package kofa.colours;

import kofa.maths.Vector3D;

public record LCh_uv(float L, float C, float h) implements Vector3D {
    public LCh_uv(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{L, C, h};
    }

    public Luv toLuv() {
        float u = (float) (C * Math.cos(h));
        float v = (float) (C * Math.sin(h));
        return new Luv(L, u, v);
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h);
    }
}
