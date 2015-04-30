package de.i3mainz.ibr.pc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PTGPoint extends de.i3mainz.ibr.geometry.Point {

	private float rem;

	public static final int FLOAT_XYZ = 1;
	public static final int DOUBLE_XYZ = 2;
	public static final int FLOAT_XYZ_REM = 5;
	public static final int DOUBLE_XYZ_REM = 6;
	public static final int FLOAT_XYZ_RBG = 9;
	public static final int DOUBLE_XYZ_RBG = 10;
	public static final int FLOAT_XYZ_REM_RBG = 13;
	public static final int DOUBLE_XYZ_REM_RBG = 14;

	public static final double[] nachkomma = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	public PTGPoint(double x, double y, double z) {
		super(x, y, z);
	}

	public PTGPoint(byte[] data, int properties) {
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		switch (properties) {
			case FLOAT_XYZ:
				this.x = buffer.getFloat(0);
				this.y = buffer.getFloat(4);
				this.z = buffer.getFloat(8);
				break;
			case DOUBLE_XYZ:
				this.x = buffer.getDouble(0);
				this.y = buffer.getDouble(8);
				this.z = buffer.getDouble(16);
				break;
			case FLOAT_XYZ_REM:
				this.x = buffer.getFloat(0);
				this.y = buffer.getFloat(4);
				this.z = buffer.getFloat(8);
				this.rem = buffer.getFloat(12);
				break;
			case DOUBLE_XYZ_REM:
				this.x = buffer.getDouble(0);
				this.y = buffer.getDouble(8);
				this.z = buffer.getDouble(16);
				this.rem = buffer.getFloat(24);
				break;
			case FLOAT_XYZ_RBG:
				this.x = buffer.getFloat(0);
				this.y = buffer.getFloat(4);
				this.z = buffer.getFloat(8);
				break;
			case DOUBLE_XYZ_RBG:
				this.x = buffer.getDouble(0);
				this.y = buffer.getDouble(8);
				this.z = buffer.getDouble(16);
				break;
			case FLOAT_XYZ_REM_RBG:
				this.x = buffer.getFloat(0);
				this.y = buffer.getFloat(4);
				this.z = buffer.getFloat(8);
				this.rem = buffer.getFloat(12);
				break;
			case DOUBLE_XYZ_REM_RBG:
				this.x = buffer.getDouble(0);
				this.y = buffer.getDouble(8);
				this.z = buffer.getDouble(16);
				this.rem = buffer.getFloat(24);
				break;
		}
	}

	public byte[] getData() {
		ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putFloat(0, (float) x);
		buffer.putFloat(4, (float) y);
		buffer.putFloat(8, (float) z);
		buffer.putFloat(12, rem);
		return buffer.array();
	}

	public byte[] getData(int properties) {
		if (properties == FLOAT_XYZ && properties == FLOAT_XYZ_RBG) {
			ByteBuffer buffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
			buffer.putFloat(0, (float) x);
			buffer.putFloat(4, (float) y);
			buffer.putFloat(8, (float) z);
			return buffer.array();
		} else if (properties == DOUBLE_XYZ && properties == DOUBLE_XYZ_RBG) {
			ByteBuffer buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
			buffer.putDouble(0, x);
			buffer.putDouble(8, y);
			buffer.putDouble(16, z);
			return buffer.array();
		} else if (properties == FLOAT_XYZ_REM && properties == FLOAT_XYZ_REM_RBG) {
			ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
			buffer.putFloat(0, (float) x);
			buffer.putFloat(4, (float) y);
			buffer.putFloat(8, (float) z);
			buffer.putFloat(12, rem);
			return buffer.array();
		} else if (properties == DOUBLE_XYZ_REM && properties == DOUBLE_XYZ_REM_RBG) {
			ByteBuffer buffer = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);
			buffer.putDouble(0, x);
			buffer.putDouble(8, y);
			buffer.putDouble(16, z);
			buffer.putFloat(24, rem);
			return buffer.array();
		}
		return null;
	}

	public float getRemission() {
		return rem;
	}

	public double getAzimuth() {
		return Math.atan2(y, x);
	}

	public double getElevation() {
		return Math.acos(z / Math.sqrt(x * x + y * y + z * z));
	}

	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public String toString() {
		return x + " " + y + " " + z + " 0";
	}

	/**
	 *
	 * @param deci Nummer of decimal places
	 * @return Point in xyz
	 */
	public String toxyz(int deci) {
		if (deci != -1 || deci > 9) {
			x = Math.round(x * nachkomma[deci]) / nachkomma[deci];
			y = Math.round(y * nachkomma[deci]) / nachkomma[deci];
			z = Math.round(z * nachkomma[deci]) / nachkomma[deci];
		}

		return x + " " + y + " " + z;
	}

	public String toPts(int deci) {
		return this.toxyz(deci) + " " + rem;
	}

}
