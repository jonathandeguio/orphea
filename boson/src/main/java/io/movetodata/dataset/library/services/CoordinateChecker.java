package io.movetodata.dataset.library.services;

public class CoordinateChecker {
    public static class Point {
        public Double x, y;

        public Point(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "{" + x.toString() + ", " + y.toString() + "}";
        }
    }

    public static class Line {
        public Point p1, p2;

        public Line(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    static double onLine(Line l1, Point p) {
        // Check whether p is on the line or not
        if (p.x <= Math.max(l1.p1.x, l1.p2.x)
                && p.x <= Math.min(l1.p1.x, l1.p2.x)
                && (p.y <= Math.max(l1.p1.y, l1.p2.y)
                && p.y <= Math.min(l1.p1.y, l1.p2.y)))
            return 1;

        return 0;
    }


    static double direction(Point a, Point b, Point c) {
        double val = (b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y);

        // Collinear
        if (val == 0)
            return 0;

            // Anti-clockwise direction
        else if (val < 0)
            return 2;

        // Clockwise direction
        return 1;
    }

    static double isIntersect(Line l1, Line l2) {
        // Four direction for two lines and points of other
        // line
        double dir1 = direction(l1.p1, l1.p2, l2.p1);
        double dir2 = direction(l1.p1, l1.p2, l2.p2);
        double dir3 = direction(l2.p1, l2.p2, l1.p1);
        double dir4 = direction(l2.p1, l2.p2, l1.p2);

        // When intersecting
        if (dir1 != dir2 && dir3 != dir4)
            return 1;

        // When p2 of line2 are on the line1
        if (dir1 == 0 && onLine(l1, l2.p1) == 1)
            return 1;

        // When p1 of line2 are on the line1
        if (dir2 == 0 && onLine(l1, l2.p2) == 1)
            return 1;

        // When p2 of line1 are on the line2
        if (dir3 == 0 && onLine(l2, l1.p1) == 1)
            return 1;

        // When p1 of line1 are on the line2
        if (dir4 == 0 && onLine(l2, l1.p2) == 1)
            return 1;

        return 0;
    }

    static double checkInside(Point[] poly, int n, Point p) {

        // When polygon has less than 3 edge, it is not
        // polygon

        if (n < 3)
            return 0;

        // Create a point at infinity, y is same as point p
        Point pt = new Point(99999.0, p.y);
        Line exline = new Line(p, pt);
        int count = 0;
        int i = 0;
        do {

            // Forming a line from two consecutive points of
            // poly
            Line side
                    = new Line(poly[i], poly[(i + 1) % n]);
            if (isIntersect(side, exline) == 1) {

                // If side is intersects exline
                if (direction(side.p1, p, side.p2) == 0)
                    return onLine(side, p);
                count++;
            }
            i = (i + 1) % n;
        } while (i != 0);

        // When count is odd
        return count & 1;
    }
}
