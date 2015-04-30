package de.i3mainz.ibr.geometry;

import java.io.Serializable;

public class Plane implements Serializable {

	protected Point a, b, c;

	public Plane(Point a, Point b, Point c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Plane() {
		a = new Point();
		b = new Point();
		c = new Point();
	}

	public Point normal() {
		return Point.cross(Point.sub(a, b), Point.sub(a, c));
	}

	public Point firstPoint() {
		return a;
	}

	public Point normalNormalized() {
		return normal().mul(1 / normal().norm());
	}

	//dist > 0 Punkt vor Dreieck dist < 0 hinter dreieck
	public double distance(Point p) {
		double ori = Point.dot(a, normalNormalized()) < 0 ? 1 : -1;
		return ori * Point.dot(Point.sub(p, a), normalNormalized());
	}

	//Punkt innerhalb eines Dreiecks
	public boolean inTrieangelCheck(Point x) {
		if ((Point.dot(Point.cross(Point.sub(b, a), Point.sub(x, a)), this.normal())) > 0) {
			if (Point.dot(Point.cross(Point.sub(c, b), Point.sub(x, b)), this.normal()) > 0) {
				if (Point.dot(Point.cross(Point.sub(a, c), Point.sub(x, c)), this.normal()) > 0) {
					return true;
				}
			}
		}
		return false;
	}

	//Projektion eines punktes auf die Ebene
	public Point projektion(Point x) {
		Point tmp = new Point(x);
		double fac = Point.dot(tmp.sub(a), this.normal()) / Point.dot(this.normal(), this.normal());
		return Point.sub(x, Point.mul(this.normal(), fac));
	}
}
