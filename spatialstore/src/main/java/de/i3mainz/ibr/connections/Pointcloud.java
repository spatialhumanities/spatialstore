package de.i3mainz.ibr.connections;

import de.i3mainz.ibr.geometry.Angle;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Transformation;
import de.i3mainz.ibr.pc.PTGInputStream;
import de.i3mainz.ibr.pc.PTGPoint;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class Pointcloud implements Closeable {

	private final PTGInputStream stream;
	private Transformation trans;
	private Transformation inverse;

	public Pointcloud(String filename, Transformation transformation) throws IOException {
		trans = transformation;
		inverse = transformation.inverse();
		stream = new PTGInputStream(filename);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	public PTGPoint getPoint(Angle angle) throws IOException {
		Point p = new Point(angle.getAzim(), angle.getElev());
		p.transform(inverse);
		PTGPoint ptgp = stream.readPoint(p.getAzim(), p.getElev());
		if (ptgp == null) {
			ArrayList<PTGPoint> points = nextPoints(p, 1);
			ptgp =getBestPoint(points, p);
		}
		if (ptgp != null) {
			ptgp.transform(trans);
		}
		return ptgp;
	}

	public PTGPoint[] getPoints(Angle[] angles) throws IOException {
		PTGPoint[] points = new PTGPoint[angles.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = getPoint(angles[i]);
		}
		return points;
	}

	private ArrayList<PTGPoint> nextPoints(Point point, int anzahl) throws IOException {
		ArrayList<PTGPoint> punkte = new ArrayList<>();
		for (double r = Math.PI / stream.getSize(); r < Math.PI; r *= 2.0) {
			for (double x = -r; x <= r; x += Math.PI / stream.getSize()) {
				double xTotal = x + point.getAzim();
				if (xTotal > Math.PI) {
					xTotal = xTotal - 2 * Math.PI;
				}
				if (xTotal < -Math.PI) {
					xTotal = xTotal + 2 * Math.PI;
				}
				for (double y = -r; y <= r; y += Math.PI / stream.getSize()) {

					double yTotal = y + point.getElev();
					if (yTotal > Math.PI) {
						yTotal = 2 * Math.PI - yTotal;
						xTotal = xTotal + Math.PI;
					}
					if (yTotal < 0) {
						yTotal = -yTotal;
						xTotal = xTotal + Math.PI;
					}
					PTGPoint ptgp = stream.readPoint(xTotal, yTotal);
					if (ptgp != null) {
						//if (Math.sqrt(Math.pow(xTotal - point.getAzim(), 2) + Math.pow(yTotal - point.getElev(), 2)) < r) {
							punkte.add(ptgp);
						//}
					}

				}
			}
			if (punkte.size() >= anzahl) {
				return punkte;
			}
		}
		return null;
	}

	private PTGPoint getBestPoint(ArrayList<PTGPoint> points, Point point) {
		double bestDist = Double.MAX_VALUE;
		PTGPoint bestPoint = points.get(0);
		for (PTGPoint p : points) {
			if (sphericalDistance(point,p) < bestDist) {
				bestDist = sphericalDistance(point,p);
				bestPoint = p;
			}
		}

		return bestPoint;
	}
	
	private double sphericalDistance(Point a, Point b) {
		double azimA = a.getAzim();
		double elevA = a.getElev();
		double azimB = b.getAzim();
		double elevB = b.getElev();
		double azim = (Math.abs(azimB-azimA) > 2*Math.PI) ? Math.abs(azimB-azimA)-2*Math.PI : Math.abs(azimB-azimA);
		double elev = Math.abs(elevB-elevA);
		return Math.sqrt(azim*azim+elev*elev);
	}
	
}
