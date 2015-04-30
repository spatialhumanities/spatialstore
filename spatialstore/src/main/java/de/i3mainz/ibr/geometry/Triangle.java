package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGInteraction;
import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.pc.Visibility;
import java.io.IOException;
import java.util.ArrayList;

public class Triangle extends Plane implements GeoFeature {


    public Triangle(Point a, Point b, Point c) {
        super(a,b,c);
    }

    @Override
    public void transform(Transformation t) {
        a.transform(t);
        b.transform(t);
        c.transform(t);
    }

    @Override
    public String toWkt() {
        return "POLYGON Z " + wktArg();
    }
	
	@Override
	public String wktArg() {
		return "((" + a.wktArg() + "," + b.wktArg() + "," + c.wktArg() + "," + a.wktArg() + "))";
	}
	
    @Override
    public String toString() {
        return toWkt();
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }

    public Point getC() {
        return c;
    }

    public double area(){
            return 0.5*(Point.cross(Point.sub(b, a),Point.sub(c, a))).norm();
            }
    /**
     *
     * @param tol Tolleranz
     * @param path PTG File Pfad
     * @return 1 = 100% visible, 0= not visible
	 * @throws java.io.IOException
     */
    @Override
    public double visible(double tol, String path) throws IOException {
        //TODO muss aus Erfahrungswerten angepasst werden
        double step = this.area()*2; 
        step = step <=1 ? 1:step;
		return Visibility.visible(this, tol, path, step);
    }

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {
		return Visibility.getPoints(this.a, this.b, this.c, path, step);
	}

	@Override
	public double getsphArea() {
		Triangle sTri = new Triangle(new Point(a.getAzim(),a.getElev(),0),new Point(b.getAzim(),b.getElev(),0),new Point(c.getAzim(),c.getElev(),0));
		return sTri.area();
	}

	@Override
	public double getSize() {
		return this.area();
	}

	@Override
	public String getUnit() {
		return "m²";
	}

	@Override
	public double getPointTol(String path) throws IOException {
		ArrayList<PTGPoint> ptgPoints = this.getPTGPoints(path, 1);
		double[] distances = new double[ptgPoints.size()];
		for(int i =0; i<ptgPoints.size();i++){
			distances[i]=this.distance(ptgPoints.get(i));
		}
		
		return PTGInteraction.getPercentElement(distances, 0.96);
	}
}
