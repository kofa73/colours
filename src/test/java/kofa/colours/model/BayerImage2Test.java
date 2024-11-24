package kofa.colours.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static org.assertj.core.api.Assertions.assertThat;

class BayerImage2Test {

    private static final int PADDING = 16;
    private static final int SIZE = 4;
    private static final int WIDTH = SIZE;
    private static final int HEIGHT = SIZE;
    private static final int PADDED_WIDTH_IN_PIXELS = PADDING + SIZE + PADDING;
    private static final int PADDED_WIDTH_IN_COMPONENTS = PADDED_WIDTH_IN_PIXELS * 3;
    private static final short[] PIXEL_VALUES = {
            1,  2,  3,  4,
            5,  6,  7,  8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    private WritableRaster raster;

    @BeforeEach
    void setup() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_USHORT_GRAY);

        // Get raster and set pixels
        raster = image.getRaster();
        raster.setDataElements(0, 0, WIDTH, HEIGHT, PIXEL_VALUES);
    }


    @Test
    void prepareRGGB() {
        // given
        var bayerImage = new BayerImage2(raster, CFA.RGGB, 1, 1);

        // when
        float[] rgb = bayerImage.toRGB();

        // then
        float[] actualRgbPixels = extractPixels(rgb);
        float[] expectedRgbPixels = {
                //  R               G         R                G
                    1,  0,  0,   0,  2,  0,   3,  0,  0,   0,  4,  0,
                //      G                B        G                B
                    0,  5,  0,   0,  0,  6,   0,  7,  0,   0,  0,  8,

                //  R               G         R                G
                    9,  0,  0,   0, 10,  0,  11,  0,  0,   0, 12,  0,
                //      G                B        G                B
                    0, 13,  0,   0,  0, 14,   0, 15,  0,   0,  0, 16
        };
        assertThat(actualRgbPixels).isEqualTo(expectedRgbPixels);

        assertBorders(rgb);
    }

    @Test
    void prepareGRBG() {
        // given
        var bayerImage = new BayerImage2(raster, CFA.GRBG, 1, 1);

        // when
        float[] rgb = bayerImage.toRGB();

        // then
        float[] actualRgbPixels = extractPixels(rgb);
        float[] expectedRgbPixels = {
                //      G        R                G        R
                    0,  1,  0,   2,  0,  0,   0,  3,  0,   4,  0,  0,
                //          B        G                B        G
                    0,  0,  5,   0,  6,  0,   0,  0,  7,   0,  8,  0,

                //      G        R                G        R
                    0,  9,  0,  10,  0,  0,   0, 11,  0,  12,  0,  0,
                //          B        G                B        G
                    0,  0, 13,   0, 14,  0,   0,  0, 15,   0, 16,  0
        };
        assertThat(actualRgbPixels).isEqualTo(expectedRgbPixels);
        assertBorders(rgb);
    }

    private void assertBorders(float[] rgb) {
        // some arbitrary pixels in the border must match the source image pixels

        // top-left -- the content row0 is repeated in padding above, the 2 left pixels are repeated in padding left
        // [2, 3] x = 2 is even, so content pixel[0, 0] is repeated
        assertThat(rgb[pixelOffset(2, 3)]).describedAs("[2,3].r").isEqualTo(rgb[pixelOffset(PADDING, PADDING)]);
        assertThat(rgb[pixelOffset(2, 3) + 1]).describedAs("[2,3].g").isEqualTo(rgb[pixelOffset(PADDING, PADDING) + 1]);
        assertThat(rgb[pixelOffset(2, 3) + 2]).describedAs("[2,3].b").isEqualTo(rgb[pixelOffset(PADDING, PADDING) + 2]);
        // [3, 3] x = 3 is odd, so content pixel[1, 0] is repeated
        assertThat(rgb[pixelOffset(3, 3)]).describedAs("[3,3].r").isEqualTo(rgb[pixelOffset(PADDING + 1, PADDING)]);
        assertThat(rgb[pixelOffset(3, 3) + 1]).describedAs("[3,3].g").isEqualTo(rgb[pixelOffset(PADDING + 1, PADDING) + 1]);
        assertThat(rgb[pixelOffset(3, 3) + 2]).describedAs("[3,3].b").isEqualTo(rgb[pixelOffset(PADDING + 1, PADDING) + 2]);

        // top-right -- the 2 right pixels are repeated in padding right
        // [32, 3] x = 32 is even, so content pixel[2, 0] is repeated
        assertThat(rgb[pixelOffset(32, 3)]).describedAs("[32,3].r").isEqualTo(rgb[contentPixelOffset(2, 0)]);
        assertThat(rgb[pixelOffset(32, 3) + 1]).describedAs("[32,3].g").isEqualTo(rgb[contentPixelOffset(2, 0) + 1]);
        assertThat(rgb[pixelOffset(32, 3) + 2]).describedAs("[32,3].b").isEqualTo(rgb[contentPixelOffset(2, 0) + 2]);
        // [33, 3] x = 33 is odd, so content pixel[3, 0] is repeated
        assertThat(rgb[pixelOffset(33, 3)]).describedAs("[33,3].r").isEqualTo(rgb[contentPixelOffset(3, 0)]);
        assertThat(rgb[pixelOffset(33, 3) + 1]).describedAs("[33,3].g").isEqualTo(rgb[contentPixelOffset(3, 0) + 1]);
        assertThat(rgb[pixelOffset(33, 3) + 2]).describedAs("[33,3].b").isEqualTo(rgb[contentPixelOffset(3, 0) + 2]);

        // bottom-left -- the content row3 is repeated in padding below, the 2 left pixels are repeated in padding left
        // [2, 31] x = 2 is even, so content pixel[0, 3] is repeated
        assertThat(rgb[pixelOffset(2, 31)]).describedAs("[2,31].r").isEqualTo(rgb[contentPixelOffset(0, 3)]);
        assertThat(rgb[pixelOffset(2, 31) + 1]).describedAs("[2,31].g").isEqualTo(rgb[contentPixelOffset(0, 3) + 1]);
        assertThat(rgb[pixelOffset(2, 31) + 2]).describedAs("[2,31].b").isEqualTo(rgb[contentPixelOffset(0, 3) + 2]);
        // [3, 31] x = 3 is odd, so content pixel[1, 3] is repeated
        assertThat(rgb[pixelOffset(3, 31)]).describedAs("[3,31].r").isEqualTo(rgb[contentPixelOffset(1, 3)]);
        assertThat(rgb[pixelOffset(3, 31) + 1]).describedAs("[3,31].g").isEqualTo(rgb[contentPixelOffset(1, 3) + 1]);
        assertThat(rgb[pixelOffset(3, 31) + 2]).describedAs("[3,31].b").isEqualTo(rgb[contentPixelOffset(1, 3) + 2]);

        // bottom-right -- the 2 right pixels are repeated in padding right
        // [32, 31] x = 32 is even, so content pixel[2, 3] is repeated
        assertThat(rgb[pixelOffset(32, 31)]).describedAs("[32,31].r").isEqualTo(rgb[contentPixelOffset(2, 3)]);
        assertThat(rgb[pixelOffset(32, 31) + 1]).describedAs("[32,31].g").isEqualTo(rgb[contentPixelOffset(2, 3) + 1]);
        assertThat(rgb[pixelOffset(32, 31) + 2]).describedAs("[32,31].b").isEqualTo(rgb[contentPixelOffset(2, 3) + 2]);
        // x = 33 is odd, so content pixel[3, 3] is repeated
        assertThat(rgb[pixelOffset(33, 31)]).describedAs("[33,3].r").isEqualTo(rgb[contentPixelOffset(3, 3)]);
        assertThat(rgb[pixelOffset(33, 31) + 1]).describedAs("[33,3].g").isEqualTo(rgb[contentPixelOffset(3, 3) + 1]);
        assertThat(rgb[pixelOffset(33, 31) + 2]).describedAs("[33,3].b").isEqualTo(rgb[contentPixelOffset(3, 3) + 2]);
    }

    private static float[] extractPixels(float[] rgb) {
        int outIndex = 0;
        float[] actualRgbPixels = new float[SIZE * SIZE * 3];
        for (int y = PADDING; y < PADDING + SIZE; y++) {
            for (int x = PADDING; x < PADDING + SIZE; x++) {
                int index = pixelOffset(x, y);
                actualRgbPixels[outIndex] = rgb[index];
                actualRgbPixels[outIndex + 1] = rgb[index + 1];
                actualRgbPixels[outIndex + 2] = rgb[index + 2];
                outIndex += 3;
            }
        }
        return actualRgbPixels;
    }

    private static int pixelOffset(int x, int y) {
        return y * PADDED_WIDTH_IN_COMPONENTS + 3 * x;
    }

    private static int contentPixelOffset(int x, int y) {
        return pixelOffset(PADDING + x, PADDING + y);
    }
}
