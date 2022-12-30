package kofa.maths;

import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;

import static org.assertj.core.api.Assertions.assertThat;

class SpaceConversionMatrixTest {
    private final SpaceConversionMatrix<Source, Interim> sourceToInterim = new SpaceConversionMatrix<>(
            Interim::new,
            new double[][]{
                    {2, 3, 5},
                    {7, 11, 13},
                    {17, 19, 23}
            }
    );

    @Test
    void multiply_vector() {
        // given
        // when
        Interim result = sourceToInterim.multiply(new Source(29, 31, 37));

        // then
        assertThat(result.coordinates()).containsExactly(336.0, 1025.0, 1933.0);
    }

    @Test
    void multiply_matrix() {
        // given
        var interimToTarget = new SpaceConversionMatrix<Interim, Target>(
                Target::new,
                new double[][]{
                        {29, 31, 37},
                        {41, 43, 47},
                        {53, 59, 61}
                }
        );

        // when
        SpaceConversionMatrix<Source, Target> result = interimToTarget.multiply(sourceToInterim);

        // then
        assertThat(result.values()[0]).containsExactly(904, 1131, 1399);
        assertThat(result.values()[1]).containsExactly(1182, 1489, 1845);
        assertThat(result.values()[2]).containsExactly(1556, 1967, 2435);
    }

    @Test
    void associativity() {
        var source = new Source(67, 71, 73);

        var interim = sourceToInterim.multiply(source);

        var interimToTarget = new SpaceConversionMatrix<Interim, Target>(
                Target::new,
                new double[][]{
                        {29, 31, 37},
                        {41, 43, 47},
                        {53, 59, 61}
                }
        );

        var target1 = interimToTarget.multiply(interim);

        SpaceConversionMatrix<Source, Target> sourceToTarget = interimToTarget.multiply(sourceToInterim);

        var target2 = sourceToTarget.multiply(source);

        assertThat(target1.coordinates()).containsExactlyElementsOf(target2.coordinates().boxed().toList());
    }

    private static class Source extends Vector3 {
        final double coordinate1;
        final double coordinate2;
        final double coordinate3;

        Source(double coordinate1, double coordinate2, double coordinate3) {
            super(coordinate1, coordinate2, coordinate3);
            this.coordinate1 = coordinate1;
            this.coordinate2 = coordinate2;
            this.coordinate3 = coordinate3;
        }

        @Override
        public DoubleStream coordinates() {
            return DoubleStream.of(coordinate1, coordinate2, coordinate3);
        }

        @Override
        public String toString() {
            return Vector3.format(this, coordinate1, coordinate2, coordinate3);
        }
    }

    private static class Interim extends Vector3 {
        final double coordinate1;
        final double coordinate2;
        final double coordinate3;

        Interim(double coordinate1, double coordinate2, double coordinate3) {
            super(coordinate1, coordinate2, coordinate3);
            this.coordinate1 = coordinate1;
            this.coordinate2 = coordinate2;
            this.coordinate3 = coordinate3;
        }

        @Override
        public DoubleStream coordinates() {
            return DoubleStream.of(coordinate1, coordinate2, coordinate3);
        }

        @Override
        public String toString() {
            return Vector3.format(this, coordinate1, coordinate2, coordinate3);
        }
    }

    private static class Target extends Vector3 {
        final double coordinate1;
        final double coordinate2;
        final double coordinate3;

        Target(double coordinate1, double coordinate2, double coordinate3) {
            super(coordinate1, coordinate2, coordinate3);
            this.coordinate1 = coordinate1;
            this.coordinate2 = coordinate2;
            this.coordinate3 = coordinate3;
        }

        @Override
        public DoubleStream coordinates() {
            return DoubleStream.of(coordinate1, coordinate2, coordinate3);
        }

        @Override
        public String toString() {
            return Vector3.format(this, coordinate1, coordinate2, coordinate3);
        }
    }
}