package de.i3mainz.ibr.triangulation;


import de.i3mainz.ibr.geometry.Point;

public class Point2D {

	private double x, y;
	private Point r;

	public Point2D() {
	}

	/**
	 * @param x in Raster Koordinaten
	 * @param y in Raster Koordinaten
	 */
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @param x in Raster Koordinaten
	 * @param y in Raster Koordinaten
	 * @param r verweis auf 3D Koordinate
	 */
	public Point2D(double x, double y, Point r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	protected double distance(Point2D other) {

		return Math.sqrt(Math.pow(this.x - other.x, 2)
				+ Math.pow(this.y - other.y, 2));

	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Point getR() {
		return this.r;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}

}
