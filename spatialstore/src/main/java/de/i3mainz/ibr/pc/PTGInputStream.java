package de.i3mainz.ibr.pc;

import de.i3mainz.ibr.geometry.Line;
import de.i3mainz.ibr.geometry.Plane;
import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.math.Matrix;
import java.io.*;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

public class PTGInputStream extends PTGStream {
	
	private RandomAccessFile in = null;
	
	private int version = 0;
	private String sw_name = null;
	private String scan_name = null;
	private String scanner_name = null;
	private String scanner_model = null;
	private String scanner_ip_addr = null;
	private Date creation_date = null;
	private Time creation_time = null;
	private int cols = 0;
	private int rows = 0;
	private double azim_min = 0;
	private double azim_max = 0;
	private double elev_min = 0;
	private double elev_max = 0;
	private Matrix transform = null;
	private int properties = 0;
	
	private int record = 0;
	private long[] offsets = null;
	private int size = 0;
	
	private int currentCol = -1;
	private int[] currentValids = null;
	private byte[] currentData = null;
	
	/**
	 * open PTG file to read
	 * @param filename
	 * @throws IOException
	 */
	public PTGInputStream(String filename) throws IOException {
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filename));
		readFileTypeTag(stream);
		readMagicNumber(stream);
		readHeader(stream);
		readOffsets(stream);
		stream.close();
		
		record = getRecordSize(properties);
		size = (elev_max-elev_min != 0) ? (int)Math.round(Math.PI*(rows-1)/(elev_max - elev_min)) : 0;
		// = (int)Math.round(Math.PI*(cols-1)/(azim_max - azim_min));
		// size==0  <=>  unstructured PTG file
		
		currentValids = new int[size == 0 ? rows : size];
		for (int i=0; i<currentValids.length; i++) {
			currentValids[i] = -1;
		}
		in = new RandomAccessFile(filename,"r");
	}
	
	private PTGPoint getPoint(int row) {
		if (currentValids[row] == -1)
			return null;
		return new PTGPoint(Arrays.copyOfRange(currentData,currentValids[row],currentValids[row]+record), properties);
	}
	
	public PTGPoint readPoint(int col, int row) throws IOException {
		if (currentCol != col)
			readColumnData(col);
		return getPoint(row);
	}
	
	public PTGPoint readPoint(double azim, double elev) throws IOException {
		if (size == 0) {
			return null;
		}
		else {
			int col = azimToCol(azim,size);
			int row = elevToRow(elev,size);
			return readPoint(col,row);
		}
	}
	
	private void readColumnData(int col) throws IOException {
		if (size!=0 && (colToAzim(col,size) > azim_min || colToAzim(col,size) < azim_max)) {
			for (int i=0; i<size; i++) {
				currentValids[i] = -1;
			}
			currentData = new byte[0];
		}
		else {
			in.seek(offsets[col - (size==0 ? 0 : azimToCol(azim_min,size))]);
			byte[] valids = new byte[(rows+7)/8];
			in.read(valids);
			int counter = 0;
			int rowOffset = size==0 ? 0 : elevToRow(elev_min,size);
			for (int i=0; i<rows; i++) {
				if ((valids[i/8] & 0x80) != 0) {
					currentValids[i+rowOffset] = counter;
					counter += record;
				}
				else
					currentValids[i+rowOffset] = -1;
				valids[i/8] <<= 1;
			}
			currentData = new byte[counter];
			in.read(currentData);
		}
		currentCol = col;
	}
	
	public PTGPoint[] readColumn(int col) throws IOException {
		readColumnData(col);
		PTGPoint[] points = new PTGPoint[currentData.length/record];
		for (int i=0; i<points.length; i++) {
			points[i] = new PTGPoint(Arrays.copyOfRange(currentData,i*record,(i+1)*record), properties);
		}
		return points;
	}
	
	public PTGPoint[] readFullColumn(int col) throws IOException {
		readColumnData(col);
		PTGPoint[] points = new PTGPoint[currentValids.length];
		for (int i=0; i<points.length; i++)
			points[i] = getPoint(i);
		return points;
	}
	
	public PTGPoint[] readColumn() throws IOException {
		int col = currentCol+1;
		if (col >= 0 && col < (size==0 ? cols : 2*size))
			return readColumn(col);
		return null;
	}
	
	public PTGPoint[] readFullColumn() throws IOException {
		int col = currentCol+1;
		if (col >= 0 && col < (size==0 ? cols : 2*size))
			return readFullColumn(col);
		return null;
	}
	public ArrayList<PTGPoint> getAllPointsInsideTriangle(double azimA, double elevA, double azimB, double elevB, double azimC, double elevC) throws IOException{
            return getAllPointsInsideTriangle(azimA, elevA, azimB, elevB, azimC, elevC, 1);
        }
        /**
         * 
         * @param azimA
         * @param elevA
         * @param azimB
         * @param elevB
         * @param azimC
         * @param elevC
         * @param step Nummber of Points to step over (step=10 every tenthe point)
         * @return
         * @throws IOException 
         */
	public ArrayList<PTGPoint> getAllPointsInsideTriangle(double azimA, double elevA, double azimB, double elevB, double azimC, double elevC, double step) throws IOException {
		if (size == 0)
			return new ArrayList<PTGPoint>();
		
		if (azimB < azimA) {
			double h = azimA;
			azimA = azimB;
			azimB = h;
			h = elevA;
			elevA = elevB;
			elevB = h;
		}
		if (azimC < azimB) {
			double h = azimC;
			azimC = azimB;
			azimB = h;
			h = elevC;
			elevC = elevB;
			elevB = h;
		}
		if (azimB < azimA) {
			double h = azimA;
			azimA = azimB;
			azimB = h;
			h = elevA;
			elevA = elevB;
			elevB = h;
		}
		
		ArrayList<PTGPoint> points = new ArrayList<PTGPoint>();
		
		Point a = new Point(azimA,elevA);
		Point b = new Point(azimB,elevB);
		Point c = new Point(azimC,elevC);
		
		Point orig = new Point(0,0,0);
		Point z = new Point(0,0,1);
		
		Line lineAC = new Line(a,c);
		Line lineAB = new Line(a,b);
		Line lineBC = new Line(b,c);
		
		for (double azim=azimA; azim<azimC; azim+=step*Math.PI/size) {
			Plane plane = new Plane(orig,z,new Point(azim,Math.PI/2));
			if (azim<azimB) {
				Point start = Point.cut(lineAC,plane);
				Point end = Point.cut(lineAB,plane);
				double elevStart = start.getElev()<end.getElev() ? start.getElev() : end.getElev();
				double elevEnd = start.getElev()>end.getElev() ? start.getElev() : end.getElev();
				for (double elev=elevStart; elev<elevEnd; elev+=step*Math.PI/size) {
					PTGPoint p = readPoint(azim>Math.PI?azim-2*Math.PI:azim,elev);
					if (p != null)
						points.add(p);
				}
			}
			else {
				Point start = Point.cut(lineAC,plane);
				Point end = Point.cut(lineBC,plane);
				double elevStart = start.getElev()<end.getElev() ? start.getElev() : end.getElev();
				double elevEnd = start.getElev()>end.getElev() ? start.getElev() : end.getElev();
				for (double elev=elevStart; elev<elevEnd; elev+=step*Math.PI/size) {
					PTGPoint p = readPoint(azim>Math.PI?azim-2*Math.PI:azim,elev);
					if (p != null)
						points.add(p);
				}
			}
		}
		
		return points;
}

        public ArrayList<PTGPoint> getAllPointsOnLine(double azimA, double elevA, double azimB, double elevB, double step) throws IOException {
        if (size == 0) {
            return new ArrayList<>();
        }

        if (azimB < azimA) {
            double h = azimA;
            azimA = azimB;
            azimB = h;
            h = elevA;
            elevA = elevB;
            elevB = h;
        }

        ArrayList<PTGPoint> points = new ArrayList<>();

        Point a = new Point(azimA, elevA);
        Point b = new Point(azimB, elevB);

        Point orig = new Point(0, 0, 0);
        Point z = new Point(0, 0, 1);

        Line line = new Line(a, b);

        for (double azim = azimA; azim < azimB; azim += step*Math.PI / size) {
            Plane plane = new Plane(orig, z, new Point(azim, Math.PI / 2));
            Point cut = Point.cut(line, plane);
            PTGPoint p = readPoint(cut.getAzim()>Math.PI?cut.getAzim()-2*Math.PI:cut.getAzim(), cut.getElev());
			if (p != null) {
                points.add(p);
            }

        }

        return points;
    }

	public int getCols() {
		return size==0 ? cols : 2*size;
	}
	
	public int getSize() {
		return size==0 ? cols/2 : size;
	}
	
	public void close() throws IOException {
		if (in != null) {
			in.close();
			in = null;
		}
	}

	public String getSwName() {
		return sw_name;
	}
	
	public String getScanName() {
		return scan_name;
	}
	
	public String getScannerName() {
		return scanner_name;
	}
	
	public String getScannerModel() {
		return scanner_model;
	}
	
	public String getScannerIpAddr() {
		return scanner_ip_addr;
	}
	
	public Date getCreationDate() {
		return creation_date;
	}
	
	public Time getCreationTime() {
		return creation_time;
	}
	
	public Matrix getTransform() {
		return transform;
	}
	
	public int getProperties() {
		return properties;
	}
	
	
	
	// private methods
	
	private void readFileTypeTag(InputStream stream) throws IOException {
		byte[] ptg = new byte[4];
		stream.read(ptg);
		if (ptg[0] != 'P' || ptg[1] != 'T' || ptg[2] != 'G' || ptg[3] != '\0')
			throw new PTGFormatException("[4 bytes] File type tag: start with �P�, �T�, �G�, �\0�.");
	}
	
	private void readMagicNumber(InputStream stream) throws IOException {
		if (readInt(stream) != PTGStream.magicNumber)
			throw new PTGFormatException("Magic number is not correct.");
	}
	
	public void readHeader(InputStream stream) throws IOException {
		String key = readString(stream);
		if (!key.equals("%%header_begin"))
			throw new PTGFormatException("%%header_begin expected, but found: " + key);
		do {
			key = readString(stream);
			if (!key.startsWith("%%"))
				throw new PTGFormatException("Wrong metadata key: " + key);
			key = key.substring(2);
			if (key.equals("version")) {
				version = readInt(stream);
				if (version != 1)
					throw new PTGFormatException("Version not supported: " + version);
			}
			else if (key.equals("sw_name"))
				sw_name = readString(stream);
			else if (key.equals("scan_name"))
				scan_name = readString(stream);
			else if (key.equals("scanner_name"))
				scanner_name = readString(stream);
			else if (key.equals("scanner_model"))
				scanner_model = readString(stream);
			else if (key.equals("scanner_ip_addr"))
				scanner_ip_addr = readString(stream);
			else if (key.equals("creation_date"))
				creation_date = readDate(stream);
			else if (key.equals("creation_time"))
				creation_time = readTime(stream);
			else if (key.startsWith("texte_") || key.startsWith("text_")) // ignored
				readString(stream);
			else if (key.equals("cols"))
				cols = readInt(stream);
			else if (key.equals("rows"))
				rows = readInt(stream);
			else if (key.equals("rows_total")) // ignored
				readInt(stream);
			else if (key.equals("azim_min"))
				azim_min = readDouble(stream);
			else if (key.equals("azim_max"))
				azim_max = readDouble(stream);
			else if (key.equals("elev_min"))
				elev_min = readDouble(stream);
			else if (key.equals("elev_max"))
				elev_max = readDouble(stream);
			else if (key.equals("transform"))
				transform = readMatrix(stream);
			else if (key.equals("properties"))
				properties = readInt(stream);
			else if (key.equals("header_end"))
				break;
			else
				throw new PTGFormatException("Metadata key does not exist: " + key);
		} while (true);
	}
	
	private void readOffsets(InputStream stream) throws IOException {
		offsets = new long[cols];
		for (int i=0; i<cols; i++) {
			offsets[i] = readLong(stream);
		}
	}

}
