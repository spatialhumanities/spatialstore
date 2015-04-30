package de.i3mainz.ibr.pc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Date;
import java.sql.Time;

import de.i3mainz.ibr.math.Matrix;

abstract class PTGStream implements Closeable {
	
	protected final static int magicNumber = 0x928FA3C7;
	
	private static ByteBuffer integerBuffer = ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
	private static ByteBuffer    longBuffer = ByteBuffer.allocate(   Long.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
	private static ByteBuffer   floatBuffer = ByteBuffer.allocate(  Float.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
	private static ByteBuffer  doubleBuffer = ByteBuffer.allocate( Double.SIZE/Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN);
	
	protected static double colToAzim(int col, int size) {
		return (1-(col+0.5)/size)*Math.PI;
	}
	
	protected static double rowToElev(int row, int size) {
		return ((row+0.5)/size)*Math.PI;
	}
	
	protected static int azimToCol(double azim, int size) {
		return (int)((1-azim/Math.PI)*size);
	}
	
	protected static int elevToRow(double elev, int size) {
		return (int)((elev/Math.PI)*size);
	}
	
	
	/**
	 * Read String from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static String readString(InputStream stream) throws IOException {
		int length = readInt(stream)-1;
		byte[] b = new byte[length+1];
		stream.read(b);
		String s = new String();
		for (int i=0; i<length; i++) {
			s += (char)b[i];
		}
		return s;
	}
	
	/**
	 * Read Integer from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static int readInt(InputStream stream) throws IOException {
		byte[] b = new byte[Integer.SIZE/Byte.SIZE];
		stream.read(b);
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	/**
	 * Read Long from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static long readLong(InputStream stream) throws IOException {
		byte[] b = new byte[Long.SIZE/Byte.SIZE];
		stream.read(b);
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}
	
	/**
	 * Read Float from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static float readFloat(InputStream stream) throws IOException {
		byte[] b = new byte[Float.SIZE/Byte.SIZE];
		stream.read(b);
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}
	
	/**
	 * Read Double from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static double readDouble(InputStream stream) throws IOException {
		byte[] b = new byte[Double.SIZE/Byte.SIZE];
		stream.read(b);
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getDouble();
	}
	
	/**
	 * Read Date from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static Date readDate(InputStream stream) throws IOException {
		return Date.valueOf(readString(stream).replace('/','-'));
	}
	
	/**
	 * Read Time from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static Time readTime(InputStream stream) throws IOException {
		return Time.valueOf(readString(stream));
	}
	
	/**
	 * Read Matrix from stream
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected static Matrix readMatrix(InputStream stream) throws IOException {
		double[] a = new double[16];
		for (int i=0; i<16; i++) {
			a[i] = readDouble(stream);
		}
		return new Matrix(a);
	}
	
	/**
	 * Write String to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, String value) throws IOException {
		byte[] b = new byte[value.length()+1];
		int count = write(stream,b.length);
		for (int i=0; i<b.length-1; i++) {
			b[i] = (byte)value.charAt(i);
		}
		b[b.length-1] = '\0';
		stream.write(b);
		return count + b.length;
	}
	
	/**
	 * Write Integer to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, int value) throws IOException {
		integerBuffer.clear();
		byte[] b = integerBuffer.putInt(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Long to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, long value) throws IOException {
		longBuffer.clear();
		byte[] b = longBuffer.putLong(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Float to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, float value) throws IOException {
		floatBuffer.clear();
		byte[] b = floatBuffer.putFloat(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Double to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, double value) throws IOException {
		doubleBuffer.clear();
		byte[] b = doubleBuffer.putDouble(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Date to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, Date value) throws IOException {
		return write(stream,value.toString().replace('-','/'));
	}
	
	/**
	 * Write Time to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, Time value) throws IOException {
		return write(stream,value.toString());
	}
	
	/**
	 * Write Matrix to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(OutputStream stream, Matrix value) throws IOException {
		int count = 0;
		double[][] a = value.array();
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				count += write(stream,a[i][j]);
			}
		}
		return count;
	}
	
	/**
	 * Write String to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, String value) throws IOException {
		byte[] b = new byte[value.length()+1];
		int count = write(stream,b.length);
		for (int i=0; i<b.length-1; i++) {
			b[i] = (byte)value.charAt(i);
		}
		b[b.length-1] = '\0';
		stream.write(b);
		return count + b.length;
	}
	
	/**
	 * Write Integer to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, int value) throws IOException {
		byte[] b = integerBuffer.putInt(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Long to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, long value) throws IOException {
		longBuffer.clear();
		byte[] b = longBuffer.putLong(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Float to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, float value) throws IOException {
		byte[] b = floatBuffer.putFloat(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Double to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, double value) throws IOException {
		byte[] b = doubleBuffer.putDouble(value).array();
		stream.write(b);
		return b.length;
	}
	
	/**
	 * Write Date to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, Date value) throws IOException {
		return write(stream,value.toString().replace('-','/'));
	}
	
	/**
	 * Write Time to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, Time value) throws IOException {
		return write(stream,value.toString());
	}
	
	/**
	 * Write Matrix to stream
	 * @param stream
	 * @param value
	 * @return number of Bytes that have been written
	 * @throws IOException
	 */
	protected static int write(RandomAccessFile stream, Matrix value) throws IOException {
		int count = 0;
		double[][] a = value.array();
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				count += write(stream,a[i][j]);
			}
		}
		return count;
	}
	
	/**
	 * Determine size of one single record, dependent of properties
	 * @param properties
	 * @return
	 */
	protected static int getRecordSize(int properties) {
		switch(properties) {
		case  1: return 12;
		case  2: return 24;
		case  5: return 16;
		case  6: return 28;
		case  9: return 15;
		case 10: return 27;
		case 13: return 19;
		case 14: return 31;
		default: return 0;
		}
	}

}
