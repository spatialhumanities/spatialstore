package de.i3mainz.ibr.pc;

import de.i3mainz.ibr.geometry.LineSegment;
import de.i3mainz.ibr.geometry.LineString;
import de.i3mainz.ibr.geometry.Plane;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Polygon;
import de.i3mainz.ibr.geometry.Triangle;
import de.i3mainz.ibr.geometry.Util;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author s3b31293
 */
public class Visibility {

    /**
     *
     * @param polygons
     * @param tol Tolleranz
     * @param ptgFile Adress of the PTG File
	 * @param step
     * @return 1=100% visible 0 inviseble
	 * @throws java.io.IOException
     */
    public static double visible(Polygon polygons, double tol, String ptgFile, double step) throws IOException {
        int countAll = 0;
        int countBehinde = 0;

        for (Triangle t : polygons.getPolygon()) {
            ArrayList<PTGPoint> points = getPoints(t.getA(), t.getB(), t.getC(), ptgFile, step);
            countAll += points.size();
            countBehinde += vis(points, t.getA(), t.getB(), t.getC(), tol);
        }

        return 1.0 * countBehinde / countAll;
    }
	
	public static double visible(LineString lines, double tol, String ptgFile, double step) throws IOException{
		int countAll = 0;
        int countBehinde = 0;

		for(LineSegment line : lines.getLines()){
			ArrayList<PTGPoint> points = getPoints(line,ptgFile, step);
			countAll+=points.size();
			countBehinde +=vis(points,line,tol);
		}
		System.out.println(countAll+"   "+step+"  "+ptgFile);
		return 1.0 * countBehinde / countAll;
	}
	
	
    /**
     *
     * @param triangle
     * @param tol Tolleranz
     * @param ptgFile Adress of the PTG File
	 * @param step
     * @return
	 * @throws java.io.IOException
     */
    public static double visible(Triangle triangle, double tol, String ptgFile,double step) throws IOException {
        ArrayList<PTGPoint> points = getPoints(triangle.getA(), triangle.getB(), triangle.getC(), ptgFile, step);
        int countAll = points.size();
        int countBehinde = vis(points, triangle.getA(), triangle.getB(), triangle.getC(), tol);
        return 1.0 * countBehinde / countAll;
    }

    /**
     *
     * @param line
     * @param tol
     * @param ptgFile
     * @return
     * @throws IOException
     */
    public static double visible(LineSegment line, double tol, String ptgFile) throws IOException {
        ArrayList<PTGPoint> points = getPoints(line, ptgFile, 1);
        int countAll = points.size();
        int countBehinde = vis(points, line, tol);
        return 1.0 * countBehinde / countAll;
    }

    /**
     *
     * @param point
     * @param tol
     * @param ptgFile
     * @return -1 Point does not exist in PTG
     * @throws IOException
     */
    public static double visible(Point point, double tol, String ptgFile) throws IOException {
        
		ArrayList<PTGPoint> other = getPoint(point, ptgFile);
        int count =0;
		int vis =0;
		for (PTGPoint p : other){
			if (p != null){
				count++;
				if ((point.norm() - p.norm()) < tol){
					vis++;
				}		
			}
		}
		if (count ==0) return 0;
		return 1.0*vis/count;
	}

    public static double visible(ArrayList<Point> points, double tol, String ptgFile) throws IOException {
        ArrayList<PTGPoint> other = getPoints(points, ptgFile);
        if (other == null) {
            return -1;
        }
        int countAll = points.size();
        int countBehinde = 0;
        for (int i = 0; i < points.size(); i++) {
            if (other.get(i) != null) {
                if ((points.get(i).norm() - other.get(i).norm()) < tol) {
                    countBehinde++;
                }
            }
        }
        return 1.0 * countBehinde / countAll;
    }

    public static ArrayList<PTGPoint> getPoint(Point point, String ptgFile) throws IOException {

		ArrayList<PTGPoint> points;
		try (PTGInputStream in = new PTGInputStream(ptgFile)) {
			points = new ArrayList<>();
			int col = (int)((1-point.getAzim()/Math.PI)*in.getSize());
			int row = (int)((point.getElev()/Math.PI)*in.getSize());
			for(int i = col-1;i<=col+1;i++){
				for(int k=row-1;k<=row+1;k++){
					points.add(in.readPoint(i, k));
				}
			}
		}
        return points;
    }

    public static ArrayList<PTGPoint> getPoints(ArrayList<Point> points, String ptgFile) throws IOException {
		ArrayList<PTGPoint> res;
		try (PTGInputStream in = new PTGInputStream(ptgFile)) {
			res = new ArrayList<>();
			for (Point p : points) {
				res.add(in.readPoint(p.getAzim(), p.getElev()));
			}
		}
        return res;
    }

    private static int vis(ArrayList<PTGPoint> points, Point a, Point b, Point c, double tol) {
        int countBehinde = 0;
        Plane polygon = new Plane(a, b, c);

        for (PTGPoint p : points) {

            Point checkpoint = new Point(p.getX(), p.getY(), p.getZ());
            double distance = polygon.distance(checkpoint);
            if (distance < tol) {
                countBehinde++;
            }
        }

        return countBehinde;
    }

    public static ArrayList<PTGPoint> getPoints(LineSegment line, String ptgFile, double step) throws IOException {
		ArrayList<PTGPoint> points;
		try (PTGInputStream in = new PTGInputStream(ptgFile)) {
			points = in.getAllPointsOnLine(line.getStart().getAzim(), line.getStart().getElev(), line.getEnd().getAzim(), line.getEnd().getElev(), step);
		}
        return points;
    }

    public static ArrayList<PTGPoint> getPoints(Point a, Point b, Point c, String ptgFile,double step) throws IOException {

//      Einlesen der Punkte Wolke aus den Vectoren a,b,c 
//      getAllPoints.. erwartet Winkelangabe        
        double azimA = a.getAzim();
        double elevA = a.getElev();
        double azimB = b.getAzim();
        double elevB = b.getElev();
        double azimC = c.getAzim();
        double elevC = c.getElev();

//      Abfangen des Bild Randes
        azimA = Util.check(azimA, azimB);
        azimA = Util.check(azimA, azimC);
        azimB = Util.check(azimB, azimA);
        azimB = Util.check(azimB, azimC);
        azimC = Util.check(azimC, azimA);
        azimC = Util.check(azimC, azimB);

		ArrayList<PTGPoint> points;
		try (PTGInputStream in = new PTGInputStream(ptgFile)) {
			points = in.getAllPointsInsideTriangle(azimA, elevA, azimB, elevB, azimC, elevC, step);
		}
        return points;
    }

    private static int vis(ArrayList<PTGPoint> points, LineSegment line, double tol) {
        Plane polygon = new Plane(line.getStart(), line.getEnd(), new Point(line.getStart().getX(), line.getStart().getY(), line.getStart().getZ() + 1));
        int countBehinde = 0;
        for (Point p : points) {
            double distance = polygon.distance(p);
            if (distance < tol) {
                countBehinde++;
            }
        }
        return countBehinde;
    }

}
