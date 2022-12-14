package kofa.maths;

public interface Vector3D<T> extends FloatVector {
    //    Constructor<T> constructor();
    interface Constructor<T> {
        T createNew(float[] values);
    }
}
