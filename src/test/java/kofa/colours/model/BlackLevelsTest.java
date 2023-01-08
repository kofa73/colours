package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.EXACT;
import static kofa.Vector3Assert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

public class BlackLevelsTest {
    @Test
    void blackFromCIEXYZ() {
        var ciexyY = CIExyY.from(CIEXYZ.BLACK);
        var cieLAB = CIELAB.from(CIEXYZ.BLACK).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(CIEXYZ.BLACK).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(CIEXYZ.BLACK).usingOriginalMatrix();
        var sRgb = Srgb.from(CIEXYZ.BLACK);
        var rec2020 = Rec2020.from(CIEXYZ.BLACK);
        assertAllBlack(
                CIEXYZ.BLACK,
                ciexyY,
                cieLAB,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void blackFromCIExyY() {
        var cieXYZ = CIExyY.D65_BLACK_2DEGREE_STANDARD_OBSERVER.toXyz();
        var cieLAB = CIELAB.from(CIEXYZ.BLACK).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(CIEXYZ.BLACK).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(CIEXYZ.BLACK).usingOriginalMatrix();
        var sRgb = Srgb.from(CIEXYZ.BLACK);
        var rec2020 = Rec2020.from(CIEXYZ.BLACK);
        assertAllBlack(
                cieXYZ,
                CIExyY.D65_BLACK_2DEGREE_STANDARD_OBSERVER,
                cieLAB,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void blackFromCIELAB() {
        var cieXYZ = CIELAB.BLACK.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Srgb.from(cieXYZ);
        var rec2020 = Rec2020.from(cieXYZ);
        assertAllBlack(
                cieXYZ,
                ciexyY,
                CIELAB.BLACK,
                cieLUV,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void blackFromCIELUV() {
        var cieXYZ = CIELUV.BLACK.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLAB = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Srgb.from(cieXYZ);
        var rec2020 = Rec2020.from(cieXYZ);
        assertAllBlack(
                cieXYZ,
                ciexyY,
                cieLAB,
                CIELUV.BLACK,
                okLAB,
                sRgb,
                rec2020
        );
    }

    @Test
    void blackFromOkLAB() {
        var cieXYZ = OkLAB.BLACK.toXyz().usingOriginalMatrix();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var sRgb = Srgb.from(OkLAB.BLACK.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER());
        var rec2020 = Rec2020.from(cieXYZ);
        assertAllBlack(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                OkLAB.BLACK,
                sRgb,
                rec2020
        );
    }

    @Test
    void blackFromSrgb() {
        var cieXYZ = Srgb.BLACK.toXyz();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLab = OkLAB.from(Srgb.BLACK.toXyz()).usingD65_2DEGREE_STANDARD_OBSERVER();
        var rec2020 = Srgb.BLACK.toRec2020();
        assertAllBlack(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                okLab,
                Srgb.BLACK,
                rec2020
        );
    }


    @Test
    void blackFromRec2020() {
        var cieXYZ = Rec2020.BLACK.toXyz();
        var ciexyY = CIExyY.from(cieXYZ);
        var cieLAB = CIELAB.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var cieLUV = CIELUV.from(cieXYZ).usingD65_2DEGREE_STANDARD_OBSERVER();
        var okLab = OkLAB.from(cieXYZ).usingOriginalMatrix();
        var sRgb = Rec2020.BLACK.toSRGB();
        assertAllBlack(
                cieXYZ,
                ciexyY,
                cieLAB,
                cieLUV,
                okLab,
                sRgb,
                Rec2020.BLACK
        );
    }

    private void assertAllBlack(CIEXYZ cieXYZ, CIExyY ciexyY, CIELAB cieLAB, CIELUV cieLUV, OkLAB okLAB, Srgb sRgb, Rec2020 rec2020) {
        assertThat(cieXYZ.isBlack()).isTrue();
        assertThat(cieXYZ).isCloseTo(CIEXYZ.BLACK, EXACT);

        assertThat(ciexyY.isBlack()).isTrue();
        assertThat(ciexyY).isCloseTo(CIExyY.D65_BLACK_2DEGREE_STANDARD_OBSERVER, EXACT);

        assertThat(cieLAB.isBlack()).isTrue();
        assertThat(cieLAB).isCloseTo(CIELAB.BLACK, EXACT);

        var lchAb = cieLAB.toLch();
        assertThat(lchAb.isBlack()).isTrue();
        assertThat(lchAb).isCloseTo(CIELCh_ab.BLACK, EXACT);

        assertThat(cieLUV.isBlack()).isTrue();
        assertThat(cieLUV).isCloseTo(CIELUV.BLACK, EXACT);

        var lchUv = cieLUV.toLch();
        assertThat(lchUv.isBlack()).isTrue();
        assertThat(lchUv).isCloseTo(CIELCh_uv.BLACK, EXACT);

        assertThat(okLAB.isBlack()).isTrue();
        assertThat(okLAB).isCloseTo(OkLAB.BLACK.BLACK, EXACT);

        assertThat(sRgb.isBlack()).isTrue();
        assertThat(sRgb).isCloseTo(Srgb.BLACK, EXACT);

        assertThat(rec2020.isBlack()).isTrue();
        assertThat(rec2020).isCloseTo(Rec2020.BLACK, EXACT);
    }
}
