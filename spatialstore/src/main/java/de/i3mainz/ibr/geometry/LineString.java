package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGInteraction;
import de.i3mainz.ibr.pc.PTGPoint;
import de.i3mainz.ibr.pc.Visibility;
import java.io.IOException;
import java.util.ArrayList;

public class LineString implements GeoFeature {

	ArrayList<LineSegment> lines;
	
	public LineString(Point[] points){
		this.constuctLines(points);
	}
	
	public LineString(String wkt){
			String[] wktPoints = wkt.split(",");
			Point[] linePoints = new Point[wktPoints.length];
			for (int i = 0; i < wktPoints.length; i++) {
				linePoints[i] = new Point(wktPoints[i]);
			}
			
			this.constuctLines(linePoints);
	}
	
	private void constuctLines(Point[] linePoints){
		this.lines = new ArrayList<>();
		for(int i =1 ; i<linePoints.length;i++){
			this.lines.add(new LineSegment(new Point(linePoints[i-1]),new Point(linePoints[i])));
		}
	}
	
	
	@Override
	public void transform(Transformation t) {
		for (LineSegment line: lines) {
			line.transform(t);
		}
	}

	@Override
	public String toWkt() {
		return "LINESTRING(" + wktArg() + ")";
	}

	@Override
	public String wktArg() {
		String wkt = "";
		for (int i = 0; i<lines.size();i++){
			if(i==0){
				wkt = lines.get(i).getStart().wktArg() + "," + lines.get(i).getEnd().wktArg();
			}
			else{
				wkt = wkt + "," + lines.get(i).getEnd().wktArg();
			}
		}
		return wkt;
	}

	@Override
	public double visible(double tol, String path) throws IOException {
		double step = this.getLength();
		return Visibility.visible(this, tol, path, step);
	}

	@Override
	public ArrayList<PTGPoint> getPTGPoints(String path, double step) throws IOException {
		ArrayList<PTGPoint> points = new ArrayList<>();
		for (LineSegment line: lines) {
			points.addAll(line.getPTGPoints(path,step));
		}
		return points;
	}

	public ArrayList<LineSegment> getLines() {
		return lines;
	}
	
	private double getLength(){
		double length=0;
		for (LineSegment line: lines){
			length += line.lineLenght();
		}
		return length;
	}

	@Override
	public double getsphArea() {
		return 0;
	}

	@Override
	public double getSize() {
		return this.getLength();
	}

	@Override
	public String getUnit() {
		return "m";
	}

	@Override
	public double getPointTol(String path) throws IOException {
		double[] distances = new double[lines.size()];
		for(int i = 0; i< lines.size();i++){
			distances[i]=lines.get(i).getPointTol(path);
		}
		return PTGInteraction.getPercentElement(distances, 1);
	}
}
