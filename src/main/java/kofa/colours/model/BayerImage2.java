package kofa.colours.model;

import java.awt.image.Raster;

public class BayerImage2 implements Cloneable {
    private static final int PADDING_SIZE = 16;

    public final int paneWidth;
    public final int paneHeight;
    public final float[] pane0;
    public final float[] pane1;
    public final float[] pane2;
    public final float[] pane3;

    private final float rMultiplier;
    private final float bMultiplier;
    private final CFA cfa;

    private BayerImage2(CFA cfa, int paneWidth, int paneHeight, float[] pane0, float[] pane1, float[] pane2, float[] pane3, float rMultiplier, float bMultiplier) {
        this.cfa = cfa;
        this.paneWidth = paneWidth;
        this.paneHeight = paneHeight;
        this.pane0 = pane0;
        this.pane1 = pane1;
        this.pane2 = pane2;
        this.pane3 = pane3;
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
    }

    public BayerImage2(Raster raster, CFA cfa, float rMultiplier, float bMultiplier) {
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
        this.cfa = cfa;
        int rasterHeight = raster.getHeight();
        paneHeight = rasterHeight / 2;
        int rasterWidth = raster.getWidth();
        paneWidth = rasterWidth / 2;
        pane0 = new float[paneHeight * paneWidth];
        pane1 = new float[paneHeight * paneWidth];
        pane2 = new float[paneHeight * paneWidth];
        pane3 = new float[paneHeight * paneWidth];

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

    public float[] toRGB() {
        // 2x2 pixels per cell; padding is applied on both sides
        int innerContentWidth = 2 * paneWidth;
        int paddedWidth = innerContentWidth + 2 * PADDING_SIZE;
        int innerContentHeight = paneHeight * 2;
        int paddedHeight = innerContentHeight + 2 * PADDING_SIZE;
        int componentsPerRow = paddedWidth * 3;
        float[] rgb = new float[componentsPerRow * paddedHeight];
        switch (cfa) {
            case RGGB: prepareRGGB(rgb, componentsPerRow); break;
            case GRBG: prepareGRBG(rgb, componentsPerRow); break;
            default: throw new IllegalArgumentException("Unsupported CFA: " + cfa);
        };
        for (int y = PADDING_SIZE; y < innerContentHeight + PADDING_SIZE; y++) {
            int rowStart = y * componentsPerRow;
            int firstPixelOffset = rowStart + 3 * PADDING_SIZE;
            // -2, because we copy 2 pixels
            int lastPixelPairOffset = firstPixelOffset + 3 * (innerContentWidth - 2);
            int rightBorderStart = firstPixelOffset + 3 * (innerContentWidth);
            for (int paddingPixelNumber = 0; paddingPixelNumber < PADDING_SIZE; paddingPixelNumber += 2) {
                int leftPaddingPixelOffset = rowStart + paddingPixelNumber * 3;
                int rightPaddingPixelOffset = rightBorderStart + paddingPixelNumber * 3;
                // 2 pixels, 2 * 3 components
                for (int c = 0; c < 6; c++) {
                    rgb[leftPaddingPixelOffset + c] = rgb[firstPixelOffset + c];
                    rgb[rightPaddingPixelOffset + c] = rgb[lastPixelPairOffset + c];
                }
            }
        }

        int firstContentRowStart = PADDING_SIZE * componentsPerRow;
        int lastContentRowStart = firstContentRowStart + (innerContentHeight - 1) * componentsPerRow;
        for (int row = 0; row < PADDING_SIZE; row++) {
            int topBorderRowStart = row * componentsPerRow;
            System.arraycopy(rgb, firstContentRowStart, rgb, topBorderRowStart, componentsPerRow);
            int bottomBorderRowStart = topBorderRowStart + (PADDING_SIZE + innerContentHeight) * componentsPerRow;
            System.arraycopy(rgb, lastContentRowStart, rgb, bottomBorderRowStart, componentsPerRow);
        }
        return rgb;
    }

    private void prepareRGGB(float[] rgb, int componentsPerRow) {
        int paneIndex = 0;
        for (int paneY = 0; paneY < paneHeight; paneY++) {
            // 2 output rows for each pane row; skip padding rows on top; skip padding columns on left, 3 components per pixel
            int outIndex = (paneY * 2 + PADDING_SIZE) * componentsPerRow + 3 * PADDING_SIZE;
            for (int paneX = 0; paneX < paneWidth; paneX++) {
                float r = pane0[paneIndex];
                float g1 = pane1[paneIndex];
                float g2 = pane2[paneIndex];
                float b = pane3[paneIndex];
                rgb[outIndex] = r;
                // +3: next pixel; +1: green component
                rgb[outIndex + 3 + 1] = g1;
                // + componentsPerRow: next row; +1: green component
                rgb[outIndex + componentsPerRow + 1] = g2;
                // + 3: next pixel; + 2: blue component
                rgb[outIndex + componentsPerRow + 3 + 2] = b;
                paneIndex++;
                outIndex += 6;
            }
        }
    }

    private void prepareGRBG(float[] rgb, int componentsPerRow) {
        int paneIndex = 0;
        for (int paneY = 0; paneY < paneHeight; paneY++) {
            // 2 output rows for each pane row; skip padding rows on top; skip padding columns on left, 3 components per pixel
            int outIndex = (paneY * 2 + PADDING_SIZE) * componentsPerRow + 3 * PADDING_SIZE;
            for (int paneX = 0; paneX < paneWidth; paneX++) {
                float g1 = pane0[paneIndex];
                float r = pane1[paneIndex];
                float b = pane2[paneIndex];
                float g2 = pane3[paneIndex];
                // +1: green component
                rgb[outIndex + 1] = g1;
                // +3: next pixel
                rgb[outIndex + 3] = r;
                // + componentsPerRow: next row; +2: blue component
                rgb[outIndex + componentsPerRow + 2] = b;
                // + 3: next pixel; + 1: green component
                rgb[outIndex + componentsPerRow + 3 + 1] = g2;
                paneIndex++;
                outIndex += 6;
            }
        }
    }

    public float[] bilinearDemosaic() {
        float[] rgb = toRGB();
        int redColumn = switch (cfa) {
            case RGGB -> PADDING_SIZE;
            case GRBG -> PADDING_SIZE - 1;
        };
        int innerContentHeight = paneHeight * 2;
        int innerContentWidth = 2 * paneWidth;
        int paddedWidth = innerContentWidth + 2 * PADDING_SIZE;
        int paddedHeight = innerContentHeight + 2 * PADDING_SIZE;
        int componentsPerRow = paddedWidth * 3;
        for (int y = PADDING_SIZE; y < PADDING_SIZE + innerContentHeight; y += 2) {
            for (int x = redColumn; x < redColumn + innerContentWidth; x++) {
                int pixelIndex = y * componentsPerRow + 3 * x;
                // RG | RG row
                // original red pixel
                int redIndex = pixelIndex;
                int greenIndex = redIndex + 1;
                int blueIndex = redIndex + 2;
                // original red pixel: red is already set
                // green: left, right, above, below
                rgb[greenIndex] = (rgb[greenIndex - 3] + rgb[greenIndex + 3] + rgb[greenIndex - componentsPerRow] + rgb[greenIndex + componentsPerRow]) / 4;
                // blue: above left, above right, below left, below right
                rgb[blueIndex] = (rgb[blueIndex - componentsPerRow - 3] + rgb[blueIndex - componentsPerRow + 3] + rgb[blueIndex + componentsPerRow - 3] + rgb[blueIndex + componentsPerRow + 3]) / 4;

                // original green1 pixel
                redIndex = redIndex + 3;
                greenIndex = redIndex + 1;
                blueIndex = redIndex + 2;
                // red: left and right
                rgb[redIndex] = (rgb[redIndex - 3] + rgb[redIndex + 3]) / 2;
                // green: already set
                // blue: above, below
                rgb[blueIndex] = (rgb[blueIndex - componentsPerRow] + rgb[blueIndex + componentsPerRow]) / 2;


                // GB | GB row
                // green2 pixel
                redIndex = pixelIndex + componentsPerRow;
                greenIndex = redIndex + 1;
                blueIndex = redIndex + 2;
                // red: above and below
                rgb[redIndex] = (rgb[redIndex - componentsPerRow] + rgb[redIndex + componentsPerRow]) / 2;
                // original green pixel: green is already set
                // blue: left and right
                rgb[blueIndex] = (rgb[blueIndex - 3] + rgb[blueIndex + 3]) / 2;

                // blue pixel
                redIndex = redIndex + 3;
                greenIndex = redIndex + 1;
                blueIndex = redIndex + 2;
                // red: top left/right, bottom left/right
                rgb[redIndex] = (rgb[redIndex - componentsPerRow - 3] + rgb[redIndex - componentsPerRow + 3] + rgb[redIndex + componentsPerRow - 3] + rgb[redIndex + componentsPerRow + 3]) / 4;
                // green: left, right, above, below
                rgb[greenIndex] = (rgb[greenIndex - 3] + rgb[greenIndex + 3] + rgb[greenIndex - componentsPerRow] + rgb[greenIndex + componentsPerRow]) / 4;
            }
        }
        return rgb;
    }

    private float[] simpleGRBGDemosaic() {
        int fullWidth = paneWidth * 2;
        int fullHeight = paneHeight * 2;
        float[] demosaicked = new float[fullWidth * fullHeight * 3];

        int position = 0;

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                float red;
                float green;
                float blue;
                int paneX = x / 2;
                int paneY = y / 2;
                int cellIndex = paneY * paneWidth + paneX;
                boolean firstRow = paneY == 0;
                boolean lastRow = paneY == paneHeight - 1;
                boolean firstColumn = paneX == 0;
                boolean lastColumn = paneX == paneWidth - 1;
                int cellIndexAbove = firstRow ? cellIndex : cellIndex - paneWidth;
                int cellIndexBelow = lastRow ? cellIndex : cellIndex + paneWidth;
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
        int fullWidth = paneWidth * 2;
        int fullHeight = paneHeight * 2;
        float[] demosaicked = new float[fullWidth * fullHeight * 3];

        int position = 0;

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                float red;
                float green;
                float blue;
                int paneX = x / 2;
                int paneY = y / 2;
                int cellIndex = paneY * paneWidth + paneX;
                boolean firstRow = paneY == 0;
                boolean lastRow = paneY == paneHeight - 1;
                boolean firstColumn = paneX == 0;
                boolean lastColumn = paneX == paneWidth - 1;
                int cellIndexAbove = firstRow ? cellIndex : cellIndex - paneWidth;
                int cellIndexBelow = lastRow ? cellIndex : cellIndex + paneWidth;
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
    public BayerImage2 clone() {
        return new BayerImage2(
                cfa,
                paneWidth, paneHeight,
                pane0.clone(), pane1.clone(), pane2.clone(), pane3.clone(),
                rMultiplier, bMultiplier
        );
    }
}
