package de.i3mainz.ibr.triangulation;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Transformation;
import de.i3mainz.ibr.geometry.Triangle;
import java.util.ArrayList;

/**
 *
 * @author s3b31293
 */
public class Triangulation {

	public static ArrayList<Triangle> triangulation(Point[] polypoints) throws Exception {
		TriPoint[] points = new TriPoint[polypoints.length];
		for (int i = 0; i<polypoints.length;i++) {
			points[i]=new TriPoint(polypoints[i]);
		}
		TriPlane projectionPlane = new TriPlane(polypoints);
		Transformation t = projectionPlane.getTransomation();
		for (TriPoint p : points) {
			p.projectTo(projectionPlane);				// Auf Ebene Porjezieren
			p.transform(t);								// Transformieren das Punkt x,y-Ebene liegt
		}
        ArrayList<Point2D> polygon2D = new ArrayList<>();
        for (int i = 0; i < polypoints.length; i++) {
            polygon2D.add(new Point2D(points[i].getX(), points[i].getY(), polypoints[i]));
        }
		ArrayList<Triangle2D> triangulation = delaunyTrio(polygon2D);
		
		ArrayList<Triangle> finTri = new ArrayList<>();
        for(Triangle2D tri: triangulation){
            finTri.add(tri.toTriangle());
        }
		
		return finTri;
	}

    /*
     * Delauny Triangolierung kombiniert mit Earclipping
     */
    /**
     * @param polygon Zu Triangolierendes Polygon
     * @return Triangulierung als Liste von Dreiecken
     */
    public static ArrayList<Triangle2D> delaunyTrio(ArrayList<Point2D> polygon) throws Exception {
        //validierung
		if(!firstValidation(polygon)){
			throw new ClientException("Fehler: Sie haben einen Punkt mehrfach geklickt!",400);	
		}
		if(!secondValidation(polygon)){
			throw new ClientException("Fehler: Ihr Polygon ist topologisch inkonsistent!",400);
		}

		//TODO in weitere Methode aufteilen
		
		int maxRuns = maxruns(polygon.size());

        polygon = makeClockwise(polygon);
        boolean pointFree;
        Circle2D c = new Circle2D();
        ArrayList<Triangle2D> drei = new ArrayList<>();

		int counter =0;
        while (polygon.size() >= 3) {
            if (polygon.size() == 3) {
                drei = addTriangle(0, 1, 2, drei, polygon);
                break;
            }
			if(counter > maxRuns){
				Exception e =new Exception("Triangolierung nicht möglich!");	
				e.printStackTrace();
				throw e;
			}
            for (int i = 0; i < polygon.size(); i++) {
                c.circumCircle(polygon.get(fixIndex(i - 1, polygon.size())),
                        polygon.get(i),
                        polygon.get(fixIndex(i + 1, polygon.size())));
                pointFree = true;
                for (int k = 0; k < polygon.size(); k++) {
                    if (k != i - 1 && k != i && k != i + 1) {
                        if (c.inside(polygon.get(k))) {
                            pointFree = false;
                            break;
                        }
                    }
                }
                if (pointFree) {
                    if (Math.PI < convex(
                            polygon.get(fixIndex(i - 1, polygon.size())),
                            polygon.get(i),
                            polygon.get(fixIndex(i + 1, polygon.size())))) {
                        addTriangle(fixIndex(i - 1, polygon.size()), i,
                                fixIndex(i + 1, polygon.size()), drei, polygon);
                        polygon.remove(i);
                        break;
                    }
                }
            }
			counter++;
		}
        return drei;
    }

    /*
     * fuegt neue Dreiecke zur Tirangulierung hinzu
     */
    private static ArrayList<Triangle2D> addTriangle(int i, int j, int k,
            ArrayList<Triangle2D> drei, ArrayList<Point2D> polygon) {

        Triangle2D newTrieangle = new Triangle2D(polygon.get(i), polygon.get(j),
                polygon.get(k));
        drei.add(newTrieangle);
        return drei;
    }

    /*
     * Dreht die orientierung des Polygons wenn es nicht im uhrzeigersinn
     * eingegeben wurde
     */
    private static ArrayList<Point2D> makeClockwise(ArrayList<Point2D> polygon) {
        if (!isPolygonClockwise(polygon)) {
            ArrayList<Point2D> clockwise = new ArrayList<Point2D>();
            for (int i = polygon.size() - 1; i >= 0; i--) {
                clockwise.add(polygon.get(i));
            }
            polygon = clockwise;
        }
        return polygon;
    }

    private static int fixIndex(int index, int max) {
        if (index > max - 1) {
            return index - max;
        } else if (index < 0) {
            return max + index;
        } else {
            return index;
        }
    }

    /*
     * Prueft ob ein polygon im Uhrzeigersinn eingegeben wurde Innen Winkel
     * (n-2)*PI wegen Rundungsfehlern (n-1)*PI
     */
    private static boolean isPolygonClockwise(ArrayList<Point2D> polygon) {

        double tol = (polygon.size() - 1) * Math.PI;
        double innenSum = 0;
        for (int i = 0; i < polygon.size(); i++) {

            innenSum += convex(
                    polygon.get(i <= 0 ? polygon.size() - 1 : i - 1),
                    polygon.get(i),
                    polygon.get(i >= polygon.size() - 1 ? 0 : i + 1));
        }

        return innenSum > tol;
    }

    /*
     * Bestimmt den Winkel zwischen zwei Punkten ueber einen zwischen Punkt
     */
    private static double convex(Point2D a, Point2D b, Point2D c) {
        double x = Math.atan2(b.getX() - a.getX(), b.getY() - a.getY());
        double y = 2.0 * Math.PI
                - Math.atan2(b.getX() - c.getX(), b.getY() - c.getY());
        return (x + y) % (2 * Math.PI);
    }

	private static int maxruns(int n){
		int result = 0;
		for(int i=1; i<=n;i++){
			result +=i;
		}	
	return result;		
	}
	/**
	 * 
	 * @param polygon
	 * @return false if polygon has the same point more the once
	 */
	private static boolean firstValidation(ArrayList<Point2D> polygon){
		for (int i =0; i<polygon.size();i++){
			for (int k = i+1; k<polygon.size();k++){
				if (polygon.get(i).getX()==polygon.get(k).getX()&&polygon.get(i).getY()==polygon.get(k).getY()){
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean secondValidation(ArrayList<Point2D> polygon){
		for (int i=0;i<polygon.size();i++){
			Line2D lineOne = new Line2D(polygon.get(i),polygon.get((i+1)%polygon.size()));
			for(int k = i+2;k<polygon.size()+i-1;k++){
				Line2D lineTow = new Line2D(polygon.get(k%polygon.size()),polygon.get((k+1)%polygon.size()));
				if(lineOne.isCut(lineTow)){
					return false;
				}
			}
		}
		return true;
	}
	

}
