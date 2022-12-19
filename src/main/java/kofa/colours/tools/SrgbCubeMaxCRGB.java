package kofa.colours.tools;

import kofa.colours.Lab;
import kofa.colours.Luv;
import kofa.colours.SRGB;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Math.*;

/**
 * Tries to find max C values for LCh(ab) and LCh(uv) by scanning the surfaces of the sRGB cube.
 * Does not work too well, the generated values result in slightly out of gamut RGB.
 */
class SrgbCubeMaxCRGB {

    private static final int L_RESOLUTION = 1000;
    private static final int H_RESOLUTION = 360;
    private static final int RGB_STEPS = 10000;

    public static void main(String[] ignored) {
        double[][] maxC_forLh_ab_byLByH = new double[L_RESOLUTION + 1][];
        double[][] maxC_forLh_uv_byLByH = new double[L_RESOLUTION + 1][];
        initByHue(maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        // sweep the faces of the RGB cube
        sweepFace(SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To1, SrgbCubeMaxCRGB::sweepFrom0To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To1, SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To1, SrgbCubeMaxCRGB::sweepFrom0To1, SrgbCubeMaxCRGB::just0And1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        sweepFace(SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To01, SrgbCubeMaxCRGB::sweepFrom0To01, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To01, SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To01, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To01, SrgbCubeMaxCRGB::sweepFrom0To01, SrgbCubeMaxCRGB::just0And1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        sweepFace(SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To001, SrgbCubeMaxCRGB::sweepFrom0To001, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To001, SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom0To001, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom0To001, SrgbCubeMaxCRGB::sweepFrom0To001, SrgbCubeMaxCRGB::just0And1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        sweepFace(SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom09To1, SrgbCubeMaxCRGB::sweepFrom09To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom09To1, SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom09To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom09To1, SrgbCubeMaxCRGB::sweepFrom09To1, SrgbCubeMaxCRGB::just0And1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        sweepFace(SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom099To1, SrgbCubeMaxCRGB::sweepFrom099To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom099To1, SrgbCubeMaxCRGB::just0And1, SrgbCubeMaxCRGB::sweepFrom099To1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);
        sweepFace(SrgbCubeMaxCRGB::sweepFrom099To1, SrgbCubeMaxCRGB::sweepFrom099To1, SrgbCubeMaxCRGB::just0And1, maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        printMaxC(maxC_forLh_ab_byLByH, "Lab");
        printMaxC(maxC_forLh_uv_byLByH, "Luv");
    }

    private static void printMaxC(double[][] maxC, String space) {
        System.out.println("======== " + space + " ========");
        for (int L = 0; L < maxC.length; L++) {
            for (int h = 0; h < maxC[L].length; h++) {
                System.out.printf("%d,%d,%f%n", L, h, maxC[L][h]);
            }
        }
    }

    private static void initByHue(double[][] maxC_forLh_ab_byLByH, double[][] maxC_forLh_uv_byLByH) {
        for (int L = 0; L < maxC_forLh_ab_byLByH.length; L++) {
            maxC_forLh_ab_byLByH[L] = new double[H_RESOLUTION + 1];
            maxC_forLh_uv_byLByH[L] = new double[H_RESOLUTION + 1];
        }
    }

    private static void sweepFace(
            Supplier<DoubleStream> redRange,
            Supplier<DoubleStream> greenRange,
            Supplier<DoubleStream> blueRange,
            double[][] maxC_forLh_ab_byLByH,
            double[][] maxC_forLh_uv_byLByH
    ) {
        redRange.get().parallel().forEach(red ->
                greenRange.get().parallel().forEach(green ->
                        blueRange.get().forEach(blue -> {
                                    var rgb = new SRGB(red, green, blue);
                                    var xyz = rgb.toXYZ();
                                    var lCh_ab = Lab.from(xyz).usingD65().toLCh();
                            var lCh_uv = Luv.from(xyz).usingD65().toLCh();
                            int L = (int) Math.round(lCh_ab.L() / 100.0 * L_RESOLUTION);
                                    int h_ab = (int) round(lCh_ab.h() / (2 * PI) * H_RESOLUTION);
                                    int h_uv = (int) round(lCh_uv.h() / (2 * PI) * H_RESOLUTION);
                                    h_ab = min(h_ab, H_RESOLUTION);
                                    h_uv = min(h_uv, H_RESOLUTION);
                                    maxC_forLh_ab_byLByH[L][h_ab] = Math.max(maxC_forLh_ab_byLByH[L][h_ab], lCh_ab.C());
                                    maxC_forLh_uv_byLByH[L][h_uv] = Math.max(maxC_forLh_uv_byLByH[L][h_uv], lCh_uv.C());
                                }
                        )
                )
        );
    }

    private static DoubleStream sweepFrom0To1() {
        return IntStream.rangeClosed(0, RGB_STEPS).mapToDouble(i -> i / (double) RGB_STEPS);
    }

    private static DoubleStream sweepFrom0To01() {
        return IntStream.rangeClosed(0, RGB_STEPS).mapToDouble(i -> i / (double) (10 * RGB_STEPS));
    }

    private static DoubleStream sweepFrom0To001() {
        return IntStream.rangeClosed(0, RGB_STEPS).mapToDouble(i -> i / (double) (100 * RGB_STEPS));
    }

    private static DoubleStream sweepFrom09To1() {
        return IntStream.rangeClosed(0, RGB_STEPS).mapToDouble(i -> 0.9 + i / (double) (10 * RGB_STEPS));
    }

    private static DoubleStream sweepFrom099To1() {
        return IntStream.rangeClosed(0, RGB_STEPS).mapToDouble(i -> 0.99 + i / (double) (100 * RGB_STEPS));
    }

    private static DoubleStream just0And1() {
        return DoubleStream.of(0, 1);
    }
}
