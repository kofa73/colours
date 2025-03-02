package kofa.colours.spaces;

import java.util.function.BiConsumer;

public record SpaceParameters(
        double whitePointX, double whitePointY,
        double redX, double redY,
        double greenX, double greenY,
        double blueX, double blueY,
        BiConsumer<double[], double[]> XYZ_to_rgb) {
}
