package de.i3mainz.ibr.geometry;

/**
 * This class describes a 2D-circle
 */
public class Circle {
	
	private Angle center;
	private double radius;
	
	/**
	 * Circle at origin with radius 0
	 */
	public Circle() {
		this.center = new Angle();
		this.radius = 0;
	}
	
	/**
	 * Circle with specified center and radius
	 * @param center
	 * @param radius 
	 */
	public Circle(Angle center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	/**
	 * Circumscribed circle
	 * @param p1
	 * @param p2
	 * @param p3 
	 */
	public Circle(Angle p1, Angle p2, Angle p3) {
		// TODO
	}
	
	/**
	 * Returns true if p is inside this circle
	 * @param p
	 * @return 
	 */
	public boolean isInside(Angle p) {
		return center.distance(p) < radius;
	}
	
}
