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

    public LCh_uv toLch_uv() {
        float C = length(u, v);
        float h = (float) Math.atan2(v, u);
        return new LCh_uv(L, C, h);
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, u, v);
    }

    private float length(float u, float v) {
        return (float) Math.sqrt(u * u + v * v);
    }
}
