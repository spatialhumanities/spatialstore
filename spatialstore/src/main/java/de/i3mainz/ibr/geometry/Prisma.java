package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGPoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author Arno Heidelberg
 */
public class Prisma implements GeoFeature {

	//Reihenfolge auch im wkt Format 
	// makiertes Polygon
	private PlanarPolygon backPolygon;
	// verschobenes Polygon
	private PlanarPolygon frontPolygon;
	// Umring Polygon
	private RingPolygon umRing;

	public Prisma(Angle[] polygon, Point[] polyPoints, Point top) throws Exception {
		backPolygon = new PlanarPolygon(polygon, polyPoints);
		frontPolygon = (PlanarPolygon) Util.deepCopy(backPolygon);
		frontPolygon.normMove(top);
		umRing = new RingPolygon(backPolygon, frontPolygon);
	}
	
	public Prisma(Angle[] polygon, Point[] polyPoints, double top) throws Exception {
		backPolygon = new PlanarPolygon(polygon, polyPoints);
		frontPolygon = (PlanarPolygon) Util.deepCopy(backPolygon);
		frontPolygon.normMove(top);
		umRing = new RingPolygon(backPolygon, frontPolygon);
	}
	
	public Prisma(String wkt) {
		String[] wktPolygons = wkt.split(Pattern.quote(",MULTIPOLYGON"));
		String[] coord = new String[3];
		for (int i = 0; i < wktPolygons.length; i++) {
			coord[i] = wktPolygons[i].substring(wktPolygons[i].indexOf("(") + 1, wktPolygons[i].lastIndexOf(")"));
		}
		backPolygon = new PlanarPolygon(coord[0]);
		frontPolygon = new PlanarPolygon(coord[1]);
		umRing = new RingPolygon(coord[2], backPolygon, frontPolygon);

	}

	@Override
	public void transform(Transformation t) {
		backPolygon.transform(t);
		frontPolygon.transform(t);
	}

	/**
	 *
	 * @return Orginal Polygon, Vorschobenes Polygon, umring Polygon
	 */
	@Override
	public String toWkt() {
		return "GEOMETRYCOLLECTION(" + wktArg() + ")";
	}

	@Override
	public String wktArg() {
		String wkt = "";
		wkt += backPolygon.toWkt();
		wkt += ",";
		wkt += frontPolygon.toWkt();
		wkt += ",";
		wkt += umRing.toWkt();
		return wkt;
	}

	//TODO better version 
	@Override
	public double visible(double tol, String path) throws IOException {
		double back = backPolygon.visible(tol, path);
		double front = frontPolygon.visible(tol, path);
		return back > front ? back : front;
	}

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {
		ArrayList<PTGPoint> allPTGPoints = new ArrayList<>();
		allPTGPoints.addAll(backPolygon.getPTGPoints(path,step));
		allPTGPoints.addAll(frontPolygon.getPTGPoints(path,step));
		allPTGPoints.addAll(umRing.getPTGPoints(path,step));
		allPTGPoints = checkPoints(allPTGPoints);
		return allPTGPoints;
	}

	private ArrayList<PTGPoint> checkPoints(ArrayList<PTGPoint> allPTGPoints) {
		double abstand = backPolygon.getBestPlane().distance(frontPolygon.getBestPlane().firstPoint());
		double vorzeichen = 1.0;
		if (abstand < 0) {
			abstand *= -1.0;
			vorzeichen = -1.0;
		}
		ArrayList<PTGPoint> goodPoints = new ArrayList<>();
		for (PTGPoint p : allPTGPoints) {
			if (backPolygon.getBestPlane().distance(p) * vorzeichen <= abstand && backPolygon.getBestPlane().distance(p) * vorzeichen >= 0) {
				boolean inSide = false;
				for (Triangle t : backPolygon.polygon) {
					Point test = new Point(t.projektion(p));
					if (t.inTrieangelCheck(test)) {
						inSide = true;
					}
				}
				if (inSide) {
					goodPoints.add(p);
				}
			}
		}
		return goodPoints;
	}
	
	@Override
	public String toString() {
		return toWkt();
	}

	@Override
	public double getsphArea() {
		double sarea = backPolygon.getsphArea();
		sarea += frontPolygon.getsphArea();
		sarea += umRing.getsphArea();
		return sarea/2;
	}

	@Override
	public double getSize() {
		return Math.abs(backPolygon.getSize() * backPolygon.getBestPlane().distance(frontPolygon.getCenter()));
	}

	@Override
	public String getUnit() {
		return "m³";
	}

	@Override
	public double getPointTol(String path) throws IOException {
		double back = backPolygon.getPointTol(path);
		double front = frontPolygon.getPointTol(path);
		return back > front ? back : front;
	}
}
