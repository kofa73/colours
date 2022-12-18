package kofa.io;

public class RgbImage {
    private final float[][] redChannel;
    private final float[][] greenChannel;
    private final float[][] blueChannel;
    private final int height;
    private final int width;
    
    public RgbImage(int height, int width) {
        this.width = width;
        this.height = height;
        redChannel = new float[height][width];
        greenChannel = new float[height][width];
        blueChannel = new float[height][width];
    }
    
    public RgbImage(int height, int width, float[][] reds, float[][] greens, float[][] blues) {
        this.width = width;
        this.height = height;
        redChannel = reds;
        greenChannel = greens;
        blueChannel = blues;
    }
    
    public float[][] redChannel() {
        return redChannel;
    }
    
    public float[][] greenChannel() {
        return greenChannel;
    }
    
    public float[][] blueChannel() {
        return blueChannel;
    }

    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
}
