package de.i3mainz.ibr.geometry;

import de.i3mainz.ibr.pc.PTGPoint;
import java.io.IOException;
import java.util.ArrayList;

public interface GeoFeature {
	
	public void transform(Transformation t);
	public String toWkt();
	public String wktArg();
	public double visible(double tol, String path) throws IOException;
	public ArrayList<PTGPoint> getPTGPoints (String path, double step)throws IOException;
	public double getsphArea();
	public double getSize();
	public String getUnit();
	public double getPointTol(String path) throws IOException;
}
