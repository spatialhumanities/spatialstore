package de.i3mainz.ibr.visibility;

import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.connections.Identification;
import de.i3mainz.ibr.geometry.Point;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;

/**
 *
 * @author heidelberg
 */
public class MainVisibility implements Runnable {
	
	private final Identification user;
	private final Point[] objectPoints;
	private final double zPlane;
	private final double ignoreOffset;
	private final String discreteSpace;
	private final String wallColor;
	private final String fromColor;
	private final String toColor;
	
	public MainVisibility(Identification user, Point[] objectPoints, double zPlane, double ignoreOffset, String discreteSpace, String wallColor, String fromColor, String toColor) {
		this.user = user;
		this.objectPoints = objectPoints;
		this.zPlane = zPlane;
		this.ignoreOffset = ignoreOffset;
		this.discreteSpace = discreteSpace;
		this.wallColor = wallColor;
		this.fromColor = fromColor;
		this.toColor = toColor;
	}
	
	@Override
	public void run() {
		visWithGround();
	}
	
	private static boolean isInUse = false;

	/**
	 * generates the groundplan and the visibility for the sightlevel and the
	 * objectpoints. combines both images into one image.
	 */
	public void visWithGround() {
		String filename = Config.getProperty("dsfpath") + discreteSpace;
		try (DiscreteSpaceFileWriter dsfw = new DiscreteSpaceFileWriter(filename);) {
			ArrayList<boolean[]> visAnalysis = dsfw.visibillity(objectPoints, zPlane, ignoreOffset);
			BufferedImage image = dsfw.visExportForObjects(visAnalysis, fromColor, toColor);
			BufferedImage groundImage = dsfw.export(zPlane, wallColor);
			BufferedImage combine = combineImages(image, groundImage);
			ImageIO.write(combine,"png",new File(Config.getProperty("dsfpath") + "visibility.png"));
			user.setState(4);
		} catch (Exception e) {
			Config.warn(e.getMessage());
			user.setState(0);
		}
	}

	/**
	 * generates the visibility between the objectpoints and the sightlevel
	 *
	 * @param objectPoints Array of object points
	 * @param zPlane sightlevel
	 * @param ignoreOffset in Units of the Grid
	 * @param discreteSpace Name of the dsf file
	 * @param fromColor	starting color for no object visible (HEX format)
	 * @param toColor ending color, all objects visible (HEX format)
	 * @return
	 */
	public static Response visibilityWhitOutGround(Point[] objectPoints, double zPlane, double ignoreOffset, String discreteSpace, String fromColor, String toColor) {
		if (isInUse) {
			Exception e = new Exception("Only one analysis at the same time possible!");
			return Config.getResult(e);
		}
		isInUse = true;
		String filename = Config.getProperty("dsfpath") + discreteSpace;
		try (DiscreteSpaceFileWriter dsfw = new DiscreteSpaceFileWriter(filename);) {
			ArrayList<boolean[]> visAnalysis = dsfw.visibillity(objectPoints, zPlane, ignoreOffset);
			BufferedImage image = dsfw.visExportForObjects(visAnalysis, fromColor, toColor);
			return Config.getResult(convertBuffImg(image));
		} catch (Exception e) {
			return Config.getResult(e);
		} finally {
			isInUse = false;
		}
	}

	/**
	 * generates the groundplane for the sightlevel
	 *
	 * @param zPlane sightlevel
	 * @param discreteSpace Name of the dsf file
	 * @param wallColor Color of the not visible parts in Hex (black = FF000000)
	 * @return
	 */
	public static Response ground(double zPlane, String discreteSpace, String wallColor) {
		if (isInUse) {
			Exception e = new Exception("Only one analysis at the same time possible!");
			return Config.getResult(e);
		}
		isInUse = true;
		String filename = Config.getProperty("dsfpath") + discreteSpace;
		try (DiscreteSpaceFileWriter dsfw = new DiscreteSpaceFileWriter(filename);) {
			BufferedImage groundImage = dsfw.export(zPlane, wallColor);
			return Config.getResult(convertBuffImg(groundImage));
		} catch (Exception e) {
			return Config.getResult(e);
		} finally {
			isInUse = false;
		}
	}

	private static BufferedImage combineImages(BufferedImage imageOne, BufferedImage imageTow) {
		BufferedImage combine = new BufferedImage(imageOne.getWidth(), imageOne.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = combine.getGraphics();
		g.drawImage(imageOne, 0, 0, null);
		g.drawImage(imageTow, 0, 0, null);

		return combine;

	}

	public static Point[] getPoints(String wkt) {
		String[] pointsString = wkt.split(",");
		Point[] points = new Point[pointsString.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(pointsString[i]);
		}
		return points;
	}

	private static byte[] convertBuffImg(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		baos.flush();
		return baos.toByteArray();
	}

}
