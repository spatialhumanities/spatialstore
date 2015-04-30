package de.i3mainz.ibr.triangulation;

import de.i3mainz.ibr.geometry.Triangle;

public class Triangle2D {
	private Point2D a,b,c;

	public Triangle2D(Point2D a, Point2D b, Point2D c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;

	
	}

    public Point2D getA() {
        return a;
    }

    public Point2D getB() {
        return b;
    }

    public Point2D getC() {
        return c;
    }
         
	@Override
	public String toString() {
		return "Dreieck [a=" + a + ", b=" + b + ", c=" + c + "]";
	}
    public Triangle toTriangle(){
        return new Triangle(a.getR(),b.getR(),c.getR());
    }    
        
}
