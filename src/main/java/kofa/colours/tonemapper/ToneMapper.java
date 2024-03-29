package kofa.colours.tonemapper;

import kofa.io.RgbImage;

/**
 * The interface for HDR -> SDR tone mapping
 *
 * @param <S> the colour space the instance uses
 */
public interface ToneMapper<S> {
    void toneMap(RgbImage image);
}
