package kofa.colours.model;

import java.awt.image.Raster;

import static java.lang.Math.*;

public class BayerImage {
    public final int height;
    public final int width;
    public final int[] r;
    public final int[] g1;
    public final int[] g2;
    public final int[] b;

    public final int[] rHistogram = new int[65536];
    public final int[] g1Histogram = new int[65536];
    public final int[] g2Histogram = new int[65536];
    public final int[] bHistogram = new int[65536];

    public BayerImage(Raster raster) {
        int rasterHeight = raster.getHeight();
        height = rasterHeight / 2;
        int rasterWidth = raster.getWidth();
        width = rasterWidth / 2;
        r = new int[height * width];
        g1 = new int[height * width];
        g2 = new int[height * width];
        b = new int[height * width];

        int[] pixelBuffer = new int[1];

        int rMin = Integer.MAX_VALUE;
        int g1Min = Integer.MAX_VALUE;
        int g2Min = Integer.MAX_VALUE;
        int bMin = Integer.MAX_VALUE;
        int rMax = Integer.MIN_VALUE;
        int g1Max = Integer.MIN_VALUE;
        int g2Max = Integer.MIN_VALUE;
        int bMax = Integer.MIN_VALUE;

        /* white balance multipliers from exiv2:
        $ exiv2 -pt 2024-11-04-11-00-04-P1060036.RW2|grep -i wb
        Exif.PanasonicRaw.WBRedLevel                 Short       1  316
        Exif.PanasonicRaw.WBGreenLevel               Short       1  266
        Exif.PanasonicRaw.WBBlueLevel                Short       1  878
         */


        int index = 0;
        for (int y = 0; y < rasterHeight; y += 2) {
            for (int x = 0; x < rasterWidth; x += 2) {
                raster.getPixel(x, y, pixelBuffer);
                int pixel = pixelBuffer[0];
                g1[index] = pixel;
                g1Min = min(g1Min, pixel);
                g1Max = max(g1Max, pixel);

                raster.getPixel(x + 1, y, pixelBuffer);
                pixel = pixelBuffer[0];
                r[index] = round(pixel * 316.0f / 266);
                rMin = min(rMin, pixel);
                rMax = max(rMax, pixel);

                raster.getPixel(x, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                b[index] = round(pixel * 878.0f / 266);
                bMin = min(bMin, pixel);
                bMax = max(bMax, pixel);

                raster.getPixel(x + 1, y + 1, pixelBuffer);
                pixel = pixelBuffer[0];
                g2[index] = pixel;
                g2Min = min(g2Min, pixel);
                g2Max = max(g2Max, pixel);

                index++;
            }
        }

        for (int i = 0; i < index; i++) {
                r[i]  -= rMin;
                g1[i] -= g1Min;
                g2[i] -= g2Min;
                b[i]  -= bMin;
        }
    }

    public int[] simpleDemosaic() {
        int fullWidth = width * 2;
        int fullHeight = height * 2;
        int[] demosaicked = new int[fullWidth * fullHeight * 3];

        int position = 0;

        for (int y = 0; y < fullHeight; y++) {
            for (int x = 0; x < fullWidth; x++) {
                int red;
                int green;
                int blue;
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
                int cellIndexAboveRight = (firstRow || lastColumn) ? cellIndex : cellIndexAbove + 1;
                int cellIndexBelowLeft = (lastRow || firstColumn) ? cellIndex : cellIndexBelow - 1;
                int cellIndexBelowRight = (lastRow || lastColumn) ? cellIndex : cellIndexBelow + 1;

                /*
                 * G1 RR 0 1
                 * BB G2 2 3
                 */
                int cell = y % 2 + x % 2;
                switch (cell) {
                    case 0:
                        red = round((r[cellIndex] + r[cellIndexRight]) / 2.0f);
                        green = g1[cellIndex];
                        blue = round((b[cellIndexAbove] + b[cellIndex]) / 2.0f);
                        break;
                    case 1:
                        red = r[cellIndex];
                        green = round((g1[cellIndexLeft] + g1[cellIndexRight] + g2[cellIndexAbove] + g2[cellIndexBelow]) / 4.0f);
                        blue = round((b[cellIndexAboveLeft] + b[cellIndexAboveRight] + b[cellIndexBelowLeft] + b[cellIndexBelowRight]) / 4.0f);
                        break;
                    case 2:
                        red = round((r[cellIndexAboveLeft] + r[cellIndexAboveRight] + r[cellIndexBelowLeft] + r[cellIndexBelowRight]) / 4.0f);
                        green = round((g2[cellIndexLeft] + g2[cellIndexRight] + g1[cellIndexAbove] + g1[cellIndexBelow]) / 4.0f);
                        blue = b[cellIndex];
                        break;
                    case 3:
                        red = round((r[cellIndex] + r[cellIndexBelow]) / 2.0f);
                        green = g2[cellIndex];
                        blue = round((b[cellIndexLeft] + b[cellIndex]) / 2.0f);
                        break;
                    default:
                        throw new IllegalArgumentException("Impossible cell: " + cell);
                }
                demosaicked[position] = red;
                position++;
                demosaicked[position] = green;
                position++;
                demosaicked[position] = blue;
                position++;
            }
        }
        return demosaicked;
    }
}
