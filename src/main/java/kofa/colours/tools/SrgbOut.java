package kofa.colours.tools;

import kofa.io.RgbImage;

public class SrgbOut {
    public static final RgbImage.PixelTransformer SRGB_OUT = new RgbImage.PixelTransformer() {
        private static final double LINEAR_THRESHOLD = 0.0031308;

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            return new double[] {
                    applyGamma(red), applyGamma(green), applyGamma(blue)
            };
        }

        private static double applyGamma(double linear) {
            linear = Math.clamp(linear, 0, 1);
            return linear <= LINEAR_THRESHOLD ?
                    12.92 * linear :
                    1.055 * Math.pow(linear, 1 / 2.4) - 0.055;
        }
    };
}
