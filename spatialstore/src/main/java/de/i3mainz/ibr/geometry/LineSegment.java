package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGInteraction;
import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.pc.Visibility;
import java.io.IOException;
import java.util.ArrayList;

public class LineSegment implements GeoFeature {

    private Point start;
    private Point end;

    public LineSegment(Point start, Point end) {
        this.start = start;
        this.end = end;
    }
	
	public double lineLenght(){
		return Math.sqrt(Math.pow(start.x-end.x, 2)+Math.pow(start.y-end.y,2)+Math.pow(start.z-end.z,2));
	}
	
    @Override
    public void transform(Transformation t) {
		start.transform(t);
		end.transform(t);
    }

    @Override
    public String toWkt() {
        return "LINESTRING(" + wktArg() + ")";
    }

    @Override
    public String wktArg() {
        return start.wktArg() + "," + end.wktArg();
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
     */
    @Override
    public double visible(double tol, String path) throws IOException {
        return Visibility.visible(this, tol, path);
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public double distance(Point other) {
        return (Point.cross(Point.sub(other, start), Point.sub(end, start)).norm()) / (Point.sub(end, start).norm());
    }

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {

		return Visibility.getPoints(this, path, 1);

	}

	@Override
	public double getsphArea() {
		return 0;
	}

	@Override
	public double getSize() {
		return this.lineLenght();
	}

	@Override
	public String getUnit() {
		return "m";
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
