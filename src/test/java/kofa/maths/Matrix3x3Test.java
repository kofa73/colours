package kofa.maths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Matrix3x3Test {
    private final Matrix3x3<Source, Interim> sourceToInterim = new Matrix3x3<>(
            Interim::new,
            2, 3, 5,
            7, 11, 13,
            17, 19, 23
    );

    @Test
    void multiply_vector() {
        // given
        // when
        Interim result = sourceToInterim.multiply(new Source(29, 31, 37));

        // then
        assertThat(result.values()).containsExactly(336, 1025, 1933);
    }

    @Test
    void multiply_matrix() {
        // given
        var interimToTarget = new Matrix3x3<Interim, Target>(
                Target::new,
                29, 31, 37,
                41, 43, 47,
                53, 59, 61
        );

        // when
        Matrix3x3<Source, Target> result = interimToTarget.multiply(sourceToInterim);

        // then
        assertThat(result.values()[0]).containsExactly(904, 1131, 1399);
        assertThat(result.values()[1]).containsExactly(1182, 1489, 1845);
        assertThat(result.values()[2]).containsExactly(1556, 1967, 2435);
    }

    @Test
    void associativity() {
        var source = new Source(67, 71, 73);

        var interim = sourceToInterim.multiply(source);

        var interimToTarget = new Matrix3x3<Interim, Target>(
                Target::new,
                29, 31, 37,
                41, 43, 47,
                53, 59, 61
        );

        var target1 = interimToTarget.multiply(interim);

        Matrix3x3<Source, Target> sourceToTarget = interimToTarget.multiply(sourceToInterim);

        var target2 = sourceToTarget.multiply(source);

        assertThat(target1.values()).contains(target2.values());
    }

    private record Source(double a, double b, double c) implements Vector3D {
        @Override
        public double[] values() {
            return new double[]{a, b, c};
        }
    }

    private record Interim(double a, double b, double c) implements Vector3D {
        Interim(double[] values) {
            this(values[0], values[1], values[2]);
        }

        @Override
        public double[] values() {
            return new double[]{a, b, c};
        }
    }

    private record Target(double a, double b, double c) implements Vector3D {
        Target(double[] values) {
            this(values[0], values[1], values[2]);
        }

        @Override
        public double[] values() {
            return new double[]{a, b, c};
        }
    }
}