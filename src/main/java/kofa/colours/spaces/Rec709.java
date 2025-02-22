package kofa.colours.spaces;

public class Rec709 {
    /*
    public static void main(String[] args) {
        var m = calculateToXyzMatrix(
                0.6400, 0.3300,
                0.3000, 0.6000,
                0.1500, 0.0600,
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER
        );
        fromXyzMatrix using MatrixUtils.inverse
    }
     */

    public static void rec709_to_XYZ(double[] rgb, double[] xyz) {
        xyz[0] = 0.41238656325299233  * rgb[0] + 0.35759149092062525 * rgb[1] + 0.1804504912035636  * rgb[2];
        xyz[1] = 0.21263682167732417  * rgb[0] + 0.7151829818412505  * rgb[1] + 0.07218019648142544 * rgb[2];
        xyz[2] = 0.019330620152483994 * rgb[0] + 0.1191971636402084  * rgb[1] + 0.950372587005435   * rgb[2];
    }

    public static void XYZ_to_rec709(double[] xyz, double[] rgb) {
        rgb[0] =  3.241003232976353   * xyz[0] - 1.5373989694887833  * xyz[1] - 0.4986158819963621  * xyz[2];
        rgb[1] = -0.9692242522025167  * xyz[0] + 1.8759299836951773  * xyz[1] + 0.04155422634008489 * xyz[2];
        rgb[2] =  0.05563941985197549 * xyz[0] - 0.20401120612391002 * xyz[1] + 1.0571489771875335  * xyz[2];
    }

    public static void rec709toRec2020(double[] rec709, double[] rec2020) {
        rec2020[0] = 0.6274019247222371   * rec709[0] + 0.3292919717550017  * rec709[1] + 0.0433061035227624   * rec709[2];
        rec2020[1] = 0.06909548973926104  * rec709[0] + 0.9195442812673945  * rec709[1] + 0.011360228993344304 * rec709[2];
        rec2020[2] = 0.016393709088163195 * rec709[0] + 0.08802816239790055 * rec709[1] + 0.8955781285139356   * rec709[2];
    }
}
