package kofa.colours;

import kofa.maths.FloatVector;

public record LCh(float L, float C, float h) implements FloatVector {
    public LCh(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{L, C, h};
    }
}
