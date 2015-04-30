package de.i3mainz.ibr.geometry;

/**
 * This class could also be interpreted as 2D-Point
 */
public class Angle {

    private double azim;
    private double elev;

    /**
     * Origin
     */
    public Angle() {
        this.azim = 0;
        this.elev = 0;
    }

    /**
     * Point with specified angles
     *
     * @param azim
     * @param elev
     */
    public Angle(double azim, double elev) {
        this.azim = azim;
        this.elev = elev;
    }

    /**
     * Returns 2D-distance between this and other
     *
     * @param other
     * @return
     */
    public double distance(Angle other) {
        return Math.sqrt(Math.pow(this.azim - other.azim, 2) + Math.pow(this.elev - other.elev, 2));
    }

    public double getAzim() {
        return this.azim;
    }

    public double getElev() {
        return this.elev;
    }

    @Override
    public String toString() {
        return "{ \"azim\": " + azim + ", \"elev\": " + elev + " }";
    }

}
