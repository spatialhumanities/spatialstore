/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.i3mainz.ibr.pc;

import de.i3mainz.ibr.geometry.Point;
import de.i3mainz.ibr.geometry.Util;
import java.util.ArrayList;

/**
 *
 * @author Arno Heidelberg
 */
public class Missclick {

    private static final double tiefe = 0.1;

    public static ArrayList<Point> fixPoint(ArrayList<Point> toFix, ArrayList<PTGPoint> figure, double searchAngle) {
        ArrayList<Point> bestFix = new ArrayList<Point>();
        for (Point v : toFix) {
            bestFix.add(fixPoint(v, figure, searchAngle));

        }
        return bestFix;
    }

    /**
     *
     * @param toFix Zu prüfender Punkt
     * @param figure Zu prüfendes Polygon
     * @param searchAngle Winkel für den Suchbereich
     * @return Point Vorschlag für einen "richtigen" Punkt
     */
    public static Point fixPoint(Point toFix, ArrayList<PTGPoint> figure, double searchAngle) {
        
        ArrayList<PTGPoint> umgebung = relevantPoints(toFix, figure, searchAngle);
        double mittelwert = avrDistance(umgebung);
        Point bestFix = null;

        for (Point p : umgebung) {
            if (Math.abs(mittelwert - p.norm()) < tiefe) {
                if (bestFix != null) {
                    bestFix = angleSqr(p, toFix) < angleSqr(bestFix, toFix) ? p : bestFix;
                } else {
                    bestFix = p;
                }
            }

        }

        return bestFix;
    }

    public static boolean isMissClick(Point toFix, ArrayList<PTGPoint> figure, double searchAngle) {

        ArrayList<PTGPoint> umgebung = relevantPoints(toFix, figure, searchAngle);

        return Math.abs(avrDistance(umgebung) - toFix.norm()) > tiefe;
    }

    private static ArrayList<PTGPoint> relevantPoints(Point toFix, ArrayList<PTGPoint> figure, double searchAngle) {
        double azimMin = Util.check(toFix.getAzim() - searchAngle);
        double elevMin = Util.check(toFix.getElev() - searchAngle);
        double azimMax = Util.check(toFix.getAzim() + searchAngle);
        double elevMax = Util.check(toFix.getElev() + searchAngle);

        ArrayList<PTGPoint> umgebung = new ArrayList<PTGPoint>();

        for (PTGPoint p : figure) {
            if (p.getAzimuth() < azimMax && p.getAzimuth() > azimMin && p.getElevation() < elevMax && p.getElevation() > elevMin) {
                umgebung.add(p);
            }
        }

        return umgebung;
    }

    private static double avrDistance(ArrayList<PTGPoint> umgebung) {
        double mittelwert = 0;
        for (PTGPoint p : umgebung) {
            mittelwert += p.norm();
        }
        return mittelwert / umgebung.size();

    }

    private static double angleSqr(Point p, Point toFix) {
        return Math.sqrt(Math.pow(p.getAzim() - toFix.getAzim(), 2) + Math.pow(p.getElev() - toFix.getElev(), 2));
    }

}
