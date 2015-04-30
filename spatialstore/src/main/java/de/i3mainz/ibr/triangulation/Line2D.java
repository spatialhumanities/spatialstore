package de.i3mainz.ibr.triangulation;

import de.i3mainz.ibr.geometry.Point;

/**
 *
 * @author s3b31293
 */
public class Line2D {

	private static final double epsilon = 0.0001;
	private Point2D start, end;

	public Line2D(Point2D start, Point2D end) {
		this.start = start;
		this.end = end;
	}

	public Point2D newCut(Line2D other) {
		double x1 = this.start.getX();
		double y1 = this.start.getY();
		double x2 = this.end.getX();
		double y2 = this.end.getY();
		double x3 = other.start.getX();
		double y3 = other.start.getY();
		double x4 = other.end.getX();
		double y4 = other.end.getY();

		double nenner = (y4 - y3) * (x2 - x1) - (y2 - y1) * (x4 - x3);
		double xs = ((x4 - x3) * (x2 * y1 - x1 * y2) - (x2 - x1) * (x4 * y3 - x3 * y4)) / nenner;
		double ys = ((y1 - y2) * (x4 * y3 - x3 * y4) - (y3 - y4) * (x2 * y1 - x1 * y2)) / nenner;
		return new Point2D(xs, ys);
	}

	public Point2D cut(Line2D other) {
		Point a = new Point(this.start.getX(), this.start.getY(), 1);
		Point b = new Point(this.end.getX(), this.end.getY(), 1);
		Point c = new Point(other.start.getX(), other.start.getY(), 1);
		Point d = new Point(other.end.getX(), other.end.getY(), 1);

		Point lineOne = Point.cross(a, b);
		Point lineTow = Point.cross(c, d);

		Point cut = Point.cross(lineOne, lineTow);

		cut.mul(1 / cut.getZ());

		return new Point2D(cut.getX(), cut.getY());
	}

	public boolean isCut(Line2D other) {

		Point2D cut = this.newCut(other);
		if (boundingBoxCheck(this, cut)) {
			return boundingBoxCheck(other, cut);
		}
		return false;
	}

	/**
	 *
	 * @param other
	 * @return true if boundingBox intersect
	 */
	private static boolean boundingBoxCheck(Line2D other, Point2D cut) {
		if (other.start.getY() > other.end.getY()) {
			Point2D tmp = other.start;
			other.start = other.end;
			other.end = tmp;
		}

		if (other.start.getX() < other.end.getX()) {
			if (cut.getX() >= other.start.getX() - epsilon && cut.getX() <= other.end.getX() + epsilon) {
				if (cut.getY() >= other.start.getY() - epsilon && cut.getY() <= other.end.getY() + epsilon) {
					return true;
				}
			}
		} else {
			if (cut.getX() <= other.start.getX() + epsilon && cut.getX() >= other.end.getX() - epsilon) {
				if (cut.getY() >= other.start.getY() - epsilon && cut.getY() <= other.end.getY() + epsilon) {
					return true;
				}
			}
		}
		return false;
	}
}
