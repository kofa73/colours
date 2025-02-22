package kofa.colours.tools;

import static java.lang.Math.*;
import static kofa.colours.tools.MathHelpers.smoothstep;
import static kofa.colours.tools.MathHelpers.step;

// Uchimura 2017, "HDR theory and practice"
// https://github.com/dmnsgn/glsl-tone-map/blob/main/uchimura.glsl
// https://www.desmos.com/calculator/gslcdxvipg
public class UchimuraToneMapper {
    private static final double P = 1.0;  // max display brightness
    private static final double a = 1.0;  // contrast
    private static final double m = 0.22; // linear section start
    private static final double l = 0.4;  // linear section length
    private static final double c = 1.33; // black
    private static final double b = 0.0;  // pedestal

    public static double toneMap(double value) {
        return value > 0 ? uchimura(value, P, a, m, l, c, b) : 0;
    }

    private static double uchimura(double x, double P, double a, double m, double l, double c, double b) {
        double l0 = ((P - m) * l) / a;
        double S0 = m + l0;
        double S1 = m + a * l0;
        double C2 = (a * P) / (P - S1);
        double CP = -C2 / P;

        double w0 = 1.0 - smoothstep(0.0, m, x);
        double w2 = step(m + l0, x);
        double w1 = 1.0 - w0 - w2;

        double T = m * pow(x / m, c) + b;
        double S = P - (P - S1) * exp(CP * (x - S0));
        double L = m + a * (x - m);

        return T * w0 + L * w1 + S * w2;
    }
}
