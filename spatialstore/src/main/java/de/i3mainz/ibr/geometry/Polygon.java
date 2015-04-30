package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGInteraction;
import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.pc.Visibility;
import java.util.ArrayList;
import de.i3mainz.ibr.triangulation.Triangulation;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

public class Polygon implements GeoFeature, Serializable {

	protected ArrayList<Triangle> polygon;
	protected Point[] polyPoints;

	protected Polygon() {

	}

	public Polygon(Angle[] polygon, Point[] polyPoints) throws Exception {
		this.polyPoints = polyPoints;
		this.polygon = Triangulation.triangulation(polyPoints);
	}

	public Polygon(String wkt) {
		String[] poly = wkt.split(Pattern.quote(")),(("));
		this.polygon = new ArrayList<>();
		ArrayList<Point> allPoints = new ArrayList<>();
		for (String s : poly) {
			ArrayList<Point> punkte = new ArrayList<>();
			String[] points = s.substring(s.lastIndexOf("(") + 1, s.indexOf(")") > 0 ? s.indexOf(")") : s.length()).split(",");
			for (String bs : points) {
				Point p = new Point(bs);
				if (!allPoints.contains(p)) {
					allPoints.add(p);
				} else {
					p = allPoints.get(allPoints.lastIndexOf(p));
				}
				punkte.add(p);
			}
			polygon.add(new Triangle(punkte.get(0), punkte.get(1), punkte.get(2)));
		}
		Point[] re = new Point[allPoints.size()];
		this.polyPoints = allPoints.toArray(re);
	}

	public ArrayList<Triangle> getPolygon() {
		return polygon;
	}

	public Point[] getPoints() {
		return polyPoints;
	}

	@Override
	public void transform(Transformation t) {
		for (Point point : polyPoints) {
			point.transform(t);
		}
	}

	@Override
	public String toWkt() {
		return "MULTIPOLYGON Z" + wktArg();
	}

	@Override
	public String wktArg() {
		if (polygon.isEmpty()) {
			return "";
		}
		String wkt = "";
		for (Triangle triangle : polygon) {
			wkt += "," + triangle.wktArg();
		}
		return "(" + wkt.substring(1) + ")";
	}

	public double area() {
		double area = 0;
		for (Triangle t : polygon) {
			area += t.area();
		}
		return area;
	}

	@Override
	public String toString() {
		return toWkt();
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
		double step = this.area();
		step = step <= 0 ? 1 : step;
		return Visibility.visible(this, tol, path, step);
	}

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {
		ArrayList<PTGPoint> points = new ArrayList<>();
		for (Triangle t : this.polygon) {
			points.addAll(t.getPTGPoints(path,step));
		}
		return points;
	}

	@Override
	public double getsphArea() {
		double sarea =0;
		for(Triangle tri:polygon){
			sarea += tri.getsphArea();
		}
		return sarea;
	}

	@Override
	public double getSize() {
		return area();
	}

	@Override
	public String getUnit() {
		return "m²";
	}

	@Override
	public double getPointTol(String path) throws IOException {
		double[] distances = new double[polygon.size()];
		for(int i = 0; i< polygon.size();i++){
			distances[i]=polygon.get(i).getPointTol(path);
		}
		return PTGInteraction.getPercentElement(distances, 1);
	}

}
