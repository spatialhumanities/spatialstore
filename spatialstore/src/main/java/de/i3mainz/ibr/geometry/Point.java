package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.pc.Visibility;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Point implements GeoFeature, Serializable {

	protected double x, y, z;

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point() {
	}

	public Point(String wkt) {
		String[] xyz = wkt.split(" ");
		this.x = Double.parseDouble(xyz[0]);
		this.y = Double.parseDouble(xyz[1]);
		this.z = Double.parseDouble(xyz[2]);
	}

	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public Point(double azim, double elev) {
		this.x = Math.sin(elev) * Math.cos(azim);
		this.y = Math.sin(elev) * Math.sin(azim);
		this.z = Math.cos(elev);
	}

	public static Point add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public Point add(Point other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public static Point sub(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public Point sub(Point other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}

	public static double dot(Point a, Point b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static Point mul(Point a, double s) {
		return new Point(s * a.x, s * a.y, s * a.z);
	}

	public Point mul(double s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}

	public static Point cross(Point u, Point v) {
		double x = u.y * v.z - u.z * v.y;
		double y = u.z * v.x - u.x * v.z;
		double z = u.x * v.y - u.y * v.x;
		return new Point(x, y, z);
	}

	public static Point cut(Line line, Plane plane) {
		Point normal = plane.normal();
		double s = dot(normal, line.getA()) / dot(normal, sub(line.getA(), line.getB())) - dot(normal, plane.firstPoint());
		return add(line.getA(), sub(line.getB(), line.getA()).mul(s));
	}
	
	public static Point cutnew(Line line, Plane plane) {
		Point normal = plane.normal();
		Point c = sub(line.getB(),line.getA());
		return add(line.getA(),mul(c, dot(normal, sub(plane.a, line.getA()))/dot(normal, c)));
	}
	
	public double dist(Point other) {
		return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2) + Math.pow(this.z - other.z, 2));
	}

	public double getAzim() {
		return Math.atan2(y, x);
	}

	public double getElev() {
		return Math.acos(z / norm());
	}

	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public void transform(Transformation t) {
		double x = this.x;
		double y = this.y;
		double z = this.z;
		double w = t.getW(x, y, z);
		this.x = t.getX(x, y, z) / w;
		this.y = t.getY(x, y, z) / w;
		this.z = t.getZ(x, y, z) / w;
	}

	@Override
	public String toWkt() {
		return "POINT Z (" + wktArg() + ")";
	}

	@Override
	public String toString() {
		return toWkt();
	}

	public String wktArg() {
		return x + " " + y + " " + z;
	}

	/**
	 *
	 * @param tol Tolleranz
	 * @param path PTG File Pfad
	 * @return 1 = 100% visible, 0= not visible
	 */
	@Override
	public double visible(double tol, String path) throws IOException {
		return Visibility.visible(this, tol, path);
	}

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {
		return Visibility.getPoint(this, path);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o == this) {
			return true;
		}

		if (!o.getClass().equals(getClass())) {
			return false;
		}

		Point that = (Point) o;
		//TODO Rundungs fehler
		return this.x == that.x && this.y == that.y && this.x == that.x;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 23 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}

	@Override
	public double getsphArea() {
		return 0;
	}

	@Override
	public double getSize() {
		return 0;
	}

	@Override
	public String getUnit() {
		return "";
	}

	@Override
	public double getPointTol(String path) {
		return 0;
	}
}
