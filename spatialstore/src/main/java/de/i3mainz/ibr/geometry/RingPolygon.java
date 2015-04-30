package de.i3mainz.ibr.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * @author s3b31293
 */
public class RingPolygon extends Polygon {

	protected RingPolygon() {

	}

	protected RingPolygon(String wkt, PlanarPolygon backPolygon, PlanarPolygon frontPolygon) {

		String[] poly = wkt.split(Pattern.quote(")),(("));
		this.polygon = new ArrayList<>();
		ArrayList<Point> allPoints = new ArrayList<>();
		allPoints.addAll(Arrays.asList(backPolygon.polyPoints));
		allPoints.addAll(Arrays.asList(frontPolygon.polyPoints));
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

	protected RingPolygon(String wkt) {
		super(wkt);
	}

	protected RingPolygon(PlanarPolygon back, PlanarPolygon front) {
		polyPoints = new Point[back.polyPoints.length * 2];
		System.arraycopy(back.polyPoints, 0, polyPoints, 0, back.polyPoints.length);
		System.arraycopy(front.polyPoints, 0, polyPoints, back.polyPoints.length, front.polyPoints.length);
		polygon = new ArrayList<>();
		triangulation(back, front);
	}

	private void triangulation(PlanarPolygon back, PlanarPolygon front) {
		for (int i = 0; i < back.polyPoints.length; i++) {
			polygon.add(new Triangle(front.polyPoints[i], front.polyPoints[indexCheck(i + 1, back.polyPoints.length, 0)], back.polyPoints[i]));
			polygon.add(new Triangle(back.polyPoints[indexCheck(i - 1, back.polyPoints.length, 0)], front.polyPoints[i], back.polyPoints[i]));
		}
	}

	private int indexCheck(int index, int max, int min) {
		if (index < min) {
			return index + max;
		} else if (index >= max) {
			return max - index;
		}
		return index;
	}

	//TODO Implementieren
	@Override
	public double visible(double tol, String path) throws IOException {
		return 0;
	}

}
