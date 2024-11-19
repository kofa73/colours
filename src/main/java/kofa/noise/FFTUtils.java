package kofa.noise;

import org.jtransforms.fft.DoubleFFT_2D;

import static java.lang.Math.sqrt;

public class FFTUtils {
    public static double[] realToComplex(double[] real) {
        var complex = new double[real.length * 2];
        for (int i = 0; i < real.length; i ++) {
            complex[2 * i] = real[i];
        }
        return complex;
    }

//    public static double[] fft2d(double[] real, DoubleFFT_2D fft) {
//        double[] buffer = realToComplex(real);
//        fft.complexForward(buffer);
//        return buffer;
//    }


    public static double magnitude(double re, double im) {
        return sqrt(power(re, im));
    }

    public static double power(double re, double im) {
        return re * re + im * im;
    }

}
