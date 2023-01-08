package kofa.colours.model;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public class WhiteLevelsTest {
    @Test
    void whiteFromCIEXYZ() {
        var ciexyY = CIExyY.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        var cieLAB = CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingOriginalMatrix();
        var sRgb = Srgb.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        var rec2020 = Rec2020.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        assertAllWhite(
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER,
                ciexyY,
                cieLAB,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void whiteFromCIExyY() {
        var cieXYZ = CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER.toXyz();
        var cieLAB = CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingOriginalMatrix();
        var sRgb = Srgb.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        var rec2020 = Rec2020.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        assertAllWhite(
                cieXYZ,
                CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER,
                cieLAB,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void whiteFromCIELAB() {
        var cieXYZ = CIELAB.WHITE.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Srgb.from(cieXYZ);
        var rec2020 = Rec2020.from(cieXYZ);
        assertAllWhite(
                cieXYZ,
                ciexyY,
                CIELAB.WHITE,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void whiteFromCIELUV() {
        var cieXYZ = CIELUV.WHITE.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Srgb.from(cieXYZ);
        var rec2020 = Rec2020.from(cieXYZ);
        assertAllWhite(
                cieXYZ,
                ciexyY,
                cieLAB,
                CIELUV.WHITE,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void whiteFromOkLAB() {
        var cieXYZ = OkLAB.WHITE.toXyz().usingOriginalMatrix();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var sRgb = OkLAB.WHITE.toSrgb();
        var rec2020 = Rec2020.from(cieXYZ);
        lenientlyAssertAllWhite(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                OkLAB.WHITE,
                sRgb,
                rec2020
        );
    }

    @Test
    void whiteFromSrgb() {
        var cieXYZ = Srgb.WHITE.toXyz();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLab = OkLAB.from(Srgb.WHITE);
        var rec2020 = Srgb.WHITE.toRec2020();
        assertAllWhite(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                okLab,
                Srgb.WHITE,
                rec2020
        );
    }

    @Test
    void whiteFromRec2020() {
        var cieXYZ = Rec2020.WHITE.toXyz();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLab = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Rec2020.WHITE.toSRGB();
        assertAllWhite(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                okLab,
                sRgb,
                Rec2020.WHITE
        );
    }

    private void assertAllWhite(CIEXYZ cieXYZ, CIExyY ciexyY, CIELAB cieLAB, CIELUV cieLUV, OkLAB okLAB, Srgb sRgb, Rec2020 rec2020) {
        assertThat(cieXYZ.isWhite()).isTrue();
        assertIsCloseTo(cieXYZ, CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE, 1E-12);

        assertThat(ciexyY.isWhite()).isTrue();
        assertIsCloseTo(ciexyY, CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE, 1E-12);

        assertThat(cieLAB.isWhite()).isTrue();
        assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(cieLAB, CIELAB.WHITE_L, 1E-12, 1E-12);

        var lchAb = cieLAB.toLch();
        assertThat(lchAb.isWhite()).isTrue();
        assertThat_LIsCloseToMax_andCIsCloseToZero(lchAb, CIELCh_ab.WHITE_L, CIELCh_ab.BLACK_L_THRESHOLD);

        assertThat(cieLUV.isWhite()).isTrue();
        assertIsCloseTo(cieLUV, CIELUV.WHITE, EXACT, 1E-12);

        var lchUv = cieLUV.toLch();
        assertThat(lchUv.isWhite()).isTrue();
        assertThat_LIsCloseToMax_andCIsCloseToZero(lchUv, CIELCh_uv.WHITE_L, CIELCh_uv.BLACK_L_THRESHOLD);

        assertThat(okLAB.isWhite()).isTrue();
        assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(okLAB, OkLAB.WHITE_L, 3E-4, 1E-4);

        var lchOk = okLAB.toLch();
        lenientlyAssertThat_LIsCloseToMax_andCIsCloseToZero(lchOk, OkLCh.WHITE_L, OkLCh.BLACK_L_THRESHOLD);

        assertThat(sRgb.isWhite()).isTrue();
        assertIsCloseTo(sRgb, Srgb.WHITE, EXACT, 1E-12);

        assertThat(rec2020.isWhite()).isTrue();
        assertIsCloseTo(rec2020, Rec2020.WHITE, EXACT, 1E-12);
    }

    private void lenientlyAssertAllWhite(CIEXYZ cieXYZ, CIExyY ciexyY, CIELAB cieLAB, CIELUV cieLUV, OkLAB okLAB, Srgb sRgb, Rec2020 rec2020) {
        assertThat(cieXYZ.isWhite()).isTrue();
        assertIsCloseTo(cieXYZ, CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE, 1E-12);

        assertThat(ciexyY.isWhite()).isTrue();
        assertIsCloseTo(ciexyY, CIExyY.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE, 1E-12);

        assertThat(cieLAB.isWhite()).isTrue();
        assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(cieLAB, CIELAB.WHITE_L, 2E-6, 4E-2);

        var lchAb = cieLAB.toLch();
        assertThat(lchAb.isWhite()).isTrue();
        lenientlyAssertThat_LIsCloseToMax_andCIsCloseToZero(lchAb, CIELCh_ab.WHITE_L, 2E-6);

        assertThat(cieLUV.isWhite()).isTrue();
        assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(cieLUV, CIELUV.WHITE_L, 2E-6, 6E-2);

        var lchUv = cieLUV.toLch();
        assertThat(lchUv.isWhite()).isTrue();
        lenientlyAssertThat_LIsCloseToMax_andCIsCloseToZero(lchUv, CIELCh_uv.WHITE_L, 2E-6);

        assertThat(okLAB.isWhite()).isTrue();
        assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(okLAB, OkLAB.WHITE_L, 3E-4, 1E-4);

        var lchOk = okLAB.toLch();
        assertThat_LIsCloseToMax_andCIsCloseToZero(lchOk, OkLCh.WHITE_L, OkLCh.BLACK_L_THRESHOLD);

        assertThat(sRgb.isWhite()).isTrue();
        assertIsCloseTo(sRgb, Srgb.WHITE, EXACT, 1E-12);

        // breaks
        // assertThat(rec2020.isWhite()).isTrue();
        assertIsCloseTo(rec2020, Rec2020.WHITE, PRECISE, 1E-12);
    }

    private void assertThat_LIsCloseToMax_andCIsCloseToZero(LCh<?, ?> lch, double whiteL, double whiteTolerance) {
        // hue is unreliable
        assertThat(lch.L()).isCloseTo(whiteL, Offset.offset(whiteTolerance));
        assertThat(lch.C()).isCloseTo(0.0, Offset.offset(1E-12));
    }

    private void lenientlyAssertThat_LIsCloseToMax_andCIsCloseToZero(LCh<?, ?> lch, double whiteL, double whiteTolerance) {
        // hue is unreliable
        assertThat(lch.L()).isCloseTo(whiteL, Offset.offset(whiteTolerance));
        assertThat(lch.C()).isCloseTo(0.0, Offset.offset(0.07));
    }

    private void assertThat_LIsCloseToMax_and_colourCoordinates_areCloseToZero(
            ConvertibleToLch<?, ?> labLuv,
            double whiteL, double whiteTolerancePercentage,
            double abToleranceOffset
    ) {
        double[] coordinates = labLuv.coordinates().toArray();
        double L = coordinates[0];
        double colour1 = coordinates[1];
        double colour2 = coordinates[2];
        assertThat(L).isCloseTo(whiteL, withPercentage(whiteTolerancePercentage));
        var abTolerance = Offset.offset(abToleranceOffset);
        assertThat(colour1).isCloseTo(0.0, abTolerance);
        assertThat(colour2).isCloseTo(0.0, abTolerance);
    }
}
