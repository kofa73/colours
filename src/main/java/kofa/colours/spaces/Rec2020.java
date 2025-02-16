package kofa.colours.spaces;

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

    public static void rec2020ToXyz(float[] rgb, float[] xyz) {
        xyz[0] = 0.6369535067850743f * rgb[0] + 0.14461918466923313f * rgb[1] + 0.16885585392287336f * rgb[2];
        xyz[1] = 0.2626983389565561f * rgb[0] + 0.6780087657728164f * rgb[1] + 0.05929289527062728f * rgb[2];
        xyz[2] = 4.994070966444389E-17f * rgb[0] + 0.028073135847556947f * rgb[1] + 1.0608272349505707f * rgb[2];
    }

    public static void xyzToRec2020(float[] xyz, float[] rgb) {
        rgb[0] = 1.7166634277958794f * xyz[0] - 0.35567331973013916f * xyz[1] - 0.25336808789024756f * xyz[2];
        rgb[1] = -0.6666738361988865f * xyz[0] + 1.6164557398246975f * xyz[1] + 0.015768297096133727f * xyz[2];
        rgb[2] = 0.01764248178497722f * xyz[0] - 0.04277697638275316f * xyz[1] + 0.9422432810184306f * xyz[2];
    }

    public static void rec2020ToRec709(float[] rec2020, float[] rec709) {
        rec709[0] = 1.6604962191478272f * rec2020[0] - 0.5876564441311344f * rec2020[1] - 0.07283977501669414f * rec2020[2];
        rec709[1] = -0.12454709558601196f * rec2020[0] + 1.132895109247297f * rec2020[1] - 0.008348013661284223f * rec2020[2];
        rec709[2] = -0.01815368138707182f * rec2020[0] - 0.10059737168574254f * rec2020[1] + 1.1187510530728144f * rec2020[2];
    }
}
