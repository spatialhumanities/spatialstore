package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.triangulation.Triangulation;

/**
 *
 * @author Arno Heidelberg
 */
public class PlanarPolygon extends Polygon {

	private Point center;
	private Plane planarPlane;

	public PlanarPolygon(Angle[] polygon, Point[] polyPoints) throws Exception {
		super();
		this.polyPoints = new Point[polyPoints.length];
		System.arraycopy(polyPoints, 0, this.polyPoints, 0, polyPoints.length);
		center();
		makePlanar();
		this.polygon = Triangulation.triangulation(this.polyPoints);
	}

	public PlanarPolygon(Angle[] polygon, Point[] polyPoints, Point top) throws Exception {
		super();
		this.polyPoints = new Point[polyPoints.length];
		for (int i = 0; i < polyPoints.length; i++) {
			this.polyPoints[i] = new Point(polyPoints[i]);
		}
		center();
		makePlanar();
		this.normMove(top);
		this.polygon = Triangulation.triangulation(this.polyPoints);
	}

	public PlanarPolygon(String wkt) {
		super(wkt);
		center();
	}

	private void center() {
		this.center = new Point(0, 0, 0);
		for (Point p : polyPoints) {
			this.center.add(p);
		}
		this.center.mul(1.0 / polyPoints.length);

	}
/**
 * Berechnet eine Ausgleichsebene für das gesamte polygon
 * @return 
 */
	public Plane getBestPlane() {
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
		return bestPlane;
	}
/**
 * planarisiert das Polygon
 */
	private void makePlanar() {
		planarPlane = this.getBestPlane();
		for (int i = 0; i < polyPoints.length; i++) {			 
			polyPoints[i] = planarPlane.projektion(polyPoints[i]);
		}
	}
/**
 * Moves the polygon in the direction of the normal vector of the polygon.
 * @param newPlane position of the moved polygon
 */
	public void normMove(Point newPlane) {
		double distance = planarPlane.distance(newPlane);
		normMove(distance);
	}
/**
 * Moves the polygon in the direction of the normal vector of the polygon.
 * @param distance distance to move the polygon
 */	
	
	public void normMove(double distance){
		Point direction = planarPlane.normalNormalized();
		direction = direction.mul(-1.0 * distance);
		for (Point p : polyPoints) {
			p.add(direction);
		}
		center.add(direction);
	}
	@Override
	public void transform(Transformation t) {
		for (Point point : polyPoints) {
			point.transform(t);
		}
	center.transform(t);
	}	
/**
 * 
 * @return center of the polygon
 */
	public Point getCenter() {
		return center;
	}
}
