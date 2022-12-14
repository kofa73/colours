package kofa.colours;

import kofa.maths.Matrix3;
import kofa.maths.Vector3D;

public abstract class RGB<S extends RGB<S>> implements Vector3D {
    public final float r;
    public final float g;
    public final float b;

    RGB(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    RGB(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    protected abstract Matrix3<S, XYZ> toXyzMatrix();

    public XYZ toXYZ() {
        return toXyzMatrix().multipliedBy((S) this);
    }

    public Luv toLuv() {
        // override if space does not use D65
        return Converter.convert_XYZ_to_Luv_D65(toXYZ());
    }

    public LCh toLCh() {
        return Converter.convert_Luv_to_LCH_uv(toLuv());
    }

    @Override
    public float[] toFloats() {
        return new float[]{r, g, b};
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), r, g, b);
    }
}
