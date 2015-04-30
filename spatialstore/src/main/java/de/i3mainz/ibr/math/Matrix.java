package de.i3mainz.ibr.math;

public class Matrix {
	
	double[][] a;
	
	public Matrix(double ... a) {
		int s=0;
		while(a.length > s*s) {
			s++;
		}
		if (s*s != a.length)
			throw new DimensionMismatchException("matrix must be cubic, but has " + a.length + " entries instead");
		this.a = new double[s][s];
		for (int i=0; i<s; i++) {
			for (int j=0; j<s; j++) {
				this.a[i][j] = a[i*s+j];
			}
		}
	}
	
	public Matrix(int rows, int cols, double ... a) {
		if (rows*cols != a.length)
			throw new DimensionMismatchException("matrix should have " + rows + " rows and " + cols + " cols, but has " + a.length + " entries instead");
		this.a = new double[rows][cols];
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				this.a[i][j] = a[i*rows+j];
			}
		}
	}
	
	public Matrix(double[][] a) {
		for (int i=1; i<a.length; i++) {
			if (a[i].length != a[i-1].length)
				throw new DimensionMismatchException("matrix should have same number of cols in every row");
		}
		this.a = a.clone();
	}
	
	public static Matrix eye(int n) {
		double[][] a = new double[n][n];
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				a[i][j] = i==j ? 1 : 0;
			}
		}
		return new Matrix(a);
	}
	
	public static Matrix zeros(int n) {
		return zeros(n,n);
	}
	
	public static Matrix zeros(int rows, int cols) {
		double[][] a = new double[rows][cols];
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				a[i][j] = 0;
			}
		}
		return new Matrix(a);
	}
	
	public static Matrix ones(int n) {
		return zeros(n,n);
	}
	
	public static Matrix ones(int rows, int cols) {
		double[][] a = new double[rows][cols];
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				a[i][j] = 1;
			}
		}
		return new Matrix(a);
	}
	
	public int rows() {
		return a.length;
	}
	
	public int cols() {
		return a[0].length;
	}
	
	public Matrix mulM (Matrix other) {
		if (this.cols() != other.rows())
			throw new DimensionMismatchException("can not multiply matrix with " + this.cols() + " cols with matrix with " + other.rows() + " rows");
		double[][] a = new double[this.rows()][other.cols()];
		for (int i=0; i<this.rows(); i++) {
			for (int j=0; j<other.cols(); j++) {
				a[i][j] = 0;
				for (int k=0; k<this.cols(); k++) {
					a[i][j] += this.a[i][k]*other.a[k][j];
				}
			}
		}
		return new Matrix(a);
	}
	
	public Vector mulV (Vector vec) {
		if (this.cols() != vec.size())
			throw new DimensionMismatchException("can not multiply matrix with " + this.cols() + " cols with vector of size " + vec.size());
		double[] x = new double[this.rows()];
		for (int i=0; i<this.rows(); i++) {
			x[i] = 0;
			for (int k=0; k<this.cols(); k++) {
				x[i] += this.a[i][k]*vec.x[k];
			}
		}
		return new Vector(x);
	}
	
	public double determinant() {
		if (this.rows() != this.cols())
			throw new DimensionMismatchException("determinant only with cubic matrix");
		if (this.rows() <= 1)
			return a[0][0];
		else {
			double det = 0;
			for (int i=0; i<this.rows(); i++) {
				det += (i%2==0 ? 1 : -1)*a[i][0]*minor(i,0);
			}
			return det;
		}
	}
	

	public Matrix inverse() {
		double det = determinant();
		if (det == 0.0)
			throw new DimensionMismatchException("matrix is not invertible");
		double[][] inv = new double[this.rows()][this.cols()];
		for (int i=0; i<this.rows(); i++) {
			for (int j=0; j<this.cols(); j++) {
				inv[i][j] = ((i+j)%2==0 ? 1 : -1)*minor(j,i)/det;
			}
		}
		return new Matrix(inv);
	}
	
	private double minor(int row, int col) {
		double[][] m = new double[this.rows()-1][this.cols()-1];
		for (int i=0; i<this.rows(); i++) {
			if (i==row)
				continue;
			for (int j=0; j<this.cols(); j++) {
				if (j==col)
					continue;
				m[i<row ? i : i-1][j<col ? j : j-1] = a[i][j];
			}
		}
		return new Matrix(m).determinant();
	}
	
	public double[][] array() {
		return a;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<this.rows(); i++) {
			for (int j=0; j<this.cols(); j++) {
				s += a[i][j] + "\t";
			}
			s += "\n";
		}
		return s;
	}
	
	public Matrix clone() {
		return new Matrix(a);
	}

}

