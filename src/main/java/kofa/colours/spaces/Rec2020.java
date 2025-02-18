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

    public static void rec2020_to_XYZ(float[] rgb, float[] xyz) {
        xyz[0] = 0.6369535067850743f * rgb[0] + 0.14461918466923313f * rgb[1] + 0.16885585392287336f * rgb[2];
        xyz[1] = 0.2626983389565561f * rgb[0] + 0.6780087657728164f * rgb[1] + 0.05929289527062728f * rgb[2];
        xyz[2] = 4.994070966444389E-17f * rgb[0] + 0.028073135847556947f * rgb[1] + 1.0608272349505707f * rgb[2];
    }

    public static void XYZ_to_rec2020(float[] xyz, float[] rgb) {
        rgb[0] =  1.7166634277958794f  * xyz[0] - 0.35567331973013916f * xyz[1] - 0.25336808789024756f  * xyz[2];
        rgb[1] = -0.6666738361988865f  * xyz[0] + 1.6164557398246975f  * xyz[1] + 0.015768297096133727f * xyz[2];
        rgb[2] =  0.01764248178497722f * xyz[0] - 0.04277697638275316f * xyz[1] + 0.9422432810184306f   * xyz[2];
    }
}
