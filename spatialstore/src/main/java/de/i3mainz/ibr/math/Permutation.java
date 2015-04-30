package de.i3mainz.ibr.math;

public class Permutation {
	
	private int n;
	private int[] prev,next,h;
	
	public Permutation (int n) {
		this.n = n;
		prev = null;
		next = new int[n];
		for (int i=0; i<n; i++) {
			next[i] = i;
		}
	}
	
	public int[] next() {
		if (prev == null) {
			prev = next.clone();
			return next;
		}
		int pos = 0;
		for (int i=n-1; i>0; i--) {
			if (prev[i] > prev[i-1]) {
				pos = i;
				break;
			}
		}
		if (pos == 0) {
			return null;
		}
		pos--;
		int pos2 = 0;
		for (int i=n-1; i>pos; i--) {
			if (prev[i] > prev[pos]) {
				pos2 = i;
				break;
			}
		}
		for (int i=0; i<pos; i++) {
			next[i] = prev[i];
		}
		next[pos] = prev[pos2];
		prev[pos2] = prev[pos];
		prev[pos] = next[pos];
		for (int i=pos+1; i<n; i++) {
			next[i] = prev[pos+n-i];
		}
		h = next;
		next = prev;
		prev = h;
		return prev;
	}

}
