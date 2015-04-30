package de.i3mainz.ibr.triangulation;

import de.i3mainz.ibr.geometry.Plane;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Transformation;

/**
 *
 * @author s3b31293
 */
class TriPlane {

	TriPoint center, norm;

	protected TriPlane(TriPoint schwerpunkt, TriPoint norm) {
		this.center = schwerpunkt;
		this.norm = norm;
	}

	protected TriPlane(Point[] polyPoints) {
		center(polyPoints);
		getBestPlane(polyPoints);
//		norm = new TriPoint(center);
//		norm = norm.mul(-1.0).mul(1.0/norm.norm());

	}
	
	private void center(Point[] points) {
		center = new TriPoint(0, 0, 0);
		for (Point p : points) {
			center.add(p);
		}
		this.center.mul(1.0 / points.length);
	}

	protected Transformation getTransomation() {
		Point thirdVec = Point.cross(center, new Point(center.getY(),-center.getX(),0));
		Transformation t = new Transformation(thirdVec.getX(),center.getY(),center.getX(),0,thirdVec.getY(),-center.getX(),center.getY(),0,thirdVec.getZ(),0,center.getZ(),0,0,0,0,1);
		return t.inverse();
							
	}
	
	private void getBestPlane(Point[] polyPoints) {
		Plane bestPlane = new Plane();
		double bestDist = Double.MAX_VALUE;
		for (int i = 0; i < polyPoints.length - 1; i++) {
			for (int k = i + 1; k < polyPoints.length; k++) {
				Plane testPlane = new Plane(center, polyPoints[i], polyPoints[k]);
				double testDist = 0;
				for (Point p : polyPoints) {
					double tmp = testPlane.distance(p);
					testDist += tmp * tmp;
				}
				if (testDist < bestDist) {
					bestPlane = testPlane;
					bestDist = testDist;
				}
			}
		}
		norm = new TriPoint(bestPlane.normalNormalized().getX(),bestPlane.normalNormalized().getY(),bestPlane.normalNormalized().getZ());
	}

}
