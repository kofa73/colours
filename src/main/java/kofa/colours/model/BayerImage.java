package kofa.colours.model;

import java.awt.image.Raster;

public class BayerImage implements Cloneable {
    public enum CFA {RGGB, GRBG}

    public final int width;
    public final int height;
    public final float[] pane0;
    public final float[] pane1;
    public final float[] pane2;
    public final float[] pane3;

    private final float rMultiplier;
    private final float bMultiplier;
    private final CFA cfa;

    private BayerImage(CFA cfa, int width, int height, float[] pane0, float[] pane1, float[] pane2, float[] pane3, float rMultiplier, float bMultiplier) {
        this.cfa = cfa;
        this.width = width;
        this.height = height;
        this.pane0 = pane0;
        this.pane1 = pane1;
        this.pane2 = pane2;
        this.pane3 = pane3;
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
    }

    public BayerImage(Raster raster, CFA cfa, float rMultiplier, float bMultiplier) {
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
        this.cfa = cfa;
        int rasterHeight = raster.getHeight();
        height = rasterHeight / 2;
        int rasterWidth = raster.getWidth();
        width = rasterWidth / 2;
        pane0 = new float[height * width];
        pane1 = new float[height * width];
        pane2 = new float[height * width];
        pane3 = new float[height * width];

        int[] pixelBuffer = new int[1];

        // TODO: pass in the CFA type, and apply the red/blue multipliers ASAP. Does not matter for bilinear,
        // but may disturb other demosaic algos that calculate gradients etc. (or not?)

        int index = 0;
        for (int y = 0; y < rasterHeight; y += 2) {
            for (int x = 0; x < rasterWidth; x += 2) {
                raster.getPixel(x, y, pixelBuffer);
                int pixel = pixelBuffer[0];
                pane0[index] = pixel;

                raster.getPixel(x + 1, y, pixelBuffer);
                pixel = pixelBuffer[0];
                pane1[index] = pixel;

                raster.getPixel(x, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                pane2[index] = pixel;

                raster.getPixel(x + 1, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                pane3[index] = pixel;

                index++;
            }
        }
    }

    public float[] simpleDemosaic() {
        return switch (cfa) {
            case RGGB -> simpleRGGBDemosaic();
            case GRBG -> simpleGRBGDemosaic();
        };
    }

    private float[] simpleGRBGDemosaic() {
        int fullWidth = width * 2;
        int fullHeight = height * 2;
        float[] demosaicked = new float[fullWidth * fullHeight * 3];

        int position = 0;

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                float red;
                float green;
                float blue;
                int paneX = x / 2;
                int paneY = y / 2;
                int cellIndex = paneY * width + paneX;
                boolean firstRow = paneY == 0;
                boolean lastRow = paneY == height - 1;
                boolean firstColumn = paneX == 0;
                boolean lastColumn = paneX == width - 1;
                int cellIndexAbove = firstRow ? cellIndex : cellIndex - width;
                int cellIndexBelow = lastRow ? cellIndex : cellIndex + width;
                int cellIndexLeft = firstColumn ? cellIndex : cellIndex - 1;
                int cellIndexRight = lastColumn ? cellIndex : cellIndex + 1;
                int cellIndexAboveRight = (firstRow || lastColumn) ? cellIndex : cellIndexAbove + 1;
                int cellIndexBelowLeft = (lastRow || firstColumn) ? cellIndex : cellIndexBelow - 1;
                int cell = y % 2 + x % 2;

                float[] g1Pane;
                float[] rPane;
                float[] bPane;
                float[] g2Pane;
                /*
                 * G1 RR 0 1
                 * BB G2 2 3
                 */
                g1Pane = pane0;
                rPane = pane1;
                bPane = pane2;
                g2Pane = pane3;
                switch (cell) {
                    case 0: // G1: we have the green, must interpolate red and blue
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR | (G1)  RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        red = (rPane[cellIndexLeft] + rPane[cellIndex]) / 2;
                        green = g1Pane[cellIndex];
                        blue = (bPane[cellIndexAbove] + bPane[cellIndex]) / 2;
                        break;
                    case 1: // RR: we have the red, must interpolate green and blue
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1  (RR) | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        red = rPane[cellIndex];
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexRight] + g2Pane[cellIndexAbove] + g2Pane[cellIndex]) / 4;
                        blue = (bPane[cellIndexAbove] + bPane[cellIndexAboveRight] + bPane[cellIndex] + bPane[cellIndexRight]) / 4;
                        break;
                    case 2: // BB: we have the blue, must interpolate red and green
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 | (BB)  G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        red = (rPane[cellIndexLeft] + rPane[cellIndex] + rPane[cellIndexBelowLeft] + rPane[cellIndexBelow]) / 4;
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexBelow] + g2Pane[cellIndexLeft] + g2Pane[cellIndex]) / 4;
                        blue = pane2[cellIndex];
                        break;
                    case 3: // G2: we have the green, must interpolate red and blue
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB  (G2) | BB G2
                        // ------+-----------+------
                        // G1 RR |  G1   RR  | G1 RR
                        // BB G2 |  BB   G2  | BB G2
                        red = (rPane[cellIndex] + rPane[cellIndexBelow]) / 2;
                        green = pane3[cellIndex];
                        blue = (bPane[cellIndex] + bPane[cellIndexRight]) / 2;
                        break;
                    default:
                        throw new IllegalArgumentException("Impossible cell: " + cell);
                }
                demosaicked[position] = rMultiplier * red;
                position++;
                demosaicked[position] = green;
                position++;
                demosaicked[position] = bMultiplier * blue;
                position++;
            }
        }
        return demosaicked;
    }

    private float[] simpleRGGBDemosaic() {
        int fullWidth = width * 2;
        int fullHeight = height * 2;
        float[] demosaicked = new float[fullWidth * fullHeight * 3];

        int position = 0;

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                float red;
                float green;
                float blue;
                int paneX = x / 2;
                int paneY = y / 2;
                int cellIndex = paneY * width + paneX;
                boolean firstRow = paneY == 0;
                boolean lastRow = paneY == height - 1;
                boolean firstColumn = paneX == 0;
                boolean lastColumn = paneX == width - 1;
                int cellIndexAbove = firstRow ? cellIndex : cellIndex - width;
                int cellIndexBelow = lastRow ? cellIndex : cellIndex + width;
                int cellIndexLeft = firstColumn ? cellIndex : cellIndex - 1;
                int cellIndexRight = lastColumn ? cellIndex : cellIndex + 1;
                int cellIndexAboveLeft = (firstRow || firstColumn) ? cellIndex : cellIndexAbove - 1;
                int cellIndexBelowRight = (lastRow || lastColumn) ? cellIndex : cellIndexBelow + 1;
                int cell = y % 2 + x % 2;

                float[] g1Pane;
                float[] rPane;
                float[] bPane;
                float[] g2Pane;
                /*
                 * RR G1 0 1
                 * G2 BB 2 3
                 */
                rPane = pane0;
                g1Pane = pane1;
                g2Pane = pane2;
                bPane = pane3;
                switch (cell) {
                    case 0: // RR: we have the red, must interpolate green and blue
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 | (RR)  G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        red = rPane[cellIndex];
                        green = (g1Pane[cellIndexLeft] + g1Pane[cellIndex] + g2Pane[cellIndexAbove] + g2Pane[cellIndex]) / 4;
                        blue = (bPane[cellIndexAboveLeft] + bPane[cellIndexAbove] + bPane[cellIndexLeft] + bPane[cellIndex]) / 4;
                        break;
                    case 1: // G1: we have the red, must interpolate green and blue
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR  (G1) | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        red = (rPane[cellIndex] + rPane[cellIndexRight]) / 2;
                        green =g1Pane[cellIndex];
                        blue = (bPane[cellIndexAbove] + bPane[cellIndex]) / 2;
                        break;
                    case 2: // G2: we have the green, must interpolate red and blue
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB | (G2)  BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        red = (rPane[cellIndex] + rPane[cellIndexBelow]) / 2;
                        green = g2Pane[cellIndex];
                        blue = (bPane[cellIndexLeft] + bPane[cellIndex]) / 2;
                        break;
                    case 3: // BB: we have the blue, must interpolate red and green
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2  (BB) | G2 BB
                        // ------+-----------+------
                        // RR G1 |  RR   G1  | RR G1
                        // G2 BB |  G2   BB  | G2 BB
                        red = (rPane[cellIndex] + rPane[cellIndexRight] + rPane[cellIndexBelow] + rPane[cellIndexBelowRight]) / 4;
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexBelow] + g2Pane[cellIndex] + g2Pane[cellIndexRight]) / 4;
                        blue = bPane[cellIndex];
                        break;
                    default:
                        throw new IllegalArgumentException("Impossible cell: " + cell);
                }
                demosaicked[position] = rMultiplier * red;
                position++;
                demosaicked[position] = green;
                position++;
                demosaicked[position] = bMultiplier * blue;
                position++;
            }
        }
        return demosaicked;
    }

    @Override
    public BayerImage clone() {
        return new BayerImage(
                cfa,
                width, height,
                pane0.clone(), pane1.clone(), pane2.clone(), pane3.clone(),
                rMultiplier, bMultiplier
        );
    }
}
