package de.i3mainz.ibr.triangulation;

public class Circle2D {
	private Point2D center;
	private double radius;

	public Circle2D() {
		this.center = new Point2D();
		this.radius = 0;
	};

	/**
	 * @param center
	 * @param radius
	 */
	public Circle2D(Point2D center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	/**
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	protected void circumCircle(Point2D p1, Point2D p2, Point2D p3) {

		double cp = crossProduct(p1, p2, p3);
		if (cp != 0.0) {
			double p1Sq, p2Sq, p3Sq;
			double num;
			double cx, cy;

			p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
			p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
			p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();
			num = p1Sq * (p2.getY() - p3.getY()) + p2Sq
					* (p3.getY() - p1.getY()) + p3Sq * (p1.getY() - p2.getY());
			cx = num / (2.0f * cp);
			num = p1Sq * (p3.getX() - p2.getX()) + p2Sq
					* (p1.getX() - p3.getX()) + p3Sq * (p2.getX() - p1.getX());
			cy = num / (2.0f * cp);

			center.setX(cx);
			center.setY(cy);
		}

		radius = center.distance(p1);
	}

	private double crossProduct(Point2D p1, Point2D p2, Point2D p3) {
		double u1, v1, u2, v2;

		u1 = p2.getX() - p1.getX();
		v1 = p2.getY() - p1.getY();
		u2 = p3.getX() - p1.getX();
		v2 = p3.getY() - p1.getY();

		return u1 * v2 - v1 * u2;
	}

	protected boolean inside(Point2D point) {
		return center.distance(point) < radius ? true : false;
	}

}
