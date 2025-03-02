package kofa.colours.spaces;

import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_x;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_y;

public class Rec2020 {
    public static void main(String[] args) {
        /*
        double[][] toXyzMatrix = calculateToXyzMatrix(
                0.708, 0.292,
                0.170, 0.797,
                0.131, 0.046,
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER
        );
        fromXyzMatrix using MatrixUtils.inverse
         */
    }
    public static final SpaceParameters PARAMS = new SpaceParameters(
            D65_WHITE_2DEG_x, D65_WHITE_2DEG_y,
            0.708, 0.292,
            0.170, 0.797,
            0.131, 0.046,
            Rec2020::XYZ_to_rec2020
    );

    public static void rec2020_to_XYZ(double[] rgb, double[] xyz) {
        xyz[0] = 0.6369535067850743 * rgb[0] + 0.14461918466923313 * rgb[1] + 0.16885585392287336 * rgb[2];
        xyz[1] = 0.2626983389565561 * rgb[0] + 0.6780087657728164 * rgb[1] + 0.05929289527062728 * rgb[2];
        xyz[2] = 4.994070966444389E-17 * rgb[0] + 0.028073135847556947 * rgb[1] + 1.0608272349505707 * rgb[2];
    }

    public static void XYZ_to_rec2020(double[] xyz, double[] rgb) {
        rgb[0] =  1.7166634277958794  * xyz[0] - 0.35567331973013916 * xyz[1] - 0.25336808789024756  * xyz[2];
        rgb[1] = -0.6666738361988865  * xyz[0] + 1.6164557398246975  * xyz[1] + 0.015768297096133727 * xyz[2];
        rgb[2] =  0.01764248178497722 * xyz[0] - 0.04277697638275316 * xyz[1] + 0.9422432810184306   * xyz[2];
    }

    public static void rec2020ToRec709(double[] rec2020, double[] rec709) {
        rec709[0] = 1.6604962191478272 * rec2020[0] - 0.5876564441311344 * rec2020[1] - 0.07283977501669414 * rec2020[2];
        rec709[1] = -0.12454709558601196 * rec2020[0] + 1.132895109247297 * rec2020[1] - 0.008348013661284223 * rec2020[2];
        rec709[2] = -0.01815368138707182 * rec2020[0] - 0.10059737168574254 * rec2020[1] + 1.1187510530728144 * rec2020[2];
    }
}
