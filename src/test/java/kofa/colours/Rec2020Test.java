package kofa.colours;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class Rec2020Test {
    @Test
    void values() {
        assertThat(REC2020_663399.values()).contains(0.10805750115024938, 0.04324141440243367, 0.29037800273628517);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Rec2020.from(XYZ_663399),
                REC2020_663399,
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                REC2020_663399.toXYZ(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toSRGB() {
        assertIsCloseTo(
                REC2020_663399.toSRGB(),
                LINEAR_SRGB_663399,
                PRECISE
        );
    }

    @Test
    void x() {
        var rec = new Rec2020(1, 0, 0);
        var srgb = rec.toSRGB();

        System.out.println("rec: " + rec);
        System.out.println("Luv:  " + Luv.from(rec.toXYZ()).usingD65());
        System.out.println("Lab:  " + Lab.from(rec.toXYZ()).usingD65());

        System.out.println("sRGB: " + srgb);
        System.out.println("Luv:  " + Luv.from(srgb.toXYZ()).usingD65());
        System.out.println("Lab:  " + Lab.from(srgb.toXYZ()).usingD65());

        double[][] maxC_forLh_ab = new double[100][];
        double[][] maxC_forLh_uv = new double[100][];

        double step = 0.01;
        double maxL = -1;
        RGB<?> rgb_maxL = null;
        double maxC_ab = -1;
        RGB<?> rgb_maxC_ab = null;
        double maxC_uv = -1;
        RGB<?> rgb_maxC_uv = null;
        for (double r = 0; r < 1 + step; r += step) {
            for (double g = 0; g < 1 + step; g += step) {
                for (double b = 0; b < 1 + step; b += step) {
                    Rec2020 current_rgb = new Rec2020(r, g, b);
                    var xyz = current_rgb.toXYZ();
                    var lch_ab = Lab.from(xyz).usingD65().toLCh();
                    if (maxL < lch_ab.L()) {
                        maxL = lch_ab.L();
                        rgb_maxL = current_rgb;
                    }
                    if (maxC_ab < lch_ab.C()) {
                        maxC_ab = lch_ab.C();
                        rgb_maxC_ab = current_rgb;
                    }
                    var lch_uv = Luv.from(xyz).usingD65().toLch();
                    if (maxC_uv < lch_uv.C()) {
                        maxC_uv = lch_uv.C();
                        rgb_maxC_uv = current_rgb;
                    }
                }
            }
        }
        System.out.println("maxL = " + maxL + ", rgb = " + rgb_maxL + ", srgb=" + SRGB.from(rgb_maxL.toXYZ()));
        System.out.println("maxC_ab = " + maxC_ab + ", rgb = " + rgb_maxC_ab + ", srgb=" + SRGB.from(rgb_maxC_ab.toXYZ()));
        System.out.println("maxC_uv = " + maxC_uv + ", rgb = " + rgb_maxC_uv + ", srgb=" + SRGB.from(rgb_maxC_uv.toXYZ()));

        // sweep the faces of the RGB cube
        printValues(this::just0And1, this::sweepFrom0To1, this::sweepFrom0To1);
        printValues(this::sweepFrom0To1, this::just0And1, this::sweepFrom0To1);
        printValues(this::sweepFrom0To1, this::sweepFrom0To1, this::just0And1);
    }

    private void printValues(Supplier<DoubleStream> redRange, Supplier<DoubleStream> greenRange, Supplier<DoubleStream> blueRange) {
        redRange.get().forEach(red ->
                greenRange.get().forEach(green ->
                        blueRange.get().forEach(blue -> {
                                    System.out.println("%f\t%f\t%f".formatted(red, green, blue));
                                    var rgb = new SRGB(red, green, blue);
                                    var xyz = rgb.toXYZ();
                                    var lCh_ab = Lab.from(xyz).usingD65().toLCh();
                                    var lCh_uv = Luv.from(xyz).usingD65().toLch();
                                    System.out.println("LCh_ab: %s, LCh_uv: %s".formatted(lCh_ab, lCh_uv));
                                }
                        )
                )
        );
    }

    private static int steps = 3;
    private static double divisor = steps;

    private DoubleStream sweepFrom0To1() {
        return IntStream.rangeClosed(0, steps).mapToDouble(i -> i / divisor);
    }

    private DoubleStream just0And1() {
        return DoubleStream.of(0, 1);
    }
}
