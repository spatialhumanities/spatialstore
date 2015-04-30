package de.i3mainz.ibr.math;

public class OnlineStatistic {
	
	private int counter = 0;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private double sum = 0;
	private double sqrSum = 0;
	private int[] histo;
	
	public OnlineStatistic(int steps) {
		histo = new int[steps];
		for (int i=0; i<steps; i++)
			histo[i] = 0;
	}
	
	public void add(double value) {
		counter++;
		sum += value;
		sqrSum += value*value;
		if (value < min)
			min = value;
		if (value > max)
			max = value;
		if (value > 0 && value < 1) {
			histo[(int)(value*histo.length)]++;
		}
		else {
			System.out.println(value);
		}
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getMean() {
		return sum/counter;
	}
	
	public double getVariance() {
		return (sqrSum - sum*sum/counter)/(counter-1);
	}
	
	public void print() {
		for (int i=0; i<histo.length; i++) {
			System.out.println(i*0.01 + "\t" + histo[i]);
		}
	}

}
