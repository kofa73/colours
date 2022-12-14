package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericHelper.assertApproximatelyEqual;
import static org.assertj.core.api.Assertions.assertThat;

class LCh_uv_Test {
    private static final float H_RADIANS = (float) (280.84448f / 360 * 2 * Math.PI);
    private final LCh_uv lch = new LCh_uv(32.90281f, 68.99183f, H_RADIANS);

    @Test
    void toFloats() {
        assertThat(
                lch.toFloats()
        ).containsExactly(32.90281f, 68.99183f, H_RADIANS);
    }

    @Test
    void toLuv() {
        assertApproximatelyEqual(
                lch.toLuv(),
                new Luv(32.90281f, 12.9804f, -67.75974f),
                1E-5f
        );
    }
}