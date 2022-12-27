package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static kofa.colours.model.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class XyzTest {
    @Test
    void toDoubles() {
        assertThat(
                XYZ_663399.coordinates()
        ).containsExactly(0.12412, 0.07493, 0.3093);
    }

    @Test
    void toLuvUsingWhitePoint() {
        // from https://ajalt.github.io/colormath/converter/
        assertIsCloseTo(
                CieLuv.from(XYZ_663399).usingD65(),
                new CieLuv(32.90281, 12.9804, -67.75974),
                PRECISE
        );
        double expected_hRadians = toRadians(6.1006);
        assertIsCloseTo(
                // 154/255 = 0.60392; 58/255 = 0.22745, 61/255 = 0.23922
                CieLuv
                        .from(
                                new Rec2020(154.0 / 255, 58.0 / 255, 61.0 / 255).toXyz()
                        ).usingWhitePoint(ConversionHelper.D65_WHITE_XYZ)
                        .toLch(),
                new CieLchUv(63.91936, 83.81409, expected_hRadians),
                // picked from UI
                PRECISE, PRECISE, LENIENT
        );
        assertIsCloseTo(
                // linear Rec709 reading from a photo in darktable
                CieLuv.from(
                                new Srgb(217.0 / 255, 46.0 / 255, 59.0 / 255).toXyz()
                        ).usingWhitePoint(ConversionHelper.D65_WHITE_XYZ)
                        .toLch(),
                // LCh from https://ajalt.github.io/colormath/converter/, h in degrees
                new CieLchUv(63.91936, 83.81409, toRadians(6.1006)),
                // allow some slack because of the integers
                PRECISE, LENIENT, ROUGH
        );
    }
}
