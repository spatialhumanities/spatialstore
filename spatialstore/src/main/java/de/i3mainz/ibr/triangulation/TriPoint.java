package de.i3mainz.ibr.triangulation;

import de.i3mainz.ibr.geometry.Point;

/**
 *
 * @author s3b31293
 */
public class TriPoint extends Point {

	Point oriPoint;

	protected TriPoint(Point point) {
		this.x = point.getX();
		this.y = point.getY();
		this.z = point.getZ();
		this.oriPoint = point;
	}

	protected TriPoint() {
		super();

	}

	protected TriPoint(double x, double y, double z) {
		super(x, y, z);
	}

	@Override
	public TriPoint mul(double s) {
		super.mul(s);
		return this;
	}
	
	@Override
	public TriPoint add(Point other) {
		super.add(other);
		return this;
	}
	
	public TriPoint add(TriPoint other) {
		super.add(other);
		return this;
	}

	public static TriPoint mul(TriPoint a, double s) {
		return new TriPoint(s * a.x, s * a.y, s * a.z);
	}
	
	void projectTo(TriPlane proPlane) {
		double factor = TriPoint.dot(TriPoint.sub(this, proPlane.center),proPlane.norm)/Point.dot(proPlane.norm, proPlane.norm);
		this.sub(TriPoint.mul(proPlane.norm , factor));
	}

	
}
