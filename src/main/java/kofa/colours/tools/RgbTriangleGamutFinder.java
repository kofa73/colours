package kofa.colours.tools;

import kofa.colours.spaces.SpaceParameters;

import static java.lang.Math.min;
import static java.lang.Math.*;
import static kofa.maths.IntersectionOfLines.intersectionOf;
import static kofa.maths.MathHelpers.*;

public class RgbTriangleGamutFinder {
    private final SpaceParameters spaceParameters;

    public RgbTriangleGamutFinder(SpaceParameters spaceParameters) {
        this.spaceParameters = spaceParameters;
    }

    public double distanceFromGamut(double angle) {
        // end-point of unit vector starting from the neutral chromaticity coordinate direction
        double dx = cos(angle);
        double x = spaceParameters.whitePointX() + dx;
        double dy = sin(angle);
        double y = spaceParameters.whitePointY() + dy;

        double[] unitFromWhiteInDirectionOfAngle = {dx, dy};

        double distanceSquaredFromRedBlue = Double.MAX_VALUE;
        double distanceSquaredFromBlueGreen = Double.MAX_VALUE;
        double distanceSquaredFromGreenRed = Double.MAX_VALUE;

        // intersection with the gamut boundary lines
        double[] redBlueIntersection = intersectionOf(
                spaceParameters.whitePointX(), spaceParameters.whitePointY(),
                x, y,
                spaceParameters.redX(), spaceParameters.redY(),
                spaceParameters.blueX(), spaceParameters.blueY()
        );

        if (redBlueIntersection != null) {
            double[] whitePointToRedBlueIntersection = vectorFromWhitePoint(redBlueIntersection[0], redBlueIntersection[1]);
            if (dot2(unitFromWhiteInDirectionOfAngle, whitePointToRedBlueIntersection) > 0) {
                distanceSquaredFromRedBlue = len2Squared(whitePointToRedBlueIntersection);
            }
        }

        double[] blueGreenIntersection = intersectionOf(
                spaceParameters.whitePointX(), spaceParameters.whitePointY(),
                x, y,
                spaceParameters.blueX(), spaceParameters.blueY(),
                spaceParameters.greenX(), spaceParameters.greenY()
        );

        if (blueGreenIntersection != null) {
            double[] whitePointToBlueGreenIntersection = vectorFromWhitePoint(blueGreenIntersection[0], blueGreenIntersection[1]);
            if (dot2(unitFromWhiteInDirectionOfAngle, whitePointToBlueGreenIntersection) > 0) {
                distanceSquaredFromBlueGreen = len2Squared(whitePointToBlueGreenIntersection);
            }
        }
        
        double[] greenRedIntersection = intersectionOf(
                spaceParameters.whitePointX(), spaceParameters.whitePointY(),
                x, y,
                spaceParameters.greenX(), spaceParameters.greenY(),
                spaceParameters.redX(), spaceParameters.redY()
                );


        if (greenRedIntersection != null) {
            double[] whitePointToGreenRedIntersection = vectorFromWhitePoint(greenRedIntersection[0], greenRedIntersection[1]);
            if (dot2(unitFromWhiteInDirectionOfAngle, whitePointToGreenRedIntersection) > 0) {
                distanceSquaredFromGreenRed = len2(whitePointToGreenRedIntersection);
            }
        }

        double distanceSquared = min(distanceSquaredFromBlueGreen, min(distanceSquaredFromRedBlue, distanceSquaredFromGreenRed));
        return sqrt(distanceSquared);
    }

    private double[] vectorFromWhitePoint(double x, double y) {
        return vec2(x - spaceParameters.whitePointX(), y - spaceParameters.whitePointY());
    }
}
