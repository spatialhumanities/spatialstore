package de.i3mainz.ibr.pc;

import de.i3mainz.ibr.geometry.PlanarPolygon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author s3b31293
 */
public class PTGInteraction {

	public static double getPrismaPoint(PlanarPolygon backPolygon, String path) throws IOException {

		ArrayList<PTGPoint> points = backPolygon.getPTGPoints(path, 1);
		double[] distance = new double[points.size()];
		double avrDist=0;
		int i = 0;
		for (PTGPoint p : points) {
			double dist = backPolygon.getBestPlane().distance(p);
			avrDist += dist;
			distance[i++]=dist;	
		}
		avrDist /= (double) distance.length;
		double bestDist;
		if(avrDist<0)
			bestDist=getPercentElement(distance,0.05);
		else
			bestDist=getPercentElement(distance,0.95);
		return bestDist;
	}

	public static double getPercentElement(double[] array, double percent) {
		int k = (int)(percent*(array.length-1));
		
		int min = 0;
		int max = array.length;

		while (max - min > 1) {

			// Möglichst günstiges Pivotelement bestimmen
			int m = (max - min + 4) / 5;
			double[] b = new double[m];

			for (int i = 0; i < m - 1; i++) {
				b[i] = getMedian(array, min + i * 5, min + (i + 1) * 5);
			}

			b[m - 1] = getMedian(array, min + (m - 1) * 5, max);

			double pivot = getPercentElement(b, m / 2);

			// Zerteilung anhand Pivotelement
			int lower = min;
			int upper = max - 1;

			while (lower < upper) {
				while (array[lower] < pivot && lower < upper) {
					lower++;
				}
				while (array[upper] > pivot && lower < upper) {
					upper--;
				}

				if (lower < upper) {
					swap(array, lower, upper);
					lower++;
					upper--;
				}
			}
			if (array[lower] < pivot) {
				lower++;
			}

	  	// Steckt k im Intervall oberhalb oder unterhalb lower?
			// Entsprechende Anpassung der Grenzen
			if (lower > k) {
				max = lower;
			} else {
				min = lower;
			}
		}

		return array[min];
	}

	private static void swap(double[] array, int i, int j) {
		double temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	//return median of maximal 5 Elements
	private static double getMedian(double[] array, int from, int to) {
		Arrays.sort(array, from, to);
		return (array[from + (to - from) / 2]);
	}
}
