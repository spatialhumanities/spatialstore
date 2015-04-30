package de.i3mainz.ibr.pc;

import java.io.IOException;
import java.util.Properties;

public class PTGFile {
	
	private static String pointcloudpath;
	static {
        try {
            Properties config = new Properties();
			config.load(PTGFile.class.getClassLoader().getResourceAsStream("config.properties"));
			pointcloudpath = config.getProperty("pointcloudpath");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static PTGInputStream open(String spatialcontext, String viewpoint) throws IOException {
		return new PTGInputStream(pointcloudpath + viewpoint + ".PTG");
	}
	
	public static String getPoint(PTGInputStream in, double azimAngle, double elevAngle) throws IOException {
		PTGPoint p = in.readPoint(azimAngle,elevAngle);
		if (p == null)
			return null;
		return "POINT("+p.toString()+")";
	}
	
	public static String getLine(PTGInputStream in, double[] azimAngles, double[] elevAngles) throws IOException {
		String feature = "LINESTRING(";
		for (int i=0; i<azimAngles.length; i++) {
			PTGPoint p = in.readPoint(azimAngles[i],elevAngles[i]);
			if (p == null)
				return null;
			if (i>0)
				feature += ",";
			feature += p.toString();
		}
		return feature+")";
	}
	
	public static String getPolygon(PTGInputStream in, double[] azimAngles, double[] elevAngles) throws IOException {
		String feature = "POLYGON((";
		for (int i=0; i<azimAngles.length; i++) {
			PTGPoint p = in.readPoint(azimAngles[i],elevAngles[i]);
			if (p == null)
				return null;
			if (i>0)
				feature += ",";
			feature += p.toString();
		}
		return feature+"))";
	}
	
	public static String getPolygonTriangulation(PTGInputStream in, double[] azimAngles, double[] elevAngles) throws IOException {
		// TODO
		return null;
	}

}
