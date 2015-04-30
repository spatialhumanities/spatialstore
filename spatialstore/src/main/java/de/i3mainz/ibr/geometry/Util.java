package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.connections.ClientException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Util {

	public static double check(double a, double b) {
		if ((b - a) > Math.PI) {
			return a + 2 * Math.PI;
		}
		return a;

	}

	public static boolean isJump(double a, double b) {
		return Math.abs(a - b) > Math.PI;
	}

	public static double check(double a) {
		if (a < -Math.PI) {
			return a + 2 * Math.PI;
		}
		if (a > Math.PI) {
			return a - 2 * Math.PI;
		}
		return a;

	}

	public static GeoFeature pointsToFeature(Point[] points, Angle[] angles, String type, boolean isPlanar, double prismPoint) throws ClientException, Exception {
		if (type.equals("POINT")) {
			if (points.length != 1) {
				throw new ClientException("wrong number of points in " + type + ": " + points.length, 400);
			}
			return points[0];
		}
		if (type.equals("LINESTRING")) {
			if (points.length < 2) {
				throw new ClientException("wrong number of points in " + type + ": " + points.length, 400);
			}
			return new LineString(points);
		}
		if (type.equals("POLYGON")) {
			if (points.length < 3) {
				throw new ClientException("wrong number of points in " + type + ": " + points.length, 400);
			}
			return new Polygon(angles, points);
		}
		if (type.equals("PRISM")){
			return new Prisma(angles ,points ,prismPoint);
		}
		throw new ClientException("wrong type: " + type, 400);
	}

	public static GeoFeature wktToFeature(String wkt) {
		String type = wkt.substring(0, wkt.indexOf("("));
		String coord = wkt.substring(wkt.indexOf("(") + 1, wkt.lastIndexOf(")"));
		if (type.equals("POINT")) {
			return new Point(coord);
		} else if (type.equals("LINESTRING")) {
			return new LineString(coord);
		} else if (type.equals("MULTIPOLYGON")) {
			return new Polygon(coord);
		} else if (type.equals("GEOMETRYCOLLECTION")) {
			return new Prisma(coord);
		}
		return null;
	}

	/**
	 * All Objects to be copied must implement Serializable interface
	 * @param original any Objcet to be copied
	 * @return a deep copy of the input Object
	 * @throws Exception
	 */
	public static Object deepCopy(Object original) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(original);

		ByteArrayInputStream bais
				= new ByteArrayInputStream(baos.toByteArray());

		return new ObjectInputStream(bais).readObject();
	}

}
