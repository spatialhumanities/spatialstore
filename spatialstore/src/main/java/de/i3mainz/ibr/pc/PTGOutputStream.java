package de.i3mainz.ibr.pc;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.sql.Time;

import de.i3mainz.ibr.math.Matrix;

public class PTGOutputStream extends PTGStream {
	
	private String filename;
	private int size;
	private int col_min;
	private int col_max;
	private int row_min;
	private int row_max;
	private RandomAccessFile tmpout = null;
	
	private int version = 0;
	private String sw_name = null;
	private String scan_name = null;
	private String scanner_name = null;
	private String scanner_model = null;
	private String scanner_ip_addr = null;
	private Date creation_date = null;
	private Time creation_time = null;
	private Matrix transform = null;
	private int properties = 0;
	
	private int record = 0;
	
	public PTGOutputStream(String filename, int size, int properties) throws IOException {
		this.filename = filename;
		this.size = size;
		this.properties = properties;
		this.record = getRecordSize(properties);
		this.version = 1;
		col_min = 2*size;
		col_max = 0;
		row_min = size;
		row_max = 0;
		
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filename+".tmp",false));
		byte[] b = new byte[size*record];
		for (int i=0; i<b.length; i++) {
			b[i] = 0;
		}
		for (int i=0; i<2*size; i++) {
			stream.write(b);
		}
		stream.close();
		
		tmpout = new RandomAccessFile(filename+".tmp","rw");
	}
	
	public void write(PTGPoint p) throws IOException {
		int col = azimToCol(p.getAzimuth(),size);
		int row = elevToRow(p.getElevation(),size);
		if (col < col_min)
			col_min = col;
		if (col > col_max)
			col_max = col;
		if (row < row_min)
			row_min = row;
		if (row > row_max)
			row_max = row;
		tmpout.seek((col*size+row)*record);
		tmpout.write(p.getData());
	}
	
	public void close() throws IOException {
		tmpout.close();
		
		long marker = 0;
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filename,false));
		marker += writeFileTypeTag(stream);
		marker += writeMagicNumber(stream);
		marker += writeHeader(stream);
		long[] offset = new long[col_max-col_min+1];
		stream.write(new byte[8*offset.length]);
		marker += 8*offset.length;
		
		BufferedInputStream tmpin = new BufferedInputStream(new FileInputStream(filename+".tmp"));
		byte[] b = new byte[size*record];
		boolean[] valids = new boolean[row_max-row_min+1];
		for (int i=0; i<2*size; i++) {
			tmpin.read(b);
			if (i >= col_min && i<=col_max) {
				offset[i-col_min] = marker;
				int counter = 0;
				for (int j=row_min; j<=row_max; j++) {
					valids[j-row_min] = false;
					for (int k=0; k<record; k++) {
						if (b[j*record+k] != 0)
							valids[j-row_min] = true;
					}
					if (valids[j-row_min])
						counter++;
				}
				byte[] validBytes = new byte[(valids.length+7)/8];
				for (int j=0; j<validBytes.length; j++) {
					validBytes[j] = 0;
					for (int k=0; k<8; k++) {
						validBytes[j] <<= 1;
						if (8*j+k < valids.length && valids[8*j+k])
							validBytes[j] |= 1;
					}
				}
				stream.write(validBytes);
				marker += validBytes.length;
				byte[] data = new byte[counter*record];
				counter = 0;
				for (int j=row_min; j<=row_max; j++) {
					if (valids[j-row_min]) {
						for (int k=0; k<record; k++) {
							data[counter*record+k] = b[j*record+k];
						}
						counter++;
					}
				}
				stream.write(data);
				marker += data.length;
			}
		}
		tmpin.close();
		stream.close();
		
		tmpout = new RandomAccessFile(filename,"rw");
		tmpout.seek(offset[0]-8*offset.length);
		for (int i=0; i<offset.length; i++) {
			write(tmpout,offset[i]);
		}
		tmpout.close();
		
		new File(filename+".tmp").delete();
	}
	
	public void setSwName(String value) {
		sw_name = value;
	}
	
	public void setScanName(String value) {
		scan_name = value;
	}
	
	public void setScannerName(String value) {
		scanner_name = value;
	}
	
	public void setScannerModel(String value) {
		scanner_model = value;
	}
	
	public void setScannerIpAddr(String value) {
		scanner_ip_addr = value;
	}
	
	public void setCreationDate(Date value) {
		creation_date = value;
	}
	
	public void setCreationTime(Time value) {
		creation_time = value;
	}
	
	public void setTransform(Matrix value) {
		transform = value;
	}
	
	
	
	// private methods
	
	private int writeFileTypeTag(OutputStream stream) throws IOException {
		byte[] ptg = new byte[4];
		ptg[0] = 'P';
		ptg[1] = 'T';
		ptg[2] = 'G';
		ptg[3] = '\0';
		stream.write(ptg);
		return 4;
	}
	
	private int writeMagicNumber(OutputStream stream) throws IOException {
		return write(stream,PTGStream.magicNumber);
	}
	
	private int writeHeader(OutputStream stream) throws IOException {
		int count = 0;
		count += write(stream,"%%header_begin");
		count += write(stream,"%%version");
		count += write(stream,version);
		if (sw_name != null) {
			count += write(stream,"%%sw_name");
			count += write(stream,sw_name);
		}
		if (scan_name != null) {
			count += write(stream,"%%scan_name");
			count += write(stream,scan_name);
		}
		if (scanner_name != null) {
			count += write(stream,"%%scanner_name");
			count += write(stream,scanner_name);
		}
		if (scanner_model != null) {
			count += write(stream,"%%scanner_model");
			count += write(stream,scanner_model);
		}
		if (scanner_ip_addr != null) {
			count += write(stream,"%%scanner_ip_addr");
			count += write(stream,scanner_ip_addr);
		}
		if (creation_date != null) {
			count += write(stream,"%%creation_date");
			count += write(stream,creation_date);
		}
		if (creation_time != null) {
			count += write(stream,"%%creation_time");
			count += write(stream,creation_time);
		}
		count += write(stream,"%%cols");
		count += write(stream,col_max-col_min+1);
		count += write(stream,"%%rows");
		count += write(stream,row_max-row_min+1);
		count += write(stream,"%%azim_min");
		count += write(stream,colToAzim(col_min,size));
		count += write(stream,"%%azim_max");
		count += write(stream,colToAzim(col_max,size));
		count += write(stream,"%%elev_min");
		count += write(stream,rowToElev(row_min,size));
		count += write(stream,"%%elev_max");
		count += write(stream,rowToElev(row_max,size));
		if (transform != null) {
			count += write(stream,"%%transform");
			count += write(stream,transform);
		}
		count += write(stream,"%%properties");
		count += write(stream,properties);
		count += write(stream,"%%header_end");
		return count;
	}

}
