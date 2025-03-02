package kofa.maths;

public class IntersectionOfLines {
    private IntersectionOfLines() {
    }

    /**
     * Finds the intersection of lines A-B and P-Q
     *
     * @param xa point A x-coordinate
     * @param ya point A y-coordinate
     * @param xb point B x-coordinate
     * @param yb point B y-coordinate
     * @param xp point P x-coordinate
     * @param yp point P y-coordinate
     * @param xq point Q x-coordinate
     * @param yq point Q y-coordinate
     * @return an X-Y coordinate pair; null, if the lines are parallel or coincident
     */
    public static double[] intersectionOf(
            double xa, double ya,
            double xb, double yb,
            double xp, double yp,
            double xq, double yq) {

        double dx_ab = xb - xa;
        double dy_ab = yb - ya;
        double dx_pq = xq - xp;
        double dy_pq = yq - yp;

        double denominator = dx_pq * dy_ab - dx_ab * dy_pq;

        if (denominator == 0) {
            // Lines are parallel or coincident
            return null;
        }

        double t = (dx_pq * (yp - ya) - dy_pq * (xp - xa)) / denominator;

        double x = xa + t * dx_ab;
        double y = ya + t * dy_ab;

        /*
        double u = (dx_ab * (yp - ya) - dy_ab * (xp - xa)) / denominator;
        double x_check = xp + u * dx_pq;
        double y_check = yp + u * dy_pq;
        if (abs(x - x_check) > 1e-9 || abs(y - y_check) > 1e-9) {
            System.out.println("Warning: Discrepancy in intersection calculation.");
        }
         */

        return new double[]{x, y};
    }
}
