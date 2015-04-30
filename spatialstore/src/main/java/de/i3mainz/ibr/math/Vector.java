package de.i3mainz.ibr.math;

public class Vector {
	
	protected double[] x;
	
	public Vector (double ... x) {
		this.x = x.clone();
	}
	
	public static Vector zeros(int n) {
		double[] x = new double[n];
		for (int i=0; i<n; i++) {
			x[i] = 0;
		}
		return new Vector(x);
	}
	
	public static Vector ones(int n) {
		double[] x = new double[n];
		for (int i=0; i<n; i++) {
			x[i] = 1;
		}
		return new Vector(x);
	}
	
	public int size() {
		return x.length;
	}
	
	public double sqrnorm() {
		double norm = 0;
		for (int i=0; i<this.size(); i++) {
			norm += x[i]*x[i];
		}
		return norm;
	}
	
	public double norm() {
		return Math.sqrt(this.sqrnorm());
	}
	
	public Vector addV (Vector other) {
		if (this.size() != other.size())
			throw new DimensionMismatchException("can not add vector of size " + other.size() + " to vector of size " + this.size());
		double[] x = new double[this.size()];
		for (int i=0; i<x.length; i++) {
			x[i] = this.x[i]+other.x[i];
		}
		return new Vector(x);
	}
	
	public Vector subV (Vector other) {
		if (this.size() != other.size())
			throw new DimensionMismatchException("can not substract vector of size " + other.size() + " from vector of size " + this.size());
		double[] x = new double[this.size()];
		for (int i=0; i<x.length; i++) {
			x[i] = this.x[i]-other.x[i];
		}
		return new Vector(x);
	}
	
	public Vector mulS (double number) {
		double[] x = new double[this.size()];
		for (int i=0; i<x.length; i++) {
			x[i] = number*this.x[i];
		}
		return new Vector(x);
	}
	
	public double mulV (Vector other) {
		if (this.size() != other.size())
			throw new DimensionMismatchException("can not multiply vector of size " + this.size() + " with vector of size " + other.size());
		double dot = 0;
		for (int i=0; i<x.length; i++) {
			dot += this.x[i]*other.x[i];
		}
		return dot;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<this.size(); i++) {
			s += x[i] + "\t";
		}
		return s;
	}
	
	public Vector clone() {
		return new Vector(x);
	}

}
