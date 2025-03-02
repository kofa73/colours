package kofa.colours.spaces;

import java.util.function.BiConsumer;

public record SpaceParameters(
        double whitePoint_x, double whitePoint_y,
        double red_x, double red_y,
        double green_x, double green_y,
        double blue_x, double blue_y,
        BiConsumer<double[], double[]> XYZ_to_rgb) {
}
