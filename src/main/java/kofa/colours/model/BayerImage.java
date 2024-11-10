package kofa.colours.model;

import java.awt.image.Raster;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BayerImage {
    public enum CFA {RGGB, GRBG}

    public final int height;
    public final int width;
    public final int[] pane0;
    public final int[] pane1;
    public final int[] pane3;
    public final int[] pane2;

    // just for debugging
    private int pane01Min = Integer.MAX_VALUE;
    private int pane00Min = Integer.MAX_VALUE;
    private int pane_11Min = Integer.MAX_VALUE;
    private int pane10Min = Integer.MAX_VALUE;
    private int pane01Max = Integer.MIN_VALUE;
    private int pane00Max = Integer.MIN_VALUE;
    private int pane_11Max = Integer.MIN_VALUE;
    private int pane10Max = Integer.MIN_VALUE;

    public final int[] pane01Histogram = new int[65536];
    public final int[] pane00Histogram = new int[65536];
    public final int[] pane_11Histogram = new int[65536];
    public final int[] pane10Histogram = new int[65536];
    private final float rMultiplier;
    private final float bMultiplier;

    public BayerImage(Raster raster, float rMultiplier, float bMultiplier) {
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
        int rasterHeight = raster.getHeight();
        height = rasterHeight / 2;
        int rasterWidth = raster.getWidth();
        width = rasterWidth / 2;
        pane0 = new int[height * width];
        pane1 = new int[height * width];
        pane2 = new int[height * width];
        pane3 = new int[height * width];

        int[] pixelBuffer = new int[1];
        
        int index = 0;
        for (int y = 0; y < rasterHeight; y += 2) {
            for (int x = 0; x < rasterWidth; x += 2) {
                raster.getPixel(x, y, pixelBuffer);
                int pixel = pixelBuffer[0];
                pane0[index] = pixel;
                pane00Min = min(pane00Min, pixel);
                pane00Max = max(pane00Max, pixel);
                pane00Histogram[pixel]++;

                raster.getPixel(x + 1, y, pixelBuffer);
                pixel = pixelBuffer[0];
                pane1[index] = pixel;
                pane01Min = min(pane01Min, pixel);
                pane01Max = max(pane01Max, pixel);
                pane01Histogram[pixel]++;

                raster.getPixel(x, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                pane2[index] = pixel;
                pane10Min = min(pane10Min, pixel);
                pane10Max = max(pane10Max, pixel);
                pane10Histogram[pixel]++;

                raster.getPixel(x + 1, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                pane3[index] = pixel;
                pane_11Min = min(pane_11Min, pixel);
                pane_11Max = max(pane_11Max, pixel);
                pane_11Histogram[pixel]++;

                index++;
            }
        }
    }

    public float[] simpleDemosaic(CFA cfa) {
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

                int[] g1Pane;
                int[] rPane;
                int[] bPane;
                int[] g2Pane;
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
                        red = (rPane[cellIndexLeft] + rPane[cellIndex]) / 2.0f;
                        green = g1Pane[cellIndex];
                        blue = (bPane[cellIndexAbove] + bPane[cellIndex]) / 2.0f;
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
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexRight] + g2Pane[cellIndexAbove] + g2Pane[cellIndex]) / 4.0f;
                        blue = (bPane[cellIndexAbove] + bPane[cellIndexAboveRight] + bPane[cellIndex] + bPane[cellIndexRight]) / 4.0f;
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
                        red = (rPane[cellIndexLeft] + rPane[cellIndex] + rPane[cellIndexBelowLeft] + rPane[cellIndexBelow]) / 4.0f;
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexBelow] + g2Pane[cellIndexLeft] + g2Pane[cellIndex]) / 4.0f;
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
                        red = (rPane[cellIndex] + rPane[cellIndexBelow]) / 2.0f;
                        green = pane3[cellIndex];
                        blue = (bPane[cellIndex] + bPane[cellIndexRight]) / 2.0f;
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

                int[] g1Pane;
                int[] rPane;
                int[] bPane;
                int[] g2Pane;
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
                        green = (g1Pane[cellIndexLeft] + g1Pane[cellIndex] + g2Pane[cellIndexAbove] + g2Pane[cellIndex]) / 4.0f;
                        blue = (bPane[cellIndexAboveLeft] + bPane[cellIndexAbove] + bPane[cellIndexLeft] + bPane[cellIndex]) / 4.0f;
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
                        red = (rPane[cellIndex] + rPane[cellIndexRight]) / 2.0f;
                        green =g1Pane[cellIndex];
                        blue = (bPane[cellIndexAbove] + bPane[cellIndex]) / 2.0f;
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
                        red = (rPane[cellIndex] + rPane[cellIndexBelow]) / 2.0f;
                        green = g2Pane[cellIndex];
                        blue = (bPane[cellIndexLeft] + bPane[cellIndex]) / 2.0f;
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
                        red = (rPane[cellIndex] + rPane[cellIndexRight] + rPane[cellIndexBelow] + rPane[cellIndexBelowRight]) / 4.0f;
                        green = (g1Pane[cellIndex] + g1Pane[cellIndexBelow] + g2Pane[cellIndex] + g2Pane[cellIndexRight]) / 4.0f;
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
}
